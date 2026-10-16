package com.assessment.employees.infrastructure.rest.resource;

import com.assessment.employees.application.dto.EmployeePageResult;
import com.assessment.employees.application.dto.EmployeeResult;
import com.assessment.employees.application.usecase.*;
import com.assessment.employees.infrastructure.rest.dto.CreateEmployeeRequest;
import com.assessment.employees.infrastructure.rest.dto.PagedEmployeeResponse;
import com.assessment.employees.infrastructure.rest.dto.UpdateEmployeeRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeResourceTest {
    private static final EmployeeResult EMPLOYEE = new EmployeeResult("CC", "123456", "Ana", "Diaz",
        "ana@example.com", "Analyst", LocalDate.of(2020, 1, 1));

    @Mock CreateEmployee createEmployee;
    @Mock GetEmployee getEmployee;
    @Mock ListEmployees listEmployees;
    @Mock UpdateEmployee updateEmployee;
    @Mock DeleteEmployee deleteEmployee;
    @Mock UriInfo uriInfo;
    private EmployeeResource resource;

    @BeforeEach
    void setUp() {
        resource = new EmployeeResource(createEmployee, getEmployee, listEmployees, updateEmployee, deleteEmployee);
    }

    @Test
    void createReturns201AndNaturalIdentityLocation() {
        when(createEmployee.execute(any())).thenReturn(EMPLOYEE);
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/api/v1/employees"));

        Response response = resource.create(new CreateEmployeeRequest("CC", "123456", "Ana", "Diaz",
            "ana@example.com", "Analyst", EMPLOYEE.hireDate()), uriInfo);

        assertEquals(201, response.getStatus());
        assertEquals("http://localhost/api/v1/employees/CC/123456", response.getLocation().toString());
    }

    @Test
    void listReturnsRepositoryPaginationMetadata() {
        when(listEmployees.execute(1, 5)).thenReturn(new EmployeePageResult(List.of(EMPLOYEE), 1, 5, 12));
        Response response = resource.list(1, 5);
        PagedEmployeeResponse body = (PagedEmployeeResponse) response.getEntity();
        assertEquals(200, response.getStatus());
        assertEquals(12, body.totalItems());
        assertEquals(1, body.items().size());
    }

    @Test
    void getUpdateAndDeleteDelegateWithDocumentPath() {
        when(getEmployee.execute("CC", "123456")).thenReturn(EMPLOYEE);
        when(updateEmployee.execute(any())).thenReturn(EMPLOYEE);
        UpdateEmployeeRequest request = new UpdateEmployeeRequest("Ana", "Diaz",
            "ana@example.com", "Analyst", EMPLOYEE.hireDate());

        assertEquals(200, resource.get("CC", "123456").getStatus());
        assertEquals(200, resource.update("CC", "123456", request).getStatus());
        assertEquals(204, resource.delete("CC", "123456").getStatus());
        verify(updateEmployee).execute(argThat(command ->
            command.documentType().equals("CC") && command.documentNumber().equals("123456")));
        verify(deleteEmployee).execute("CC", "123456");
    }
}
