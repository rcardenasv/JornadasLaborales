package com.assessment.employees.domain.model;

import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

public class WorkShift {
    private UUID id;
    private Document employeeDocument;
    private Date date;
    private Time startTime;
    private Time endTime;
    private Duration workedHours;
    private Duration daytimeHours;
    private Duration nighttimeHours;
    private DayType dayType;
    private ShiftType shiftType;

    public WorkShift(UUID id, Document employeeDocument, LocalDate date,
                     LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.employeeDocument = employeeDocument;
        this.date = new Date(date);
        this.startTime = new Time(startTime);
        this.endTime = new Time(endTime);
        validate();
        this.workedHours = calculateWorkedHours();
        this.daytimeHours = calculateDaytimeHours();
        this.nighttimeHours = workedHours.minus(daytimeHours);
        this.dayType = calculateDayType();
        this.shiftType = calculateShiftType();
    }

    private LocalDateTime getStartDateTime() {
        return date.value().atTime(startTime.value());
    }

    private LocalDateTime getEndDateTime() {
        LocalDate endDate = endTime.value().isBefore(startTime.value())
            ? date.value().plusDays(1)
            : date.value();
        return endDate.atTime(endTime.value());
    }

    public Duration getWorkedHoursWithin(LocalDateTime startInclusive,
                                         LocalDateTime endExclusive) {
        LocalDateTime overlapStart = getStartDateTime().isAfter(startInclusive)
            ? getStartDateTime()
            : startInclusive;
        LocalDateTime overlapEnd = getEndDateTime().isBefore(endExclusive)
            ? getEndDateTime()
            : endExclusive;
        return overlapStart.isBefore(overlapEnd)
            ? Duration.between(overlapStart, overlapEnd)
            : Duration.ZERO;
    }

    public boolean overlaps(WorkShift other) {
        return getStartDateTime().isBefore(other.getEndDateTime())
            && other.getStartDateTime().isBefore(getEndDateTime());
    }

    public LocalDate getEndDate() {
        return endTime.value().isBefore(startTime.value())
            ? date.value().plusDays(1)
            : date.value();
    }

    public LocalDate getWeekStartDate() {
        return date.value().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void validate() {
        if (employeeDocument == null) {
            throw new IllegalArgumentException("Employee document cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (endTime.value().equals(startTime.value())) {
            throw new IllegalArgumentException("End time must differ from start time");
        }
    }

    private Duration calculateWorkedHours() {
        return Duration.between(getStartDateTime(), getEndDateTime());
    }

    private Duration calculateDaytimeHours() {
        LocalTime daytimeStart = DayNightPolicy.daytimeStart(date.value());
        LocalTime daytimeEnd = DayNightPolicy.daytimeEnd(date.value());
        Duration daytime = Duration.ZERO;
        for (LocalDate currentDate = date.value();
             !currentDate.isAfter(getEndDate());
             currentDate = currentDate.plusDays(1)) {
            LocalDateTime daytimeStartDateTime = currentDate.atTime(daytimeStart);
            LocalDateTime daytimeEndDateTime = currentDate.atTime(daytimeEnd);
            daytime = daytime.plus(getWorkedHoursWithin(daytimeStartDateTime, daytimeEndDateTime));
        }
        return daytime;
    }

    private DayType calculateDayType() {
        LocalDate shiftDate = date.value();
        if (ColombianHolidayCalendar.isHoliday(shiftDate)) {
            return DayType.FESTIVO;
        }
        return shiftDate.getDayOfWeek() == DayOfWeek.SUNDAY
            ? DayType.DOMINICAL
            : DayType.ORDINARIO;
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

    public void update(LocalDate date, LocalTime startTime, LocalTime endTime) {
        WorkShift updated = new WorkShift(id, employeeDocument, date, startTime, endTime);
        this.date = updated.date;
        this.startTime = updated.startTime;
        this.endTime = updated.endTime;
        this.workedHours = updated.workedHours;
        this.daytimeHours = updated.daytimeHours;
        this.nighttimeHours = updated.nighttimeHours;
        this.dayType = updated.dayType;
        this.shiftType = updated.shiftType;
    }

    // Getters
    public UUID getId() { return id; }
    public Document getEmployeeDocument() { return employeeDocument; }
    public LocalDate getDate() { return date.value(); }
    public LocalTime getStartTime() { return startTime.value(); }
    public LocalTime getEndTime() { return endTime.value(); }
    public Duration getWorkedHours() { return workedHours; }
    public Duration getDaytimeHours() { return daytimeHours; }
    public Duration getNighttimeHours() { return nighttimeHours; }
    public DayType getDayType() { return dayType; }
    public ShiftType getShiftType() { return shiftType; }
}