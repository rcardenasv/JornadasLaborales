package com.assessment.employees.infrastructure.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration tests for the six work-shift endpoints, including HTTP error codes
 * (400/404/409) and their problem+json bodies.
 */
@QuarkusTest
class WorkShiftResourceIT {
    private static final String MONDAY = "2027-01-04";
    private static final String TUESDAY = "2027-01-05";
    private static final String WEDNESDAY = "2027-01-06";

    @Test
    void shouldRejectInvalidWorkShiftCreation() {
        String suffix = uniqueSuffix();
        String location = createEmployee(suffix, "shifts");

        try {
            RestAssured.given()
                .contentType("application/json")
                .body(shiftBody(MONDAY, "09:00", "09:00"))
                .when()
                .post(location + "/work-shifts")
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Solicitud inválida"))
                .body("detail", equalTo("La hora de fin debe ser distinta de la hora de inicio."));

            RestAssured.given()
                .contentType("application/json")
                .body(shiftBody(MONDAY, "09:00", "17:00"))
                .when()
                .post("/api/v1/employees/CC/88888888/work-shifts")
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("No encontrado"))
                .body("detail", equalTo("No se encontró el empleado solicitado."));

            RestAssured.given().contentType("application/json")
                .body(shiftBody(MONDAY, "09:00", "17:00"))
                .when().post(location + "/work-shifts")
                .then().statusCode(201);

            RestAssured.given()
                .contentType("application/json")
                .body(shiftBody(MONDAY, "16:00", "20:00"))
                .when()
                .post(location + "/work-shifts")
                .then()
                .statusCode(409)
                .contentType("application/problem+json")
                .body("detail", containsString("se solapa con otra jornada"))
                .body("detail", containsString("16:00"));
        } finally {
            RestAssured.given().when().delete(location).then().statusCode(204);
        }
    }

    @Test
    void shouldListWorkShiftsOrderedAndFilterByRange() {
        String suffix = uniqueSuffix();
        String location = createEmployee(suffix, "list");
        createShift(location, "2026-12-30", "08:00", "12:00");
        createShift(location, TUESDAY, "08:00", "13:00");
        createShift(location, MONDAY, "09:00", "17:00");

        try {
            RestAssured.given().when().get(location + "/work-shifts").then()
                .statusCode(200)
                .body("shifts", hasSize(3))
                .body("totalWorkedHours", equalTo(17.0f))
                .body("shifts[0].date", equalTo("2026-12-30"))
                .body("shifts[1].date", equalTo(MONDAY))
                .body("shifts[2].date", equalTo(TUESDAY))
                .body("weeklySummaries", hasSize(2));

            RestAssured.given().when().get(location + "/work-shifts?from=" + MONDAY).then()
                .statusCode(200)
                .body("shifts", hasSize(2))
                .body("totalWorkedHours", equalTo(13.0f));

            RestAssured.given()
                .when()
                .get(location + "/work-shifts?from=" + MONDAY + "&to=" + MONDAY)
                .then()
                .statusCode(200)
                .body("shifts", hasSize(1))
                .body("totalWorkedHours", equalTo(8.0f));

            RestAssured.given()
                .when()
                .get(location + "/work-shifts?from=" + TUESDAY + "&to=" + MONDAY)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Solicitud inválida"))
                .body("detail",
                    equalTo("La fecha desde no puede ser posterior a la fecha hasta."));

            RestAssured.given().when().get("/api/v1/employees/CC/88888888/work-shifts").then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("No encontrado"));
        } finally {
            delete(location);
        }
    }

    @Test
    void shouldGetUpdateAndDeleteSingleWorkShift() {
        String suffix = uniqueSuffix();
        String location = createEmployee(suffix, "single");
        String otherLocation = createEmployee(uniqueSuffix(), "single-other");
        String shiftLocation = createShift(location, WEDNESDAY, "09:00", "17:00");
        String shiftId = shiftLocation.substring(shiftLocation.lastIndexOf('/') + 1);

        try {
            RestAssured.given().when().get(shiftLocation).then()
                .statusCode(200)
                .body("date", equalTo(WEDNESDAY))
                .body("startTime", equalTo("09:00"))
                .body("endTime", equalTo("17:00"))
                .body("workedHours", equalTo(8.0f));

            RestAssured.given().when()
                .get("/api/v1/employees/CC/88888888/work-shifts/" + shiftId)
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("detail", equalTo("No se encontró la jornada laboral solicitada."));

            RestAssured.given().when()
                .get(otherLocation + "/work-shifts/" + shiftId)
                .then()
                .statusCode(404)
                .contentType("application/problem+json")
                .body("detail", equalTo("No se encontró la jornada laboral solicitada."));

            RestAssured.given()
                .contentType("application/json")
                .body(shiftBody(WEDNESDAY, "10:00", "18:00"))
                .when()
                .put(shiftLocation)
                .then()
                .statusCode(200)
                .body("startTime", equalTo("10:00"))
                .body("endTime", equalTo("18:00"));

            RestAssured.given().when().delete(shiftLocation).then().statusCode(204);
            RestAssured.given().when().get(shiftLocation).then().statusCode(404);
            RestAssured.given().when().delete(shiftLocation).then().statusCode(404);
        } finally {
            delete(location);
            delete(otherLocation);
        }
    }

    @Test
    void shouldRejectInvalidBulkRequests() {
        String suffix = uniqueSuffix();
        String location = createEmployee(suffix, "bulk");
        String bulkPath = location + "/work-shifts/bulk";

        try {
            RestAssured.given()
                .contentType("application/json")
                .body(bulkBody("2027-01-10", MONDAY, "[\"MONDAY\"]", "09:00", "17:00"))
                .when()
                .post(bulkPath)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("El rango de fechas es obligatorio y debe ser válido."));

            RestAssured.given()
                .contentType("application/json")
                .body(bulkBody("2027-01-01", "2027-04-30", "[\"MONDAY\"]", "09:00", "17:00"))
                .when()
                .post(bulkPath)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("El rango de carga no puede superar 92 días."));

            RestAssured.given()
                .contentType("application/json")
                .body(bulkBody(MONDAY, MONDAY, "[\"SUNDAY\"]", "09:00", "17:00"))
                .when()
                .post(bulkPath)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("detail", equalTo("El rango no genera jornadas para los días seleccionados."));

            RestAssured.given()
                .contentType("application/json")
                .body(bulkBody(MONDAY, TUESDAY, "[]", "09:00", "17:00"))
                .when()
                .post(bulkPath)
                .then()
                .statusCode(400)
                .contentType("application/problem+json")
                .body("title", equalTo("Error de validación"));

            RestAssured.given()
                .contentType("application/json")
                .body(bulkBody(MONDAY, MONDAY, "[\"MONDAY\"]", "09:00", "17:00"))
                .when()
                .post("/api/v1/employees/CC/88888888/work-shifts/bulk")
                .then()
                .statusCode(404)
                .contentType("application/problem+json");
        } finally {
            delete(location);
        }
    }

    private static String uniqueSuffix() {
        return "%08d".formatted(Math.floorMod(UUID.randomUUID().hashCode(), 100_000_000));
    }

    private static String createEmployee(String documentNumber, String emailLocalPart) {
        return RestAssured.given()
            .contentType("application/json")
            .body("""
                {
                  "documentType": "CC",
                  "documentNumber": "%s",
                  "firstName": "Ana",
                  "lastName": "Diaz",
                  "email": "%s.%s@example.com",
                  "position": "Analyst",
                  "hireDate": "%s"
                }
                """.formatted(documentNumber, emailLocalPart, documentNumber,
                LocalDate.now().minusDays(1)))
            .when()
            .post("/api/v1/employees")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    }

    private static String createShift(String employeeLocation, String date,
                                      String startTime, String endTime) {
        return RestAssured.given()
            .contentType("application/json")
            .body(shiftBody(date, startTime, endTime))
            .when()
            .post(employeeLocation + "/work-shifts")
            .then()
            .statusCode(201)
            .extract()
            .header("Location");
    }

    private static void delete(String employeeLocation) {
        RestAssured.given().when().delete(employeeLocation).then().statusCode(204);
    }

    private static String shiftBody(String date, String startTime, String endTime) {
        return """
            {"date":"%s","startTime":"%s","endTime":"%s"}
            """.formatted(date, startTime, endTime);
    }

    private static String bulkBody(String from, String to, String weekdaysArray,
                                   String startTime, String endTime) {
        return """
            {
              "from": "%s",
              "to": "%s",
              "weekdays": %s,
              "startTime": "%s",
              "endTime": "%s"
            }
            """.formatted(from, to, weekdaysArray, startTime, endTime);
    }
}
