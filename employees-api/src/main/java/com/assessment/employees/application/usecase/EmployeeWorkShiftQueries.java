package com.assessment.employees.application.usecase;

import com.assessment.employees.domain.exception.EmployeeNotFoundException;
import com.assessment.employees.domain.model.Document;
import com.assessment.employees.domain.model.WorkShift;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Consultas compartidas por los casos de uso de jornadas: garantiza la existencia
 * del empleado y recupera las jornadas relevantes para validar solapes y carga semanal.
 */
final class EmployeeWorkShiftQueries {
    private final EmployeeRepository employeeRepository;
    private final WorkShiftRepository workShiftRepository;

    EmployeeWorkShiftQueries(EmployeeRepository employeeRepository,
                             WorkShiftRepository workShiftRepository) {
        this.employeeRepository = employeeRepository;
        this.workShiftRepository = workShiftRepository;
    }

    void requireExistingEmployee(Document document) {
        if (employeeRepository.findByDocument(document).isEmpty()) {
            throw new EmployeeNotFoundException(document);
        }
    }

    /** Jornadas próximas a la semana ISO del día recibido, extendidas un día hacia atrás. */
    List<WorkShift> shiftsAroundWeekOf(Document document, LocalDate date) {
        LocalDate weekStart = WorkShift.weekStartOf(date);
        return workShiftRepository.findByEmployeeDocumentAndDateRange(
            document, weekStart.minusDays(1), weekStart.plusWeeks(2));
    }

    /** Jornadas entre las semanas ISO de `from` y `to`, extendidas un día hacia atrás. */
    List<WorkShift> shiftsBetweenWeeksOf(Document document, LocalDate from, LocalDate to) {
        LocalDate firstWeek = WorkShift.weekStartOf(from);
        LocalDate lastWeek = WorkShift.weekStartOf(to);
        return workShiftRepository.findByEmployeeDocumentAndDateRange(
            document, firstWeek.minusDays(1), lastWeek.plusWeeks(1).plusDays(6));
    }
}
