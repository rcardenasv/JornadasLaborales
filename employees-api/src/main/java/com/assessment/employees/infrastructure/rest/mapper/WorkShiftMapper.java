package com.assessment.employees.infrastructure.rest.mapper;

import com.assessment.employees.application.dto.BulkCreateWorkShiftsCommand;
import com.assessment.employees.application.dto.CreateWorkShiftCommand;
import com.assessment.employees.application.dto.UpdateWorkShiftCommand;
import com.assessment.employees.application.dto.WeeklyWorkloadSummaryResult;
import com.assessment.employees.application.dto.WorkShiftListResult;
import com.assessment.employees.application.dto.WorkShiftResult;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.infrastructure.rest.dto.BulkCreateWorkShiftsRequest;
import com.assessment.employees.infrastructure.rest.dto.CreateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.UpdateWorkShiftRequest;
import com.assessment.employees.infrastructure.rest.dto.WeeklySummaryResponse;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftListResponse;
import com.assessment.employees.infrastructure.rest.dto.WorkShiftResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.WeekFields;
import java.util.UUID;

/**
 * Mapper between REST DTOs and application DTOs.
 * The hours reported in the API are decimal with two places (HALF_UP),
 * calculated from the exact minutes of the domain.
 */
public final class WorkShiftMapper {

    private static final int HOURS_SCALE = 2;

    private WorkShiftMapper() {}

    public static BigDecimal toHours(Duration duration) {
        return BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(60), HOURS_SCALE, RoundingMode.HALF_UP);
    }

    public static CreateWorkShiftCommand toCommand(String documentType, String documentNumber,
                                                    CreateWorkShiftRequest request) {
        return new CreateWorkShiftCommand(
            Document.of(documentType, documentNumber),
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }

    public static BulkCreateWorkShiftsCommand toCommand(String documentType, String documentNumber,
                                                         BulkCreateWorkShiftsRequest request) {
        return new BulkCreateWorkShiftsCommand(
            Document.of(documentType, documentNumber),
            request.from(),
            request.to(),
            request.weekdays(),
            request.startTime(),
            request.endTime()
        );
    }

    public static UpdateWorkShiftCommand toCommand(UUID shiftId, Document document,
                                                   UpdateWorkShiftRequest request) {
        return new UpdateWorkShiftCommand(
            shiftId,
            document,
            request.date(),
            request.startTime(),
            request.endTime()
        );
    }

    /** Convierte el identificador textual de la ruta en UUID (formato inválido → 400). */
    public static UUID toShiftId(String shiftId) {
        return UUID.fromString(shiftId);
    }

    public static Document toDocument(String documentType, String documentNumber) {
        return Document.of(documentType, documentNumber);
    }

    public static WorkShiftResponse toResponse(WorkShiftResult result) {
        return new WorkShiftResponse(
            result.id(),
            result.date(),
            result.startTime(),
            result.endTime(),
            result.dayType().name(),
            result.shiftType().name(),
            toHours(result.workedHours()),
            toHours(result.daytimeHours()),
            toHours(result.nighttimeHours())
        );
    }

    public static WorkShiftListResponse toListResponse(WorkShiftListResult result) {
        return new WorkShiftListResponse(
            result.shifts().stream().map(WorkShiftMapper::toResponse).toList(),
            toHours(result.totalWorkedHours()),
            result.weeklySummaries().stream()
                .map(WorkShiftMapper::toSummaryResponse)
                .toList()
        );
    }

    public static WeeklySummaryResponse toSummaryResponse(WeeklyWorkloadSummaryResult summary) {
        LocalDate weekStart = summary.weekStart();
        WeekFields iso = WeekFields.ISO;
        return new WeeklySummaryResponse(
            "%d-W%02d".formatted(weekStart.get(iso.weekBasedYear()),
                weekStart.get(iso.weekOfWeekBasedYear())),
            weekStart,
            weekStart.plusDays(6),
            toHours(summary.scheduledHours()),
            toHours(summary.ordinaryLimitHours()),
            toHours(summary.overtimeHours()),
            toHours(summary.remainingOrdinaryHours())
        );
    }

    public static LocalDate parseIsoDate(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                "Las fechas deben usar el formato ISO yyyy-MM-dd.", exception);
        }
    }
}
