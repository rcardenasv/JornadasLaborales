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
}
