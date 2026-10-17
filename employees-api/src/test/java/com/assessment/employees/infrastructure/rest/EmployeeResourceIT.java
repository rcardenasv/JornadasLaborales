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
        String idSuffix = "%08d".formatted(
            Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000)
        );
        String request = """
            {
              "documentType": "CC",
              "documentNumber": "%s",
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
            .body("documentType", equalTo("CC"))
            .body("documentNumber", equalTo(idSuffix))
            .body("email", equalTo("ana." + idSuffix + "@example.com"));

        RestAssured.given().when().get("/api/v1/employees?page=0&size=5").then()
            .statusCode(200)
            .body("items", not(empty()))
            .body("totalItems", greaterThan(0));

        String shiftLocation = RestAssured.given()
            .contentType("application/json")
            .body("""
                {"date":"2026-11-01","startTime":"09:00","endTime":"17:00"}
                """)
            .when()
            .post(location + "/work-shifts")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
        RestAssured.given().when().get(shiftLocation).then()
            .statusCode(200)
            .body("startTime", equalTo("09:00"))
            .body("workedHours", equalTo(8.0f))
            .body("daytimeHours", equalTo(8.0f))
            .body("nighttimeHours", equalTo(0.0f));

        RestAssured.given().when().delete(location).then().statusCode(204);
        RestAssured.given().when().get(shiftLocation).then().statusCode(404);
    }

    @Test
    void shouldBulkCreateAtomicallyAndReturnWeeklySummary() {
        String suffix = "%08d".formatted(
            Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000)
        );
        String employeeLocation = RestAssured.given()
            .contentType("application/json")
            .body("""
                {
                  "documentType": "CC",
                  "documentNumber": "%s",
                  "firstName": "Ana",
                  "lastName": "Diaz",
                  "email": "bulk.%s@example.com",
                  "position": "Analyst",
                  "hireDate": "%s"
                }
                """.formatted(suffix, suffix, LocalDate.now().minusDays(1)))
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

        String bulkPath = employeeLocation + "/work-shifts/bulk";
        String body = """
            {
              "from": "2026-11-02",
              "to": "2026-11-03",
              "weekdays": ["MONDAY", "TUESDAY"],
              "startTime": "09:00",
              "endTime": "13:00"
            }
            """;
        String listLocation = RestAssured.given()
            .contentType("application/json")
            .body(body)
            .when()
            .post(bulkPath)
            .then()
            .statusCode(201)
            .body("count", equalTo(2))
            .body("scheduledHours", equalTo(8.0f))
            .extract()
            .header("Location");

        RestAssured.given().when().get(listLocation).then()
            .statusCode(200)
            .body("shifts", hasSize(2))
            .body("totalWorkedHours", equalTo(8.0f))
            .body("weeklySummaries", hasSize(1))
            .body("weeklySummaries[0].scheduledHours", equalTo(8.0f))
            .body("weeklySummaries[0].ordinaryLimitHours", equalTo(42.0f))
            .body("weeklySummaries[0].overtimeHours", equalTo(0.0f))
            .body("weeklySummaries[0].remainingOrdinaryHours", equalTo(34.0f));

        RestAssured.given()
            .contentType("application/json")
            .body(body)
            .when()
            .post(bulkPath)
            .then()
            .statusCode(409)
            .contentType("application/problem+json")
            .body("detail", containsString("2026-11-02"))
            .body("detail", containsString("2026-11-03"));

        RestAssured.given().when().get(listLocation).then()
            .statusCode(200)
            .body("shifts", hasSize(2));
        RestAssured.given().when().delete(employeeLocation).then().statusCode(204);
    }

    @Test
    void shouldRejectInvalidAndDuplicateEmployeeCreation() {
        RestAssured.given()
            .contentType("application/json")
            .body("""
                {
                  "documentType": "CC",
                  "documentNumber": "12",
                  "firstName": "Ana",
                  "lastName": "Diaz",
                  "email": "ana@invalid",
                  "position": "Analyst",
                  "hireDate": "2020-01-01"
                }
                """)
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("title", equalTo("Error de validación"));

        RestAssured.given()
            .contentType("application/json")
            .body("""
                {
                  "documentType": "XX",
                  "documentNumber": "123456789",
                  "firstName": "Ana",
                  "lastName": "Diaz",
                  "email": "ana.example@example.com",
                  "position": "Analyst",
                  "hireDate": "2020-01-01"
                }
                """)
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("title", equalTo("Solicitud inválida"))
            .body("detail", equalTo("El tipo de documento debe ser CC, CE o PA."));

        String documentNumber = uniqueSuffix();
        String location = RestAssured.given().contentType("application/json")
            .body(createEmployeeBody(documentNumber, "dup"))
            .when().post("/api/v1/employees")
            .then().statusCode(201).extract().header("Location");
        try {
            RestAssured.given()
                .contentType("application/json")
                .body(createEmployeeBody(documentNumber, "dup2"))
                .when()
                .post("/api/v1/employees")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("title", equalTo("Conflicto"))
                .body("detail", equalTo("Ya existe un empleado con ese documento."));
        } finally {
            RestAssured.given().when().delete(location).then().statusCode(204);
        }
    }

    @Test
    void shouldRejectDuplicateEmailWithDifferentDocument() {
        String suffix = uniqueSuffix();
        RestAssured.given().contentType("application/json")
            .body(createEmployeeBody(suffix, "dup." + suffix))
            .when().post("/api/v1/employees").then().statusCode(201);
        try {
            RestAssured.given()
                .contentType("application/json")
                .body("""
                    {
                      "documentType": "CE",
                      "documentNumber": "%s",
                      "firstName": "Bea",
                      "lastName": "Lopez",
                      "email": "dup.%s@example.com",
                      "position": "Analyst",
                      "hireDate": "2020-01-01"
                    }
                    """.formatted(suffix, suffix))
                .when()
                .post("/api/v1/employees")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("detail", equalTo("Ya existe un empleado con ese correo."));
        } finally {
            RestAssured.given().when()
                .delete("/api/v1/employees/CC/" + suffix).then().statusCode(204);
        }
    }

    @Test
    void shouldPaginateEmployeesAndRejectInvalidPagination() {
        RestAssured.given().when().get("/api/v1/employees?page=0&size=5").then()
            .statusCode(200)
            .body("page", equalTo(0))
            .body("size", equalTo(5));

        RestAssured.given().when().get("/api/v1/employees?page=-1&size=10").then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("title", equalTo("Solicitud inválida"))
            .body("detail", equalTo("La página o el tamaño solicitado no son válidos."));

        RestAssured.given().when().get("/api/v1/employees?page=0&size=101").then()
            .statusCode(400)
            .contentType("application/problem+json")
            .body("title", equalTo("Solicitud inválida"));
    }

    @Test
    void shouldReturnErrorsOnGetUpdateAndDeleteEmployee() {
        String suffix = uniqueSuffix();
        String location = RestAssured.given()
            .contentType("application/json")
            .body(createEmployeeBody(suffix, "ops." + suffix))
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
        String otherLocation = RestAssured.given().contentType("application/json")
            .body(createEmployeeBody(uniqueSuffix(), "other." + suffix))
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");

        try {
            RestAssured.given().when().get("/api/v1/employees/XX/123456789").then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("El tipo de documento debe ser CC, CE o PA."));

            RestAssured.given().when().get("/api/v1/employees/CC/88888888").then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("No encontrado"))
                .body("detail", equalTo("No se encontró el empleado solicitado."));

            RestAssured.given()
                .contentType("application/json")
                .body("{}")
                .when()
                .put(location)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("Debe enviar al menos un campo a actualizar."));

            RestAssured.given()
                .contentType("application/json")
                .body("{\"position\":\"Senior Analyst\",\"hireDate\":\"2010-01-01\"}")
                .when()
                .put(location)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("La fecha de contratación no se puede modificar."));

            RestAssured.given()
                .contentType("application/json")
                .body("{\"position\":\"Senior Analyst\"}")
                .when()
                .put(location)
                .then()
                .statusCode(200)
                .body("position", equalTo("Senior Analyst"))
                .body("documentNumber", equalTo(suffix))
                .body("email", equalTo("ops." + suffix + "@example.com"));

            RestAssured.given()
                .contentType("application/json")
                .body("{\"email\":\"other." + suffix + "@example.com\"}")
                .when()
                .put(location)
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("detail", equalTo("Ya existe un empleado con ese correo."));

            RestAssured.given()
                .contentType("application/json")
                .body("{\"position\":\"Lead\"}")
                .when()
                .put("/api/v1/employees/CC/88888888")
                .then()
                .statusCode(404);

            RestAssured.given().when().delete("/api/v1/employees/CC/88888888").then()
                .statusCode(404)
                .contentType("application/problem+json");
        } finally {
            RestAssured.given().when().delete(location).then().statusCode(204);
            RestAssured.given().when().delete(otherLocation);
        }
    }

    private static String uniqueSuffix() {
        return "%08d".formatted(Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000));
    }

    private static String createEmployeeBody(String documentNumber, String emailLocalPart) {
        return """
            {
              "documentType": "CC",
              "documentNumber": "%s",
              "firstName": "Ana",
              "lastName": "Diaz",
              "email": "%s@example.com",
              "position": "Analyst",
              "hireDate": "%s"
            }
            """.formatted(documentNumber, emailLocalPart, LocalDate.now().minusDays(1));
    }
}
