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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeResourceTest {
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
    void createReturns201AndLocation() {
        UUID id = UUID.randomUUID();
        when(createEmployee.execute(any())).thenReturn(employee(id));
        when(uriInfo.getAbsolutePathBuilder()).thenReturn(UriBuilder.fromUri("http://localhost/api/v1/employees"));

        Response response = resource.create(new CreateEmployeeRequest("EMP001", "Ana", "Diaz",
            "ana@example.com", "Analyst", LocalDate.now()), uriInfo);

        assertEquals(201, response.getStatus());
        assertEquals("http://localhost/api/v1/employees/" + id, response.getLocation().toString());
    }

    @Test
    void listReturnsRepositoryPaginationMetadata() {
        when(listEmployees.execute(1, 5)).thenReturn(new EmployeePageResult(List.of(employee(UUID.randomUUID())), 1, 5, 12));
        Response response = resource.list(1, 5);
        PagedEmployeeResponse body = (PagedEmployeeResponse) response.getEntity();
        assertEquals(200, response.getStatus());
        assertEquals(12, body.totalItems());
        assertEquals(1, body.items().size());
    }

    @Test
    void getUpdateAndDeleteDelegateWithPathId() {
        String id = UUID.randomUUID().toString();
        when(getEmployee.execute(id)).thenReturn(employee(UUID.fromString(id)));
        when(updateEmployee.execute(any())).thenReturn(employee(UUID.fromString(id)));

        assertEquals(200, resource.get(id).getStatus());
        assertEquals(200, resource.update(id, new UpdateEmployeeRequest("EMP001", "Ana", "Diaz",
            "ana@example.com", "Analyst", LocalDate.now())).getStatus());
        assertEquals(204, resource.delete(id).getStatus());
        verify(updateEmployee).execute(argThat(command -> command.id().equals(id)));
        verify(deleteEmployee).execute(id);
    }

    private EmployeeResult employee(UUID id) {
        return new EmployeeResult(id, "EMP001", "Ana", "Diaz", "ana@example.com", "Analyst", LocalDate.now());
    }
}
