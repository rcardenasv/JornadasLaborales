package com.assessment.employees.domain.model;

import com.assessment.employees.domain.exception.InvalidWorkShiftException;
import com.assessment.employees.domain.exception.WorkShiftOverlapException;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/**
 * Jornada laboral inmutable: toda modificación produce una nueva instancia
 * mediante {@link #with}. La clasificación (desglose diurno/nocturno, tipo de
 * día y tipo de turno) se calcula en el constructor según las reglas
 * colombianas vigentes para la fecha de inicio.
 */
public class WorkShift {
    private final UUID id;
    private final Document employeeDocument;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Duration workedHours;
    private final Duration daytimeHours;
    private final Duration nighttimeHours;
    private final DayType dayType;
    private final ShiftType shiftType;

    public WorkShift(UUID id, Document employeeDocument, LocalDate date,
                     LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeDocument = employeeDocument;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        validate();
        this.workedHours = calculateWorkedHours();
        this.daytimeHours = calculateDaytimeHours();
        this.nighttimeHours = workedHours.minus(daytimeHours);
        this.dayType = calculateDayType();
        this.shiftType = calculateShiftType();
    }

    private void validate() {
        if (id == null) {
            throw new InvalidWorkShiftException("El identificador de la jornada es obligatorio.");
        }
        if (employeeDocument == null) {
            throw new InvalidWorkShiftException("El empleado de la jornada es obligatorio.");
        }
        if (date == null) {
            throw new InvalidWorkShiftException("La fecha de la jornada es obligatoria.");
        }
        if (startTime == null) {
            throw new InvalidWorkShiftException("La hora de inicio de la jornada es obligatoria.");
        }
        if (endTime == null) {
            throw new InvalidWorkShiftException("La hora de fin de la jornada es obligatoria.");
        }
        if (endTime.equals(startTime)) {
            throw new InvalidWorkShiftException("La hora de fin debe ser distinta de la hora de inicio.");
        }
    }

    private LocalDateTime getStartDateTime() {
        return date.atTime(startTime);
    }

    private LocalDateTime getEndDateTime() {
        LocalDate endDate = endTime.isBefore(startTime) ? date.plusDays(1) : date;
        return endDate.atTime(endTime);
    }

    public Duration getWorkedHoursWithin(LocalDateTime startInclusive,
                                         LocalDateTime endExclusive) {
        LocalDateTime overlapStart = max(getStartDateTime(), startInclusive);
        LocalDateTime overlapEnd = min(getEndDateTime(), endExclusive);
        return overlapStart.isBefore(overlapEnd)
            ? Duration.between(overlapStart, overlapEnd)
            : Duration.ZERO;
    }

    public boolean overlaps(WorkShift other) {
        return getStartDateTime().isBefore(other.getEndDateTime())
            && other.getStartDateTime().isBefore(getEndDateTime());
    }

    public LocalDate getEndDate() {
        return endTime.isBefore(startTime) ? date.plusDays(1) : date;
    }

    public LocalDate getWeekStartDate() {
        return weekStartOf(date);
    }

    private Duration calculateWorkedHours() {
        return Duration.between(getStartDateTime(), getEndDateTime());
    }

    private Duration calculateDaytimeHours() {
        LocalTime daytimeStart = DayNightPolicy.daytimeStart();
        LocalTime daytimeEnd = DayNightPolicy.daytimeEnd(date);
        Duration daytime = Duration.ZERO;
        for (LocalDate currentDate = date;
             !currentDate.isAfter(getEndDate());
             currentDate = currentDate.plusDays(1)) {
            daytime = daytime.plus(getWorkedHoursWithin(
                currentDate.atTime(daytimeStart), currentDate.atTime(daytimeEnd)));
        }
        return daytime;
    }

    private DayType calculateDayType() {
        if (ColombianHolidayCalendar.isHoliday(date)) {
            return DayType.FESTIVO;
        }
        return date.getDayOfWeek() == DayOfWeek.SUNDAY ? DayType.DOMINICAL : DayType.ORDINARIO;
    }

    private ShiftType calculateShiftType() {
        if (daytimeHours.isZero()) {
            return ShiftType.NOCTURNA;
        }
        return nighttimeHours.isZero() ? ShiftType.DIURNA : ShiftType.MIXTA;
    }

    public static WorkShift create(Document employeeDocument, LocalDate date,
                                   LocalTime startTime, LocalTime endTime) {
        return new WorkShift(UUID.randomUUID(), employeeDocument, date, startTime, endTime);
    }

    /** Devuelve una nueva jornada con la misma identidad pero nueva fecha y horario. */
    public WorkShift with(LocalDate date, LocalTime startTime, LocalTime endTime) {
        return new WorkShift(id, employeeDocument, date, startTime, endTime);
    }

    /** Lunes de la semana que toca la fecha dada (corte ISO: lunes 00:00). */
    public static LocalDate weekStartOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    /** Rechaza `candidate` si se solapa con alguna jornada de `existing` (409 en REST). */
    public static void rejectOverlap(List<WorkShift> existing, WorkShift candidate) {
        existing.stream()
            .filter(candidate::overlaps)
            .findFirst()
            .ifPresent(shift -> {
                throw new WorkShiftOverlapException(candidate.getDate(), candidate.getStartTime(),
                    candidate.getEndTime());
            });
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a : b;
    }

    // Getters
    public UUID getId() { return id; }
    public Document getEmployeeDocument() { return employeeDocument; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Duration getWorkedHours() { return workedHours; }
    public Duration getDaytimeHours() { return daytimeHours; }
    public Duration getNighttimeHours() { return nighttimeHours; }
    public DayType getDayType() { return dayType; }
    public ShiftType getShiftType() { return shiftType; }
}
