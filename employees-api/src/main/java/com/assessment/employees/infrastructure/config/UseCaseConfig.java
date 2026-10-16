package com.assessment.employees.infrastructure.config;

import com.assessment.employees.application.usecase.CreateEmployee;
import com.assessment.employees.application.usecase.CreateWorkShift;
import com.assessment.employees.application.usecase.BulkCreateWorkShifts;
import com.assessment.employees.application.usecase.DeleteEmployee;
import com.assessment.employees.application.usecase.DeleteWorkShift;
import com.assessment.employees.application.usecase.GetEmployee;
import com.assessment.employees.application.usecase.GetWorkShift;
import com.assessment.employees.application.usecase.ListEmployeeWorkShifts;
import com.assessment.employees.application.usecase.ListEmployees;
import com.assessment.employees.application.usecase.UpdateEmployee;
import com.assessment.employees.application.usecase.UpdateWorkShift;
import com.assessment.employees.domain.repository.EmployeeRepository;
import com.assessment.employees.domain.repository.WorkShiftRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Configuration for application services.
 * Produces and wires use cases with their dependencies.
 */
@ApplicationScoped
public class UseCaseConfig {
    
    @Produces
    @ApplicationScoped
    public CreateEmployee createEmployee(EmployeeRepository repository) {
        return new CreateEmployee(repository);
    }
    
    @Produces
    @ApplicationScoped
    public GetEmployee getEmployee(EmployeeRepository repository) {
        return new GetEmployee(repository);
    }
    
    @Produces
    @ApplicationScoped
    public ListEmployees listEmployees(EmployeeRepository repository) {
        return new ListEmployees(repository);
    }
    
    @Produces
    @ApplicationScoped
    public UpdateEmployee updateEmployee(EmployeeRepository repository) {
        return new UpdateEmployee(repository);
    }
    
    @Produces
    @ApplicationScoped
    public DeleteEmployee deleteEmployee(EmployeeRepository repository) {
        return new DeleteEmployee(repository);
    }
    
    @Produces
    @ApplicationScoped
    public CreateWorkShift createWorkShift(WorkShiftRepository workShiftRepository,
                                           EmployeeRepository employeeRepository) {
        return new CreateWorkShift(workShiftRepository, employeeRepository);
    }

    @Produces
    @ApplicationScoped
    public BulkCreateWorkShifts bulkCreateWorkShifts(WorkShiftRepository workShiftRepository,
                                                      EmployeeRepository employeeRepository) {
        return new BulkCreateWorkShifts(workShiftRepository, employeeRepository);
    }
    
    @Produces
    @ApplicationScoped
    public GetWorkShift getWorkShift(WorkShiftRepository repository) {
        return new GetWorkShift(repository);
    }
    
    @Produces
    @ApplicationScoped
    public ListEmployeeWorkShifts listEmployeeWorkShifts(WorkShiftRepository workShiftRepository,
                                                          EmployeeRepository employeeRepository) {
        return new ListEmployeeWorkShifts(workShiftRepository, employeeRepository);
    }
    
    @Produces
    @ApplicationScoped
    public UpdateWorkShift updateWorkShift(WorkShiftRepository workShiftRepository,
                                            EmployeeRepository employeeRepository) {
        return new UpdateWorkShift(workShiftRepository, employeeRepository);
    }
    
    @Produces
    @ApplicationScoped
    public DeleteWorkShift deleteWorkShift(WorkShiftRepository workShiftRepository,
                                            EmployeeRepository employeeRepository) {
        return new DeleteWorkShift(workShiftRepository, employeeRepository);
    }
}