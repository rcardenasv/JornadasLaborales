package com.assessment.employees.infrastructure.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EmployeeResourceIT {
    @Test
    void shouldCreateRetrieveListAndDeleteEmployee() {
        String idSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String request = """
            {
              "documentNumber": "EMP%s",
              "firstName": "Ana",
              "lastName": "Diaz",
              "email": "ana.%s@example.com",
              "position": "Analyst",
              "hireDate": "%s"
            }
            """.formatted(idSuffix, idSuffix, LocalDate.now().minusDays(1));

        String location = RestAssured.given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
        assertNotNull(location);
        assertFalse(location.isBlank());

        RestAssured.given().when().get(location).then()
            .statusCode(200)
            .body("email", equalTo("ana." + idSuffix + "@example.com"));

        RestAssured.given().when().get("/api/v1/employees?page=0&size=5").then()
            .statusCode(200)
            .body("items", not(empty()))
            .body("totalItems", greaterThan(0));

        RestAssured.given().when().delete(location).then().statusCode(204);
    }
}
