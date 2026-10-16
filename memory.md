# Memoria Operativa - API de Empleados y Jornadas

## Contexto
- API REST para CRUD de empleados y jornadas laborales, con PostgreSQL y Flyway.
- Stack acordado: Java 21, Quarkus, Maven, JUnit 5, Mockito y JaCoCo.
- La API no lleva front-end; Swagger UI es la interfaz para la demostracion.

## Reglas del Proyecto
- Clean Architecture: domain <- application <- infrastructure.
- Domain y application no importan Jakarta, Quarkus ni Jackson.
- No agregar dependencias no aprobadas; no usar Lombok ni generadores de mappers.
- Borrado fisico de empleados; jornadas con cascada en la base de datos.
- JaCoCo debe exigir al menos 80% de cobertura unitaria. Exclusiones permitidas: `infrastructure/config/**` y `infrastructure/persistence/entity/**`.
- README.md es la documentacion principal; memory.md mantiene el seguimiento de trabajo.

## Correcciones Aplicadas
- El listado de empleados pagina desde el repositorio y devuelve el conteo total.
- Las respuestas POST construyen `Location`; PUT usa los campos del body sin duplicar el ID de la ruta.
- Los IDs de jornadas son UUID de extremo a extremo y coinciden con el esquema/seed.
- Las escrituras JPA son transaccionales; los adaptadores usan inyeccion por constructor y `merge` para guardar.
- El filtro de jornadas acepta `from` o `to` por separado y valida fechas y rangos.
- La validacion de fecha de contratacion rechaza fechas futuras; los errores REST usan `application/problem+json` en espanol.
- Se corrigieron claves de Swagger, CORS, datasource y migraciones Flyway.
- Los updates de Employee y WorkShift validan antes de reemplazar estado.
- Maven Failsafe ejecuta pruebas con sufijo `IT`; JaCoCo excluye solo configuracion CDI y entidades JPA simples.
- `WorkShiftEntity.employeeId` y los parametros JPQL usan UUID, igual que la FK `work_shifts.employee_id`; asi se evita comparar `uuid` con `varchar` en PostgreSQL.
- El Maven Wrapper oficial esta generado en modo `only-script` y fijado a Maven 3.9.9 (`employees-api/mvnw.cmd`).
- Docker usa build multietapa y copia el artefacto fast-jar de Quarkus (`target/quarkus-app`).
- Las diez operaciones tienen resumen, codigos documentados y parametros OpenAPI; los DTOs de entrada incluyen ejemplos.
- Los avisos recientes en pruebas se corrigieron: `EmployeeResourceIT` reemplaza el matcher Hamcrest deprecated por aserciones JUnit; stubbings Mockito de varios valores (`TypedQuery`/`Stream`) se encadenaron para evitar arrays genericos de varargs. EmployeeJpaRepositoryTest (5) y WorkShiftJpaRepositoryTest (4) pasaron en ejecuciones focalizadas; `EmployeeResourceIT` se recompilo correctamente, pero no se ha vuelto a ejecutar desde ese cambio.
- Las seis respuestas de uso de IA en `employees-api/README.md` ya estan redactadas sin placeholders y acotadas a esta sesion; `AGENTS.md` refleja el stack real y distingue evidencia de pendientes.

## Estado y Verificaciones
- Wrapper probado: Maven 3.9.9, Java 21.
- `mvnw.cmd -f employees-api/pom.xml verify` con PostgreSQL Compose: BUILD SUCCESS; 39 pruebas unitarias y 1 de integracion, sin fallos.
- JaCoCo unitario: 96.82% (608/628 lineas); el proceso Failsafe no instrumenta los IT.
- `EmployeeResourceIT` valida Flyway, Hibernate y CRUD real contra PostgreSQL 15.
- La unica IT actual es `EmployeeResourceIT`: no cubre jornadas ni todos los codigos de error a traves de HTTP. Las pruebas unitarias de recursos usan casos de uso mockeados.
- `docker compose config` valida; `docker compose up --build -d` construyo y arranco app/PostgreSQL. `/q/health`, `/q/openapi` y `/q/swagger-ui` respondieron HTTP 200.
- OpenAPI publicado: 10 operaciones, 0 sin resumen, con respuestas HTTP y ejemplos en los esquemas de entrada.
- La revision de imports no encontro dependencias Jakarta, Quarkus o Jackson en `domain`/`application`.
- Dev Services queda habilitado por defecto. En este entorno su cliente Docker aislado de Quarkus 3.8.5 pide API 1.32 mientras Docker Desktop exige 1.40; se verifico el IT usando PostgreSQL externo con `TEST_DB_DEV_SERVICES=false`.

## Pendientes
1. Repetir `mvnw.cmd verify` tras las correcciones recientes a las pruebas; las clases afectadas pasaron individualmente, pero no se ha registrado un verify integral posterior.
2. Agregar pruebas HTTP de integración para jornadas y errores de los diez endpoints si se requiere cumplir completamente el alcance de pruebas de `AGENTS.md`.
3. Dev Services permanece habilitado por defecto pero no fue validado en este entorno debido a la incompatibilidad Docker; la integración registrada usó PostgreSQL Compose con `TEST_DB_DEV_SERVICES=false`.
4. El contenedor `employees-api` y PostgreSQL quedaron ejecutándose para la demostración; detenerlos con `docker compose down` desde `employees-api` cuando el usuario termine. No borrar el volumen.
5. El candidato debe revisar que las respuestas de IA reflejen fielmente su experiencia personal; el texto actual se limita deliberadamente a hechos observables de esta sesión.

