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

## Plan Aprobado (2026-10-15): Jornadas Colombianas e Identidad por Documento

El usuario aprobo dos frentes de cambio. AGENTS.md (secciones 1, 4, 5, 7, 8 y 13) es la especificacion objetivo; el estado previo (UUID como identidad, sin cruce de medianoche, sin tope semanal) queda obsoleto al terminar la reestructuracion.

**Jornadas aterrizadas a Colombia:**
- Clasificacion calculada por el dominio: dayType (ORDINARIO/DOMINICAL/FESTIVO), shiftType (DIURNA/NOCTURNA/MIXTA), daytimeHours (06:00-21:00) y nighttimeHours (21:00-06:00).
- ColombianHolidayCalendar en dominio, Java puro, sin dependencias nuevas: fijos, Semana Santa, pascuales al lunes y Emiliani (Ley 904/2004).
- Turnos que cruzan medianoche: endTime < startTime = termina al dia siguiente; endTime == startTime -> 400. Solape por intervalo real con consultas extendidas un dia atras.
- Tope 42 h por semana ISO (Ley 2101, vigente desde 1-jul-2026), horas cortadas en lunes 00:00; exceder -> WeeklyHoursExceededException -> 409.
- POST .../work-shifts/bulk por empleado: from/to + weekdays + una franja; todo-o-nada (409 con fechas conflictivas, no crea nada); rango maximo 92 dias; weekdays vacio o cero jornadas -> 400.
- weeklySummaries visibles en el GET de jornadas (semanas ISO completas tocadas por el rango): scheduledHours, maxWeeklyHours=42h, remainingHours. Sin endpoint de resumen separado.
- Pausas no modeladas: almuerzo = dos jornadas; documentarlo en README.

**Identidad del empleado por documento:**
- Identidad de API y dominio = (documentType: CC|CE|PA, documentNumber). El UUID queda como PK tecnica interna, nunca expuesta.
- Inmutables tras creacion: tipo, numero de documento y hireDate (PUT con hireDate distinta -> 400). PUT actualiza firstName, lastName, email, position.
- Validacion: CC/CE solo digitos 3-20; PA alfanumerico 5-20; tipo case-insensitive normalizado. UNIQUE(document_type, document_number) y email unico -> 409 por duplicado.
- work_shifts referencia la clave natural (FK compuesta a employees(document_type, document_number), ON DELETE CASCADE); se elimina la columna employee_id UUID.
- Trade-offs aceptados (documentar en README): CE->CC exige alta nueva; documento en URLs/access logs; PUT no corrige digitacion del documento.

**Migraciones:** V3__employee_document_identifier.sql (document_type + backfill 'CC' + UNIQUE compuesta + rewire de la FK de work_shifts) y V4__colombian_work_shifts.sql (CHECK end_time <> start_time + semillas realistas <= 42 h: jornada partida L-V, nocturno 22:00-06:00, festivo). No tocar V1/V2.

**Correcciones del usuario a sugerencias de la IA (filas para la tabla de la pregunta 6 del README):**
1. IA sugirio UUID como identidad + endpoint de cruce por documento -> usuario decidio documento como identificador en toda la API.
2. IA sugirio endpoint de resumen semanal -> usuario decidio ver la carga semanal en el GET de jornadas existente.
3. IA sugirio hireDate actualizable -> usuario decidio inmutable tras creacion.
4. Jornadas no aterrizadas a horarios de Colombia -> reestructuracion con reglas colombianas (peticion del usuario).

**Orden de implementacion (7 incrementos, cada uno compila y pasa sus pruebas):**
1. Dominio: calendario de festivos + DayType/ShiftType + desglose de horas.
2. Dominio: cruce de medianoche + solape real + WeeklyWorkloadPolicy (42 h).
3. Dominio + application de empleados: Document como identidad, hireDate inmutable.
4. Persistencia: V3 + adaptadores con clave natural.
5. REST de empleados: rutas por documento + DTOs + OpenAPI.
6. Jornadas end-to-end: bulk + respuestas enriquecidas + weeklySummaries + V4; verify integral.
7. Docs: README (contrato, ER, curl, decisiones, filas de IA) + AGENTS.md/memory.md + demo Docker.

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
1. Ejecutar los 7 incrementos de la reestructuracion (ver plan aprobado); al terminar, repetir `mvnw.cmd verify` integral y registrar los numeros nuevos (pruebas y JaCoCo) en README y memory.md.
2. Actualizar README con el contrato de 11 endpoints, ejemplos curl (bulk y cruce de medianoche incluidos), diagramas, decisiones nuevas y las 4 filas de correcciones del usuario en la tabla de la pregunta 6.
3. Reescribir `EmployeeResourceIT` para las rutas por documento y agregar pruebas HTTP de integracion para jornadas y errores de los once endpoints si se exige el alcance completo de pruebas de `AGENTS.md`.
4. Dev Services permanece habilitado por defecto pero no fue validado en este entorno debido a la incompatibilidad Docker; la integracion registrada uso PostgreSQL Compose con `TEST_DB_DEV_SERVICES=false`.
5. El contenedor `employees-api` y PostgreSQL quedaron ejecutandose para la demostracion; detenerlos con `docker compose down` desde `employees-api` cuando el usuario termine. No borrar el volumen. Tras la reestructuracion, revalidar la demo (`docker compose up --build`) con las migraciones V3/V4.
6. El candidato debe revisar que las respuestas de IA reflejen fielmente su experiencia personal; el texto actual se limita deliberadamente a hechos observables de esta sesion.

