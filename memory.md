# Memoria Operativa - API de Empleados y Jornadas

## Contexto y decisiones
- Proyecto Java 21, Quarkus 3.8.5, Maven Wrapper 3.9.9, PostgreSQL/Flyway, JUnit 5/Mockito y JaCoCo.
- Clean Architecture: `domain <- application <- infrastructure`; domain/application no importan Jakarta, Quarkus ni Jackson.
- No agregar dependencias no aprobadas. No usar Lombok, MapStruct ni generadores.
- La API no tiene frontend propio; Swagger UI es la interfaz de demostracion.
- Identidad publica y de dominio del empleado: `(documentType, documentNumber)`, con tipos CC/CE/PA. UUID queda como PK interna de empleado. El UUID de jornada sigue siendo publico.
- `hireDate` y documento son inmutables despues de crear al empleado.
- Jornadas colombianas: calendario legal, horas diurnas/nocturnas, cruces de medianoche, solapes por intervalo real y control semanal por semana ISO (tope 42 h fijo en el codigo actual; el Frente 3 lo parametriza por vigencia y agrega horas extra con tope de 12 h/semana).
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
- Compose usa `employees_api_v2`; Flyway aplicó V1–V4. Se eliminó la base `employees_db` por solicitud explícita tras validar la nueva; el volumen PostgreSQL quedó conservado y la base activa tiene 3 empleados y 17 jornadas semilla.

## Estado verificado 2026-10-16 (auditoria previa al Frente 3)
- El contrato ya tiene once endpoints y el usuario confirmo **no hacer cambios de contrato**: el GET individual por `shiftId` se conserva (diferencia con el listado: el listado devuelve todas las jornadas filtradas por fecha con `totalWorkedHours` y `weeklySummaries`; el GET individual devuelve una jornada por UUID sin agregados; retirarlo romperia la simetria con PUT/DELETE y evaluo el usuario la simplificacion descartada).
- **Frente 3 verificado en 0 %** en el codigo: limite semanal `MAX_WEEKLY_HOURS = 42` fijo en `WeeklyWorkloadPolicy` (rechaza por encima sin concepto de horas extra; el mensaje hardcodea "42 horas"); ventana diurna/nocturna hardcodeada 06:00–21:00 como constantes en `WorkShift` (no existe `DayNightPolicy`); no existe `overtimeHours` en ninguna capa; resumen semanal tiene solo `weekStart`/`scheduledHours` y el REST computa `maxWeeklyHours`/`remainingHours` en el recurso; duraciones serializadas en segundos (`28800.0f` en el IT) y sin `HH:mm`; el listado aun trae `totalDaytimeHours`/`totalNighttimeHours` y cada jornada repite `documentType`/`documentNumber`; no existe `V5__overtime_seed_data.sql`.
- Los tests actuales cubren reparto domingo→lunes entre semanas ISO y rechazo del tope fijo, pero ninguna fecha frontera de vigencia (2023/2024/2025/2026-07-15) ni cambio de ventana (2025-12-24/25) ni semantica de extra.

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
- `mvnw.cmd -f employees-api/pom.xml clean verify` con `TEST_DB_DEV_SERVICES=false` y PostgreSQL de Compose en una base aislada: **BUILD SUCCESS**, 59 unitarias + 2 IT, cero fallos.
- JaCoCo unitario: **95.36% (864/906 lineas)**. Por capa: domain 92.15%, application 97.74%, infrastructure 96.77%. Branch: domain 71.43%, application 84.38%, infrastructure 77.78%. El check del build aplica el umbral de 80% de lineas al bundle.
- `EmployeeResourceIT` verifica alta, consulta, listado y borrado por documento, crea/consulta una jornada y confirma 404 de la jornada tras borrar al empleado (cascada). El segundo IT verifica bulk, resumen semanal y rechazo 409 atomico (sin persistencia parcial) ante solape.
- Los IT no cubren todavia todos los once endpoints ni sus errores por HTTP. Las pruebas de recursos REST existentes son unitarias.
- Dev Services sigue habilitado por defecto, pero no se valido debido a incompatibilidad del cliente Docker de Quarkus 3.8.5 con la API del daemon disponible.
- `docker compose config --quiet` y `docker compose build app` pasaron. Se redesplegaron app y PostgreSQL con el volumen existente; la API apunta a `employees_api_v2`, Flyway está en V4, health HTTP 200, Swagger UI HTTP 200 y OpenAPI publica once operaciones.
- La base activa tiene 3 empleados y 17 jornadas semilla. La base previa `employees_db` se eliminó tras validar el nuevo despliegue, por instrucción del usuario; el volumen que contiene `employees_api_v2` se conserva.
- No se encontraron imports Jakarta/Quarkus/Jackson en domain/application en la revision manual.

## Pendientes / cautelas
1. Ejecutar el Frente 3 (3.1–3.4) según el plan aprobado de arriba; refrescar los numeros de `verify` y JaCoCo en este archivo y el README al cerrarlo.
2. El arbol tiene una prueba unitaria mas (60) que el ultimo `verify` registrado (59); los numeros finales salen del verify del Frente 3.
3. Si se importa otra copia de la base legada, confirmar manualmente el tipo real de los documentos alfanumericos y agregar sus mapeos explicitos antes de V3; no asumir que son pasaportes.
4. Cobertura HTTP de los once endpoints y sus errores: IT pendientes, ronda posterior al Frente 3.
5. Dev Services no tiene evidencia de ejecucion exitosa en este entorno.
6. El usuario debe confirmar que las respuestas de uso de IA en README reflejan su experiencia personal; falta la fila (5) del Frente 3 en la tabla de la pregunta 6.
