#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Strategic Test Runner for Employees API
    Runs tests in stages and outputs structured JSON for sub-agent consumption

.DESCRIPTION
    This script executes Maven test phases, parses Surefire/Failsafe reports and JaCoCo coverage,
    and outputs a structured JSON object that the strategic test runner sub-agent can consume.

.PARAMETER Stage
    Test stage to run: 'unit', 'integration', or 'combined' (default: 'combined')

.PARAMETER Trigger
    Trigger type: 'file-change', 'manual', 'scheduled' (default: 'manual')

.PARAMETER ChangedFiles
    Comma-separated list of changed files that triggered the run

.EXAMPLE
    ./run-tests-strategic.ps1 -Stage combined -Trigger file-change -ChangedFiles "src/test/.../EmployeeTest.java,src/main/.../Employee.java"

.OUTPUTS
    JSON object to stdout with test results, coverage, and actionable suggestions
#>

param(
    [ValidateSet('unit', 'integration', 'combined')]
    [string]$Stage = 'combined',
    
    [ValidateSet('file-change', 'manual', 'scheduled')]
    [string]$Trigger = 'manual',
    
    [string]$ChangedFiles = ''
)

$ErrorActionPreference = 'Stop'
$ProjectDir = $PSScriptRoot
$SessionId = "subagent-test-$(Get-Random)-$(Get-Random)-$(Get-Date -Format 'yyyyMMddHHmmss')"

# Parse changed files
$changedFilesArray = if ($ChangedFiles) { $ChangedFiles -split ',' | ForEach-Object { $_.Trim() } } else { @() }

# Initialize result object
$result = [ordered]@{
    sessionId           = $SessionId
    stage               = $Stage
    trigger             = $Trigger
    changedFiles        = $changedFilesArray
    summary             = @{ testsRun = 0; failures = 0; errors = 0; skipped = 0; durationMs = 0 }
    coverage            = @{ line = 0.0; branch = 0.0; thresholdMet = $false; deltaVsLastRun = 0.0 }
    failedTests         = @()
    newFailures         = @()
    fixedSinceLastRun   = @()
    artifacts           = @{
        jacocoXml     = "target/site/jacoco/jacoco.xml"
        surefireDir   = "target/surefire-reports"
        failsafeDir   = "target/failsafe-reports"
    }
}

function Write-Result {
    $result | ConvertTo-Json -Depth 10 -Compress
}

function Run-Command {
    param(
        [string]$Command,
        [int]$TimeoutSec = 180
    )
    
    $startTime = [DateTime]::Now
    try {
        $process = Start-Process -FilePath "cmd.exe" -ArgumentList "/c $Command" -WorkingDirectory $ProjectDir -Wait -PassThru -RedirectStandardOutput "$env:TEMP\stdout-$SessionId.txt" -RedirectStandardError "$env:TEMP\stderr-$SessionId.txt"
        $durationMs = [math]::Round(([DateTime]::Now - $startTime).TotalMilliseconds)
        
        $stdout = Get-Content "$env:TEMP\stdout-$SessionId.txt" -Raw -ErrorAction SilentlyContinue
        $stderr = Get-Content "$env:TEMP\stderr-$SessionId.txt" -Raw -ErrorAction SilentlyContinue
        
        Remove-Item "$env:TEMP\stdout-$SessionId.txt", "$env:TEMP\stderr-$SessionId.txt" -ErrorAction SilentlyContinue
        
        return @{
            exitCode = $process.ExitCode
            stdout   = $stdout
            stderr   = $stderr
            duration = $durationMs
        }
    }
    catch {
        return @{
            exitCode = -1
            stdout   = ""
            stderr   = $_.Exception.Message
            duration = [math]::Round(([DateTime]::Now - $startTime).TotalMilliseconds)
        }
    }
}

function Parse-SurefireReports {
    param([string]$ReportDir)
    
    $failedTests = @()
    $totalTests = 0
    $totalFailures = 0
    $totalErrors = 0
    $totalSkipped = 0
    
    if (-not (Test-Path $ReportDir)) { return @{ failedTests = $failedTests; summary = @{} } }
    
    $xmlFiles = Get-ChildItem -Path $ReportDir -Filter "TEST-*.xml" -ErrorAction SilentlyContinue
    foreach ($xmlFile in $xmlFiles) {
        try {
            [xml]$xml = Get-Content $xmlFile.FullName -Raw
            $testsuite = $xml.testsuite
            
            $totalTests += [int]$testsuite.tests
            $totalFailures += [int]$testsuite.failures
            $totalErrors += [int]$testsuite.errors
            $totalSkipped += [int]$testsuite.skipped
            
            if ($testsuite.testcase) {
                $testcases = @($testsuite.testcase)
                foreach ($tc in $testcases) {
                    if ($tc.failure -or $tc.error) {
                        $className = $tc.classname
                        $methodName = $tc.name
                        $errorMsg = if ($tc.failure) { $tc.failure.'#text' } else { $tc.error.'#text' }
                        $errorType = if ($tc.failure) { $tc.failure['@type'] } else { $tc.error['@type'] }
                        
                        # Extract root cause and suggestion
                        $rootCause = Extract-RootCause $errorMsg $errorType
                        $suggestion = Generate-Suggestion $className $methodName $errorMsg $errorType
                        
                        $failedTests += @{
                            class      = $className
                            method     = $methodName
                            error      = "$errorType: $($errorMsg.Substring(0, [math]::Min(200, $errorMsg.Length)))"
                            rootCause  = $rootCause
                            suggestion = $suggestion
                        }
                    }
                }
            }
        }
        catch {
            Write-Warning "Failed to parse $($xmlFile.Name): $_"
        }
    }
    
    return @{
        failedTests = $failedTests
        summary     = @{
            testsRun  = $totalTests
            failures  = $totalFailures
            errors    = $totalErrors
            skipped   = $totalSkipped
        }
    }
}

function Parse-FailsafeReports {
    param([string]$ReportDir)
    # Same structure as Surefire
    Parse-SurefireReports -ReportDir $ReportDir
}

function Parse-JaCoCoXml {
    param([string]$XmlPath)
    
    if (-not (Test-Path $XmlPath)) {
        return @{ line = 0.0; branch = 0.0; thresholdMet = $false }
    }
    
    try {
        [xml]$xml = Get-Content $XmlPath -Raw
        $report = $xml.report
        $counterLine = $report.counter | Where-Object { $_.type -eq 'LINE' }
        $counterBranch = $report.counter | Where-Object { $_.type -eq 'BRANCH' }
        
        $lineCovered = [int]$counterLine.covered
        $lineMissed = [int]$counterLine.missed
        $branchCovered = [int]$counterBranch.covered
        $branchMissed = [int]$counterBranch.missed
        
        $lineTotal = $lineCovered + $lineMissed
        $branchTotal = $branchCovered + $branchMissed
        
        $linePct = if ($lineTotal -gt 0) { [math]::Round(($lineCovered / $lineTotal) * 100, 2) } else { 0 }
        $branchPct = if ($branchTotal -gt 0) { [math]::Round(($branchCovered / $branchTotal) * 100, 2) } else { 0 }
        
        return @{
            line         = $linePct
            branch       = $branchPct
            thresholdMet = ($linePct -ge 80)
        }
    }
    catch {
        Write-Warning "Failed to parse JaCoCo XML: $_"
        return @{ line = 0.0; branch = 0.0; thresholdMet = $false }
    }
}

function Extract-RootCause {
    param([string]$ErrorMsg, [string]$ErrorType)
    
    # Common patterns for root cause extraction
    if ($ErrorMsg -match 'NullPointerException.*at\s+(\S+\.\w+:\d+)') {
        return "NullPointerException at $($matches[1])"
    }
    if ($ErrorMsg -match 'AssertionError.*expected:\s*<(.+?)>.*but was:\s*<(.+?)>') {
        return "Assertion failed: expected <$($matches[1])> but was <$($matches[2])>"
    }
    if ($ErrorMsg -match 'Mockito.*UnfinishedVerificationException') {
        return "Mockito verification incomplete - missing verify() or argument mismatch"
    }
    if ($ErrorMsg -match 'EntityManager.*flush|persist|merge') {
        return "JPA EntityManager operation failed - check mock configuration"
    }
    if ($ErrorMsg -match 'ConstraintViolationException|ValidationException') {
        return "Bean Validation constraint violation"
    }
    if ($ErrorMsg -match 'WorkShiftOverlapException') {
        return "Domain rule: overlapping work shifts detected"
    }
    if ($ErrorMsg -match 'DuplicateEmployeeException') {
        return "Domain rule: duplicate email or document number"
    }
    if ($ErrorMsg -match 'EmployeeNotFoundException|WorkShiftNotFoundException') {
        return "Entity not found - check repository query or test setup"
    }
    
    # Fallback: first line of stack trace
    $lines = $ErrorMsg -split "`r?`n"
    return $lines[0].Substring(0, [math]::Min(150, $lines[0].Length))
}

function Generate-Suggestion {
    param([string]$ClassName, [string]$MethodName, [string]$ErrorMsg, [string]$ErrorType)
    
    $suggestions = @()
    
    if ($ErrorMsg -match 'NullPointerException') {
        if ($ClassName -match 'Repository') {
            $suggestions += "Check @Mock EntityManager setup in @BeforeEach - ensure all methods return valid mocks"
            $suggestions += "Add when(entityManager.flush()).thenAnswer(invocation -> null) if flush() is called"
        }
        elseif ($ClassName -match 'Resource') {
            $suggestions += "Verify use case mock returns non-null Result/Optional in resource test"
        }
        else {
            $suggestions += "Add null check or ensure dependency is properly mocked/injected"
        }
    }
    
    if ($ErrorMsg -match 'AssertionError.*expected.*but was') {
        $suggestions += "Verify test expectation matches actual domain behavior - check domain logic in $ClassName"
        $suggestions += "Check if test data setup matches the scenario being tested"
    }
    
    if ($ErrorMsg -match 'Mockito.*UnfinishedVerificationException') {
        $suggestions += "Complete Mockito verification: add verify(mock).method(args) or fix argument matchers"
    }
    
    if ($ErrorMsg -match 'WorkShiftOverlapException') {
        $suggestions += "Test expects 409 Conflict for overlapping shifts - verify CreateWorkShift/ListWorkShifts use case logic"
        $suggestions += "Check WorkShift domain overlap validation uses correct time comparison"
    }
    
    if ($ErrorMsg -match 'DuplicateEmployeeException') {
        $suggestions += "Test expects 409 Conflict for duplicate email/document - verify CreateEmployee use case uniqueness check"
    }
    
    if ($ErrorMsg -match 'EmployeeNotFoundException|WorkShiftNotFoundException') {
        $suggestions += "Ensure test creates entity before querying, or verify repository findById returns Optional.empty() for missing IDs"
    }
    
    if ($ErrorMsg -match 'ConstraintViolationException') {
        $suggestions += "Check @Valid annotations on REST DTOs and Bean Validation constraints match domain rules"
    }
    
    if ($suggestions.Count -eq 0) {
        $suggestions += "Investigate $ErrorType in $ClassName.$MethodName - check test setup and domain logic"
    }
    
    return $suggestions -join ' | '
}

function Load-PreviousState {
    # In a real stateful implementation, this would load from a persistent store
    # For now, return empty state
    return @{
        previousCoverage     = @{}
        previousFailedTests  = @()
        appliedFixes         = @{}
    }
}

function Compare-WithPreviousState {
    param($currentFailedTests, $currentCoverage)
    
    $prevState = Load-PreviousState
    
    $currentFailures = $currentFailedTests | ForEach-Object { "$($_.class).$($_.method)" }
    $prevFailures = $prevState.previousFailedTests
    
    $newFailures = $currentFailures | Where-Object { $_ -notin $prevFailures }
    $fixedFailures = $prevFailures | Where-Object { $_ -notin $currentFailures }
    
    $prevLineCoverage = $prevState.previousCoverage.line
    $delta = if ($prevLineCoverage) { [math]::Round($currentCoverage.line - $prevLineCoverage, 2) } else { 0 }
    
    return @{
        newFailures       = $newFailures
        fixedSinceLastRun = $fixedFailures
        deltaVsLastRun    = $delta
    }
}

# Main execution
$totalDuration = 0

if ($Stage -in @('unit', 'combined')) {
    Write-Host "=== STAGE 1: Unit Tests ===" -ForegroundColor Cyan
    $cmdResult = Run-Command "mvnw.cmd test" -TimeoutSec 120
    $totalDuration += $cmdResult.duration
    
    $surefireResult = Parse-SurefireReports -ReportDir "$ProjectDir/target/surefire-reports"
    $result.summary.testsRun  += $surefireResult.summary.testsRun
    $result.summary.failures  += $surefireResult.summary.failures
    $result.summary.errors    += $surefireResult.summary.errors
    $result.summary.skipped   += $surefireResult.summary.skipped
    $result.failedTests       += $surefireResult.failedTests
    
    $jacocoResult = Parse-JaCoCoXml -XmlPath "$ProjectDir/target/site/jacoco/jacoco.xml"
    $result.coverage.line = $jacocoResult.line
    $result.coverage.branch = $jacocoResult.branch
    $result.coverage.thresholdMet = $jacocoResult.thresholdMet
}

if ($Stage -in @('integration', 'combined')) {
    Write-Host "=== STAGE 2: Integration Tests ===" -ForegroundColor Cyan
    $cmdResult = Run-Command "mvnw.cmd verify" -TimeoutSec 240
    $totalDuration += $cmdResult.duration
    
    $failsafeResult = Parse-FailsafeReports -ReportDir "$ProjectDir/target/failsafe-reports"
    $result.summary.testsRun  += $failsafeResult.summary.testsRun
    $result.summary.failures  += $failsafeResult.summary.failures
    $result.summary.errors    += $failsafeResult.summary.errors
    $result.summary.skipped   += $failsafeResult.summary.skipped
    $result.failedTests       += $failsafeResult.failedTests
    
    $jacocoResult = Parse-JaCoCoXml -XmlPath "$ProjectDir/target/site/jacoco/jacoco.xml"
    $result.coverage.line = $jacocoResult.line
    $result.coverage.branch = $jacocoResult.branch
    $result.coverage.thresholdMet = $jacocoResult.thresholdMet
}

$result.summary.durationMs = $totalDuration

# Compare with previous state for stateful analysis
$comparison = Compare-WithPreviousState -currentFailedTests $result.failedTests -currentCoverage $result.coverage
$result.newFailures = $comparison.newFailures
$result.fixedSinceLastRun = $comparison.fixedSinceLastRun
$result.coverage.deltaVsLastRun = $comparison.deltaVsLastRun

# Output final JSON
Write-Result