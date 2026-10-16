# Memoria Operativa - API de Empleados y Jornadas

## Contexto y decisiones
- Proyecto Java 21, Quarkus 3.8.5, Maven Wrapper 3.9.9, PostgreSQL/Flyway, JUnit 5/Mockito y JaCoCo.
- Clean Architecture: `domain <- application <- infrastructure`; domain/application no importan Jakarta, Quarkus ni Jackson.
- No agregar dependencias no aprobadas. No usar Lombok, MapStruct ni generadores.
- La API no tiene frontend propio; Swagger UI es la interfaz de demostracion.
- Identidad publica y de dominio del empleado: `(documentType, documentNumber)`, con tipos CC/CE/PA. UUID queda como PK interna de empleado. El UUID de jornada sigue siendo publico.
- `hireDate` y documento son inmutables despues de crear al empleado. El PUT del empleado es **parcial**: `firstName`, `lastName`, `email` y `position` son opcionales (se actualizan solo los enviados, omitido conserva el valor); body sin campos → 400; `hireDate` omitible (si se envia y difiere → 400). Decisión confirmada por el usuario 2026-10-16.
- Jornadas colombianas: calendario legal, horas diurnas/nocturnas por vigencia (`DayNightPolicy`), cruces de medianoche, solapes por intervalo real y control semanal por semana ISO con limites por vigencia (`WeeklyWorkloadPolicy`): 48/47/46/44/42 h segun Ley 2101 de 2021; el exceso sobre el limite es `overtimeHours` y solo 409 si supera 12 h extra (art. 22 Ley 50/1990 mod. Ley 2466/2025). No se valida el tope diario de 2 h extra (supuesto confirmado).
- El GET de jornadas incorpora totales y `weeklySummaries`; la carga bulk valida todo antes de persistir y usa `saveAll` transaccional.
- El README de `employees-api` es la documentacion de entrega; esta memoria es solo seguimiento operativo.

## Implementacion completada en esta reestructuracion
- Dominio: `Document`, `DocumentType`, `ColombianHolidayCalendar`, `DayType`, `ShiftType`, `WeeklyWorkloadPolicy` y `WeeklyHoursExceededException`.
- `WorkShift` calcula horas trabajadas, diurnas/nocturnas, clasificacion, fecha de fin y duracion dentro de semana ISO; admite cruce de medianoche y rechaza horas iguales.
- Empleados: casos de uso y respuestas operan por documento; PUT preserva documento y `hireDate`.
- Persistencia: entidades y mappers adaptados a la clave natural; `work_shifts` referencia `(employee_document_type, employee_document_number)` por FK compuesta y cascada.
- Eliminacion de empleado ahora elimina una sola entidad y confia en `ON DELETE CASCADE`; evita borrado manual de jornadas en transacciones separadas.
- REST: rutas de empleado/jornadas por documento, DTOs enriquecidos, bulk, errores `problem+json` y once operaciones OpenAPI.
- La carga bulk soporta rango maximo de 92 dias, weekdays omitido/vacio/cero fechas, deteccion todo-o-nada de solapes y limite semanal, fechas conflictivas en el 409 y guardado atomico.
- El resumen semanal se calcula desde las jornadas de semanas ISO completas; turnos que cruzan el lunes se distribuyen por intervalo real.
- V3/V4 creadas; V4 agrega jornadas partidas lunes-viernes (40 h), turno nocturno y festivo sin exceder el limite semanal.
- `employees-api/README.md` actualizado con contrato de 11 operaciones, ejemplos, reglas colombianas, diagramas, decisiones y las cuatro correcciones del usuario en la tabla de IA.
- Docker Compose usa variables parametrizadas para nombre de base de datos y credenciales de desarrollo.

## Redespliegue local
- Compose usa `employees_api_v2`; Flyway aplico V1–V5. Se eliminó la base `employees_db` por solicitud explícita tras validar la nueva; el volumen PostgreSQL quedó conservado y la base activa tiene 3 empleados y 18 jornadas semilla (V5 agrega la semana 2026-W42 con 46 h → 4 h extra).

## Estado verificado post-Frente 3 (2026-10-16)
- **Frente 3 implementado y verificado** en cuatro incrementos, cada uno con suite verde: 3.1 vigencias en dominio, 3.2 dominio/aplicacion con criterio de extra, 3.3 contrato REST, 3.4 semilla V5 + verify + README.
- Dominio: `WeeklyWorkloadPolicy` con tabla de vigencias 48/47/46/44/42 (limite de la semana ISO segun su domingo, criterio conservador), `ordinaryLimitHours`/`overtimeHours`/`remainingOrdinaryHours`, tope 12 h extra en `WeeklyHoursExceededException` (detalle con semana, horas programadas y maximo permitido); `DayNightPolicy` con ventana por fecha de inicio (diurna 06:00–21:00 hasta 2025-12-24; 06:00–19:00 desde 2025-12-25) y `WorkShift` ya no tiene constantes de ventana.
- Aplicacion: `WeeklyWorkloadSummaryResult` trae weekStart, scheduledHours, ordinaryLimitHours, overtimeHours y remainingOrdinaryHours calculados por `ListEmployeeWorkShifts`; `CreateWorkShift`/`UpdateWorkShift`/`BulkCreateWorkShifts` usan el criterio nuevo (409 solo por exceso del tope de extra).
- REST: `WorkShiftResponse` sin documentType/documentNumber, orden id/date/startTime/endTime/dayType/shiftType/horas; duraciones `BigDecimal` escala 2 `HALF_UP` via `WorkShiftMapper.toHours`; requests y responses `HH:mm` con `@JsonFormat`; listado sin totalDaytimeHours/totalNighttimeHours (solo `totalWorkedHours`); `WeeklySummaryResponse` con `week/startDate/endDate/scheduledHours/ordinaryLimitHours/overtimeHours/remainingOrdinaryHours`; bulk con `scheduledHours` decimal; `@Schema` y ejemplos actualizados (incluye cruce de medianoche).
- Semillas: `V5__overtime_seed_data.sql` agrega el sabado 2026-10-17 06:00–12:00 para CC/10000001 → 2026-W42 con 46 h → 4 h extra (V4 no se toco; Flyway aplico V5 en `employees_api_v2`).
- Verificacion integral: `verify` con `TEST_DB_DEV_SERVICES=false` y PostgreSQL de Compose: **69 unitarias + 2 IT, cero fallos**; JaCoCo **95.62% (896/937)** — domain 93.09% (337/362), application 97.75% (261/267), infrastructure 96.75% (298/308).
- README actualizado: tabla de vigencias con fuentes (Ley 2101/2021, Ley 2466/2025, Ley 50/1990 art. 22), criterios de la semana (domingo) y ventana (fecha de inicio), aviso de no-asesoria-legal, curl en `HH:mm`, ejemplo de respuesta con `overtimeHours`, decisiones nuevas y fila (5) de la tabla de IA.

## Plan Frente 3 aprobado (2026-10-16)
- Supuestos confirmados por el usuario: (a) superar el limite ordinario se **informa** como horas extra (no bloquea); solo 409 si las extra superan **12 h** por semana. (b) No se valida el tope diario de 2 h extra.
- Semilla de la semana con extra en `V5__overtime_seed_data.sql` **nueva migracion**, porque V4 ya fue aplicada en `employees_api_v2` y editarla romperia el checksum de Flyway. Ejemplo objetivo: 46 h programadas en 2026 → `ordinaryLimitHours` 42.00, `overtimeHours` 4.00, `remainingOrdinaryHours` 0.00.
- Incrementos con pruebas en verde por paso: **3.1** `WeeklyWorkloadPolicy` por vigencias (48/47/46/44/42; limite de la semana ISO segun su domingo) + `DayNightPolicy` por fecha (diurna hasta 2025-12-24 06:00–21:00; desde 2025-12-25 06:00–19:00) con tests en fechas frontera; **3.2** `WorkShift` usa la policy, resumen con limite/extra/restante calculado en dominio (sale del recurso), `WeeklyHoursExceededException` y bulk con el criterio nuevo; **3.3** REST: horas decimales escala 2 `HALF_UP`, `HH:mm`, sin documento por jornada ni totales diurno/nocturno, `weeklySummaries` renombrado + `overtimeHours`, OpenAPI con `@Schema` y ejemplos (bulk y cruce de medianoche); **3.4** V5, `verify` integral, README (tabla de vigencias, fuentes, aviso no-asesoria-legal, curl nuevo, fila (5) de IA) y demo Docker con `compose down` al final.
- Alcance: solo Frente 3 en esta ronda; los IT HTTP de los once endpoints y sus errores quedan para una ronda posterior.

## Migracion de datos legados
- La base anterior no guardaba tipo de documento. V3 transforma las tres semillas conocidas a documentos numericos CC y asigna CC como backfill inicial.
- El usuario eligio **mapeo manual explicito** para documentos alfanumericos legados no reconocidos; no se debe inferir PA u otro tipo automaticamente.
- V3 incluye un guard que aborta si quedan documentos que no cumplen el formato numerico CC/CE. La base legada `employees_db` se eliminó por solicitud explícita al redesplegar sobre `employees_api_v2`; para importar otra base legada, confirmar los tipos reales y mapearlos antes de V3. El volumen PostgreSQL no se eliminó.
- La ejecucion contra la base de desarrollo existente fallo intencionalmente en este guard y la migracion PostgreSQL transaccional fue revertida. `verify` se corrio despues contra una base aislada temporal de PostgreSQL de Compose, vacia; esa base temporal fue eliminada tras las pruebas.

## Verificacion actual
- **PUT parcial de empleados ok (2026-10-16)**: `UpdateEmployeeRequest` con campos opcionales, `Employee.update` omite nulos y valida solo los presentes, `UpdateEmployee` exige >=1 campo, unicidad de email solo si se envia y hireDate solo si viene; `verify` verde con **74 unitarias + 2 IT**.
- **Demo en vivo del PUT parcial (2026-10-16)**: imagen redesplegada (`docker compose up -d --build app`); contra `CC/10000001` el PUT con solo `position` devolvio 200 actualizando el campo y conservando el resto, la restitucion devolvio 200 y el body vacio devolvio 400 con "Debe enviar al menos un campo a actualizar." Nota operativa: desde PowerShell, los bodies JSON hacia `curl.exe` deben pasarse con `@archivo` porque el quoting nativo destruye las comillas dobles (provoca 400 falsos por JSON invalido).
- Cobertura JaCoCo post-PUT parcial: **95.83% (920/960)** — domain 93.65% (354/378), application 97.81% (268/274), infrastructure 96.75% (298/308).
- Post-Frente 3: `mvnw.cmd -f employees-api/pom.xml clean test/verify` con `TEST_DB_DEV_SERVICES=false` y PostgreSQL de Compose en `employees_api_v2`: **BUILD SUCCESS** (ver numero de pruebas actualizado arriba).
- `EmployeeResourceIT` verifica alta, consulta, listado y borrado por documento, crea/consulta una jornada con el contrato `HH:mm`/horas decimales y confirma 404 de la jornada tras borrar al empleado (cascada). El segundo IT verifica bulk, resumen semanal con `ordinaryLimitHours`/`overtimeHours` y rechazo 409 atomico (sin persistencia parcial) ante duplicados del bulk.
- Los IT no cubren todavia todos los once endpoints ni sus errores por HTTP (ronda posterior). Las pruebas de recursos REST existentes son unitarias.
- Dev Services sigue habilitado por defecto, pero no se valido debido a incompatibilidad del cliente Docker de Quarkus 3.8.5 con la API del daemon disponible.
- Docker: `docker compose up -d --build app` reconstruyo y desplego app + PostgreSQL con el volumen existente. Flyway aplico V5 al arrancar; health HTTP 200, OpenAPI HTTP 200 y Swagger UI redirige (302 -> UI). `GET /api/v1/employees/CC/10000001/work-shifts?from=2026-10-12&to=2026-10-18` devolvio la semana 2026-W42 con `scheduledHours 46.00`, `ordinaryLimitHours 42.00`, `overtimeHours 4.00`, `remainingOrdinaryHours 0.00`, duraciones decimales y `HH:mm` (el 2026-10-12 se clasifica FESTIVO por el calendario). Tras la demo se ejecuto `docker compose down` y se conservo el volumen.
- La base activa `employees_api_v2` tiene 3 empleados y 18 jornadas semilla tras V5. El volumen PostgreSQL se conserva entre redespliegues.
- No se encontraron imports Jakarta/Quarkus/Jackson en domain/application en la revision manual.

## Pendientes / cautelas
1. Si se importa otra copia de la base legada, confirmar manualmente el tipo real de los documentos alfanumericos y agregar sus mapeos explicitos antes de V3; no asumir que son pasaportes.
2. Cobertura HTTP de los once endpoints y sus errores: IT pendientes, ronda posterior al Frente 3.
3. Dev Services no tiene evidencia de ejecucion exitosa en este entorno.
4. El usuario debe confirmar que las respuestas de uso de IA en README reflejan su experiencia personal (incluida la fila (5) del Frente 3) y poder sustentar las decisiones ante el evaluador.
