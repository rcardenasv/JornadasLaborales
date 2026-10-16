#!/usr/bin/env pwsh
<#
.SYNOPSIS
    File Watcher for Automatic Test Runner Invocation
    
.DESCRIPTION
    Watches for file changes in the Employees API project and automatically
    invokes the strategic test runner sub-agent when relevant files are modified.
    
    This script is meant to be run in the background by the main agent.
    
.USAGE
    # Run in background
    ./watch-and-test.ps1 -PollInterval 2
    
    # Or invoke manually
    ./watch-and-test.ps1 -Once -ChangedFiles "src/test/.../EmployeeTest.java"
#>

param(
    [int]$PollInterval = 3,
    [switch]$Once,
    [string[]]$WatchPaths = @(
        "src/test/java/**/*Test.java",
        "src/test/java/**/*IT.java",
        "src/main/java/**/domain/**/*.java",
        "src/main/java/**/application/**/*.java",
        "src/main/java/**/infrastructure/rest/**/*.java",
        "src/main/java/**/infrastructure/persistence/**/*.java"
    )
)

$ProjectDir = "$PSScriptRoot/employees-api"
$StateFile = "$env:TEMP/test-watcher-state-$PID.json"

# Track file hashes to detect changes
$fileHashes = @{}

function Get-FileHashes {
    param([string[]]$Paths)
    
    $hashes = @{}
    foreach ($pattern in $Paths) {
        $files = Get-ChildItem -Path $ProjectDir -Filter $pattern.Replace("**", "*") -Recurse -ErrorAction SilentlyContinue -File
        foreach ($file in $files) {
            try {
                $hash = Get-FileHash -Path $file.FullName -Algorithm MD5
                $relPath = $file.FullName.Substring($ProjectDir.Length + 1)
                $hashes[$relPath] = $hash.Hash
            }
            catch { }
        }
    }
    return $hashes
}

function Detect-Changes {
    param([hashtable]$OldHashes, [hashtable]$NewHashes)
    
    $changed = @()
    
    # New or modified files
    foreach ($path in $NewHashes.Keys) {
        if (-not $OldHashes.ContainsKey($path) -or $OldHashes[$path] -ne $NewHashes[$path]) {
            $changed += $path
        }
    }
    
    # Deleted files (optional - could trigger tests too)
    foreach ($path in $OldHashes.Keys) {
        if (-not $NewHashes.ContainsKey($path)) {
            $changed += "$path (deleted)"
        }
    }
    
    return $changed
}

function Invoke-StrategicTestRunner {
    param([string[]]$ChangedFiles)
    
    $trigger = if ($ChangedFiles.Count -gt 0) { 'file-change' } else { 'scheduled' }
    $changedFilesStr = $ChangedFiles -join ','
    
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Changes detected: $($ChangedFiles.Count) file(s)" -ForegroundColor Yellow
    $ChangedFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Gray }
    
    # Invoke the test runner script
    $result = & "$ProjectDir/run-tests-strategic.ps1" -Stage combined -Trigger $trigger -ChangedFiles $changedFilesStr
    
    # Parse JSON result
    try {
        $testResult = $result | ConvertFrom-Json
        
        Write-Host "[$(Get-Date -Format 'HH:mm:ss')] Test run complete: $($testResult.summary.testsRun) tests, $($testResult.summary.failures) failures, $($testResult.summary.errors) errors" -ForegroundColor Cyan
        Write-Host "  Coverage: $($testResult.coverage.line)% line, $($testResult.coverage.branch)% branch $([string]::Format('({0})', $(if ($testResult.coverage.thresholdMet) { 'PASS' } else { 'FAIL < 80%' })))" -ForegroundColor $(if ($testResult.coverage.thresholdMet) { 'Green' } else { 'Red' })
        
        if ($testResult.failedTests.Count -gt 0) {
            Write-Host "  FAILED TESTS:" -ForegroundColor Red
            foreach ($ft in $testResult.failedTests) {
                Write-Host "    $($ft.class).$($ft.method): $($ft.error)" -ForegroundColor Red
                Write-Host "      Root cause: $($ft.rootCause)" -ForegroundColor Gray
                Write-Host "      Suggestion: $($ft.suggestion)" -ForegroundColor Yellow
            }
        }
        
        if ($testResult.newFailures.Count -gt 0) {
            Write-Host "  NEW REGRESSIONS: $($testResult.newFailures -join ', ')" -ForegroundColor Red
        }
        
        if ($testResult.fixedSinceLastRun.Count -gt 0) {
            Write-Host "  FIXED: $($testResult.fixedSinceLastRun -join ', ')" -ForegroundColor Green
        }
        
        # Return structured result for main agent to consume
        return $testResult
    }
    catch {
        Write-Error "Failed to parse test runner output: $_"
        Write-Host "Raw output:" -ForegroundColor Red
        Write-Host $result
        return $null
    }
}

# Initialize
Write-Host "Starting test watcher for Employees API..." -ForegroundColor Green
Write-Host "Watching paths:" -ForegroundColor Cyan
$WatchPaths | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }

$fileHashes = Get-FileHashes -Paths $WatchPaths
Write-Host "Initial snapshot: $($fileHashes.Count) files tracked" -ForegroundColor Green

if ($Once) {
    $changes = Detect-Changes -OldHashes @{} -NewHashes $fileHashes
    if ($changes.Count -gt 0) {
        Invoke-StrategicTestRunner -ChangedFiles $changes
    }
    exit
}

# Main watch loop
try {
    while ($true) {
        Start-Sleep -Seconds $PollInterval
        
        $newHashes = Get-FileHashes -Paths $WatchPaths
        $changes = Detect-Changes -OldHashes $fileHashes -NewHashes $newHashes
        
        if ($changes.Count -gt 0) {
            Invoke-StrategicTestRunner -ChangedFiles $changes
            $fileHashes = $newHashes
        }
    }
}
finally {
    Write-Host "Test watcher stopped." -ForegroundColor Yellow
}