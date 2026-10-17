# AGENTS.md — Desafío 1: API de Empleados y Jornadas Laborales

Este archivo define el contexto, el estado implementado y los lineamientos que el agente de desarrollo debe seguir al asistir esta solución. **Léelo completo antes de escribir código** y consúltalo cada vez que tengas dudas de diseño.

---

## 1. Contexto del proyecto

Prueba técnica para el cargo **Especialista en APIs e Integraciones**. Este repositorio cubre únicamente el **Desafío 1**:

> Diseñar y desarrollar una API que implemente el CRUD de empleados y un endpoint para consultar sus jornadas laborales, aplicando **Clean Code** y **Clean Architecture** (DDD opcional, con buena separación de responsabilidades y bajo acoplamiento).

Criterios que pesan en la evaluación:

1. Buenas prácticas y principios de arquitectura evidentes en el código.
2. **No es un proyecto grande**: debe ser claro, acotado y fácil de **sustentar presencialmente**. Cada decisión técnica debe poder explicarse en pocas frases.
3. Entregables: diagrama de diseño, código fuente, y (opcional) contenedor Docker que permita ejecutar y visualizar la funcionalidad.
4. Como se usa IA, debe existir documentación que responda las 6 preguntas de la sección 12. **Tanto los diagramas como esas respuestas viven en el `README.md`** (no se crean archivos separados en `docs/`).

**Principio rector: simplicidad con intención.** Prefiere la solución más simple que respete la arquitectura. Si algo no aporta a los criterios anteriores, no lo agregues (sin sobreingeniería: nada de CQRS, event sourcing, mensajería, microservicios, etc.).

### Estado implementado (auditado 2026-10-16)

- **Frentes 1 y 2 de la reestructuración (plan 2026-10-15): implementados y verificados.** La API expone once endpoints: CRUD de empleados por documento, CRUD de jornadas, `bulk` y listado con `weeklySummaries`. Stack fijado: Java 21, Quarkus 3.8.5, Maven Wrapper 3.9.9 y `quarkus-resteasy-reactive-jackson`. No describas esta versión como la LTS más reciente ni cambies el stack sin revisar compatibilidad y ejecutar la suite.
- Persistencia: migraciones V1–V5 aplicadas (identidad por documento con FK compuesta y cascada, `CHECK (end_time <> start_time)`, semillas y semilla de horas extra en V5). La base activa `employees_api_v2` tiene 3 empleados y 18 jornadas semilla; Flyway está en V5.
- Último `verify` registrado (ronda de remediación de code review, 2026-10-16): **BUILD SUCCESS con 75 unitarias + 2 IT** y JaCoCo verificado ≥ 80 %; **96,87 % de líneas (867/895)**; por capa: domain 95,52 % (320/335), application 98,43 % (250/254), infrastructure 97,06 % (297/306). Commits de la ronda: `38b631c`, `390caa5`, `2e16df1`, `1721877`, `ef826ca`, `29c9e5b`.
- **Ronda de subsanación del code review (2026-10-16, clean code & arquitectura): implementada y verificada.** Cambios sin contrato ni esquema (único cambio externo pactado: `Problem` sin `timestamp`, con `@JsonInclude(NON_NULL)`): `InvalidWorkShiftException` y mensajes de dominio en español; `ListEmployeeWorkShifts` con `from > to` → `InvalidDateRangeException`; puerto `EmployeeRepository` sin `findByDocumentNumber` ni defaults muertos; eliminados `DocumentNumber`, el overload `Employee.create(String...)` y los records `Date`/`Time`; `WorkShift` inmutable total (`with(date,start,end)`) y `UpdateWorkShift` persiste la nueva instancia; `Employee.create` recibe `currentDate` y `CreateEmployee` un `Clock` (bean en `UseCaseConfig`), con helper `requireText` y constantes nombradas (offsets pascuales 43/64/71 documentados como múltiplos de semana + 1, test de lunes en años de referencia); `totalWorkedHours` calculado en `WorkShiftListResult`, semana ISO (`2026-W42`) y `parseIsoDate` en `WorkShiftMapper`, `WorkShiftResponse` con `dayType`/`shiftType` como `String`; guardas compartidas `WorkShift.rejectOverlap`/`weekStartOf`; commands de aplicación (empleados y jornadas) cargan `Document` en lugar de los pares de strings. `DuplicateEmployeeException` con un solo discriminador.
- Los 2 IT están en `EmployeeResourceIT`: flujo completo de empleado por documento (alta, consulta, listado, borrado), creación/consulta de una jornada (contrato `HH:mm` y horas decimales) y 404 de la jornada tras borrar al empleado (cascada); el segundo IT cubre bulk, resumen semanal con `ordinaryLimitHours`/`overtimeHours` y rechazo 409 del bulk repetido. Aún no hay cobertura HTTP de todos los once endpoints ni de sus errores (pendiente heredado).
- **Frente 3 (2026-10-16): implementado y verificado** en cuatro incrementos: `WeeklyWorkloadPolicy` con vigencias 48/47/46/44/42 h (límite de la semana ISO según su domingo) y tope de 12 h extra; `DayNightPolicy` con ventana diurna/nocturna por fecha (06:00–21:00 hasta 2025-12-24; 06:00–19:00 desde 2025-12-25); `WorkShift` ya no hardcodea la ventana; resumen semanal con `ordinaryLimitHours`/`overtimeHours`/`remainingOrdinaryHours` calculado en dominio (`ListEmployeeWorkShifts`); REST con horas decimales escala 2 `HALF_UP` (`WorkShiftMapper.toHours`), `HH:mm` vía `@JsonFormat`, sin `documentType`/`documentNumber` por jornada ni `totalDaytimeHours`/`totalNighttimeHours`; `V5__overtime_seed_data.sql` agrega el sábado 2026-10-17 06:00–12:00 para `CC/10000001` (2026-W42 = 46 h → 4 h extra). Los tests cubren las fechas frontera de 4.1 y el criterio de extra.
- Dev Services está configurado por defecto, pero no se pudo validar en el entorno registrado por incompatibilidad de versión de API Docker. Los IT se ejecutaron con PostgreSQL de Compose mediante `TEST_DB_DEV_SERVICES=false`; no afirmes que Dev Services pasó.
- Docker Compose construido y redesplegado sobre `employees_api_v2` (post-Frente 3): `health` y OpenAPI devolvieron HTTP 200, Swagger UI redirige a la UI y Flyway aplicó V5 al arrancar; `GET .../work-shifts?from=2026-10-12&to=2026-10-18` demostró la semana 2026-W42 con 46.00/42.00/4.00/0.00. Tras la demo se ejecutó `docker compose down` conservando el volumen. En la ronda de remediación la imagen se reconstruyó con el código final (`docker compose up -d --build app`): health/OpenAPI/Swagger `200` y la misma semana extra verificada en vivo; los contenedores quedaron en ejecución con el volumen conservado.
- Las seis respuestas de uso de IA están completas en `employees-api/README.md` sin placeholders, con cinco filas registradas de correcciones del usuario (identidad por documento, resumen semanal en el GET, `hireDate` inmutable, aterrizaje a Colombia, vigencias normativas/horas extra/horas decimales del Frente 3).

### Plan aprobado (2026-10-15): reestructuración — Frentes 1 y 2 completados

Los frentes 1 y 2 están **implementados, verificados con `verify` y desplegados** (ver audit de arriba). Las secciones 4, 5, 7 y 8 de este archivo son el contrato/modelo vigente y el sustento de la reestructuración ya realizada; no mezcles reglas viejas (identidad por UUID, prohibición de cruce de medianoche, ausencia de tope semanal) con las nuevas. El Frente 3, descrito a continuación, es el trabajo **pendiente**.

**Frente 1 — Jornadas aterrizadas a los horarios laborales de Colombia:**
- Clasificación **calculada por el dominio**: `dayType` (ORDINARIO/DOMINICAL/FESTIVO), `shiftType` (DIURNA/NOCTURNA/MIXTA), `daytimeHours` y `nighttimeHours`, con la ventana diurna/nocturna **según la fecha de la jornada** (Frente 3 y 4.1: 06:00–21:00 hasta 2025-12-24; 06:00–19:00 desde 2025-12-25, Ley 2466 de 2025).
- `ColombianHolidayCalendar` en dominio: festivos por año calculados algorítmicamente en Java puro (fijos, Semana Santa, pascuales al lunes y Emiliani — Ley 904/2004). Sin dependencias nuevas.
- Turnos que cruzan la medianoche permitidos: `endTime < startTime` significa fin al día siguiente; `endTime == startTime` → 400. Solapamiento por intervalo real, con consultas extendidas un día hacia atrás.
- Control semanal por **semana ISO** con corte de horas en lunes 00:00. **Reemplazado por el Frente 3**: el límite ordinario depende de la fecha (Ley 2101 de 2021: 47/46/44/42 h; **42 h rige desde el 15-jul-2026**, no desde el 1-jul), el exceso se reporta como horas extra y solo superar el tope de 12 h extra semanales → `WeeklyHoursExceededException` → 409.
- `POST .../work-shifts/bulk` por empleado: rango `from`/`to` + `weekdays` + una sola franja horaria. **Todo-o-nada**: cualquier solape o tope roto → 409 con las fechas conflictivas en el `detail`, sin crear nada. Rango máximo 92 días; `weekdays` vacío o cero jornadas generadas → 400.
- La carga semanal se ve en el `GET .../work-shifts` existente (`weeklySummaries` por semana ISO completa tocada por el rango). **No** se crea un endpoint de resumen separado.
- Las pausas (almuerzo) no se modelan: se registran como dos jornadas sin solape (p. ej. 08:00–12:00 y 13:00–17:00).

**Frente 2 — Empleado identificado por su documento:**
- Identidad de API y dominio = `(documentType, documentNumber)` con `DocumentType` ∈ {CC, CE, PA}. El UUID sobrevive solo como **PK técnica interna** de `employees`, nunca expuesta en la API.
- Inmutables tras creación: tipo y número de documento, y `hireDate` (PUT con `hireDate` distinta → 400). PUT actualiza `firstName`, `lastName`, `email`, `position`.
- Validación por tipo: CC/CE solo dígitos (3–20); PA alfanumérico (5–20); tipo case-insensitive normalizado. Duplicado = combinación `(document_type, document_number)` o email → 409.
- `work_shifts` referencia al empleado por su **clave natural** (FK compuesta a `employees(document_type, document_number)`, `ON DELETE CASCADE`).
- Consecuencias aceptadas y a documentar en el README: CE→CC (naturalización) exige alta nueva; el documento aparece en URLs/access logs (los logs de aplicación ya excluyen datos personales); PUT no corrige errores de digitación del documento.

**Frente 3 — Ajuste normativo y de claridad en las respuestas de jornadas (2026-10-16):**

Motivo: la respuesta actual de `GET .../work-shifts` aplicaba **42 h para cualquier fecha** (p. ej. la semana 2024-W03, cuando regían 47 h), no distinguía las horas extra, tomaba 21:00 como inicio nocturno sin considerar la Ley 2466 de 2025, expresaba las duraciones en **segundos** y traía totales redundantes. Cambios a implementar:
- **Parámetros legales por vigencia (fecha)**, en dominio y Java puro: límite semanal ordinario (Ley 2101 de 2021: 48/47/46/44/42 h) y ventana diurna/nocturna (nocturno desde 21:00 hasta 2025-12-24; desde 19:00 a partir de 2025-12-25, Ley 2466 de 2025). Tablas y reglas de aplicación en 4.1.
- **Exceso sobre el límite ordinario = horas extra**, no error: se reporta en `overtimeHours`. Solo se rechaza (409) si las horas extra de la semana superan **12** (art. 22 de la Ley 50 de 1990, modificado por la Ley 2466 de 2025). Cambia la semántica de `WeeklyHoursExceededException`; conserva su nombre para no generar rotación de código.
- **Duraciones en horas decimales** con 2 decimales (p. ej. `8.00`, `7.50`) en lugar de segundos; `startTime`/`endTime` en formato `HH:mm`.
- **Se eliminan** `totalDaytimeHours` y `totalNighttimeHours` del listado (el desglose diurno/nocturno ya viene en cada jornada) y el `documentType`/`documentNumber` repetido en cada jornada (ya viaja en la URL).
- **`weeklySummaries` explícito**: `maxWeeklyHours` → `ordinaryLimitHours`, `remainingHours` → `remainingOrdinaryHours` y se agrega `overtimeHours`. Contrato objetivo en 5.2.
- **Supuestos por defecto (aplícalos salvo que el usuario indique lo contrario):** (a) superar el límite ordinario se informa como horas extra en lugar de bloquearse; (b) no se valida el tope diario de 2 h extra, porque la distribución diaria de las horas ordinarias es potestad del empleador; si el usuario prefiere el comportamiento anterior (409 al superar el límite ordinario) o quiere validar el tope diario, confirma el cambio antes de implementarlo.
- Sin cambios de esquema de base de datos. El README debe documentar la tabla de vigencias, las fuentes normativas, las simplificaciones adoptadas y que **no constituye asesoría legal**.

**Decisiones confirmadas por el usuario (2026-10-16):**
- Se aplican los dos **supuestos por defecto** (a) y (b) descritos arriba: las extra se informan en lugar de bloquear, y no se valida el tope diario de 2 h extra.
- El contrato de **11 endpoints no cambia**: el GET individual por `shiftId` se conserva (complemento del PUT/DELETE por ID; evaluar retirarlo devolvería a 10 endpoints, decisión descartada).
- La semilla de la semana con horas extra va en **`V5__overtime_seed_data.sql`**: V4 ya fue aplicada en la base activa `employees_api_v2` (Flyway en V4), por lo que **no se debe editarla** — rompería el checksum de Flyway.
- Los IT heredados (HTTP de los once endpoints y sus errores) quedan para una **ronda posterior**; este Frente 3 solo ejecuta 3.1–3.4 de la sección 13.

**Alcance decidido (tras la reestructuración):**
- **CRUD de empleados** identificados por `(documentType, documentNumber)` (Create, Retrieve, Update, Delete reales), con tipo/número de documento y `hireDate` inmutables tras creación.
- **CRUD de jornadas laborales de un empleado** aterrizadas a Colombia: agregar, consultar (una y listado con filtro por fechas, clasificación y resumen semanal), actualizar y eliminar; **más carga agrupada** (`bulk`) de una misma franja horaria sobre varios días.
- Reglas colombianas **parametrizadas por fecha** (Frente 3): ventana diurna/nocturna, dominicales/festivos, límite semanal ordinario y horas extra (tope 12 h/semana). **Sin cálculo monetario** (recargos/salario), sin autenticación.
- **No hay front-end**: es una API REST. La interfaz para interactuar y demostrar la funcionalidad es **Swagger UI** (ver 5.1). No amplíes el alcance más allá de esto.

---

## 2. Stack tecnológico (decidido)

| Aspecto | Decisión |
|---|---|
| Lenguaje | Java 21 (LTS) |
| Framework | Quarkus 3.8.5 (versión implementada en `pom.xml`; comprobar compatibilidad antes de actualizar) |
| Build | Maven 3.9.9 mediante Maven Wrapper (`mvnw.cmd` en el entorno Windows usado) |
| API | RESTEasy Reactive + Jackson mediante `quarkus-resteasy-reactive-jackson` (coordenada actualmente usada) |
| Persistencia | **PostgreSQL** + Hibernate ORM (`quarkus-hibernate-orm`) + Flyway para migraciones |
| Validación | `quarkus-hibernate-validator` (solo en la capa REST) |
| Documentación API | `quarkus-smallrye-openapi` (Swagger UI habilitado) |
| Salud | `quarkus-smallrye-health` |
| Pruebas unitarias | JUnit 5 + **Mockito** (`mockito-core`, y `mockito-junit-jupiter` para la extensión) |
| Cobertura | **JaCoCo** (`jacoco-maven-plugin`) con umbral mínimo de 80 % (ver sección 9) |
| Pruebas de integración | `@QuarkusTest` + RestAssured; Dev Services está configurado, pero el IT registrado se ejecutó con PostgreSQL de Compose por incompatibilidad del cliente Docker |
| Contenedores | Dockerfile + `docker-compose.yml` (app + PostgreSQL) |

Reglas sobre dependencias:
- Mockito y JaCoCo están **aprobados** como parte del stack.
- **Pide confirmación al usuario antes de agregar cualquier otra dependencia o extensión no listada arriba.**
- Prohibido Lombok (usa `record`, constructores y getters explícitos; reduce magia y facilita la sustentación).
- Prohibido usar MapStruct u otros generadores de mapeo: los mappers son clases manuales, pequeñas y testeables.

---

## 3. Arquitectura: Clean Architecture

### 3.1 Regla de dependencia (inviolable)

```
infrastructure  ──►  application  ──►  domain
 (REST, JPA,          (casos de uso,    (entidades, value
  config, Quarkus)     puertos in/out)   objects, reglas)
```

- Las dependencias **solo apuntan hacia adentro**. `domain` no conoce a nadie; `application` solo conoce a `domain`; `infrastructure` conoce a ambas.
- **`domain` y `application` NO pueden importar** `jakarta.persistence.*`, `jakarta.ws.rs.*`, `jakarta.validation.*`, `jakarta.inject.*`, `io.quarkus.*`, ni clases de Jackson. Son Java puro.
- Los casos de uso se registran como beans desde `infrastructure/config` mediante métodos `@Produces`, de modo que `application` permanezca libre de anotaciones de framework.
- Estado actual: la regla se revisó manualmente y no se agregó ArchUnit ni una prueba automatizada de imports. No agregues ArchUnit sin aprobación; si se incorpora una prueba automatizada, debe respetar las restricciones de dependencias.

### 3.2 Estructura de paquetes

Paquete base: `com.assessment.employees`

```
src/main/java/com/assessment/employees/
├── domain/
│   ├── model/            # Employee, WorkShift, value objects (Document, DocumentType, Email)
│   │                     #   + reglas Colombia: ColombianHolidayCalendar, DayType, ShiftType,
│   │                     #     DayNightPolicy (ventana diurna/nocturna por vigencia),
│   │                     #     WeeklyWorkloadPolicy (límite ordinario por vigencia + tope de horas extra)
│   ├── repository/       # Puertos de salida: EmployeeRepository, WorkShiftRepository (interfaces)
│   └── exception/        # Excepciones de dominio (ver 7)
├── application/
│   ├── usecase/          # Empleados: CreateEmployee, GetEmployee, ListEmployees,
│   │                     #   UpdateEmployee, DeleteEmployee
│   │                     # Jornadas: CreateWorkShift, GetWorkShift, ListEmployeeWorkShifts,
│   │                     #   UpdateWorkShift, DeleteWorkShift, BulkCreateWorkShifts
│   └── dto/              # Commands y Results (records) propios de la capa
└── infrastructure/
    ├── rest/
    │   ├── resource/     # EmployeeResource, WorkShiftResource (JAX-RS), delgados:
    │   │                 # solo traducen HTTP ⇄ caso de uso
    │   ├── dto/          # Request/Response (records) con anotaciones de validación y OpenAPI
    │   ├── mapper/       # Mapeo manual REST DTO ⇄ command/result
    │   └── error/        # ExceptionMappers → application/problem+json
    ├── persistence/
    │   ├── entity/       # Entidades JPA (EmployeeEntity, WorkShiftEntity)
    │   ├── mapper/       # Entidad JPA ⇄ modelo de dominio
    │   └── repository/   # Adaptadores que implementan los puertos del dominio
    └── config/           # Productores CDI de casos de uso, configuración
```

```
src/main/resources/
├── application.properties
└── db/migration/         # V1__create_schema.sql, V2__seed_data.sql,
                          # V3__employee_document_identifier.sql, V4__colombian_work_shifts.sql

src/test/java/...         # *Test  → unitarias (JUnit 5 + Mockito, sin Quarkus)
                          # *IT    → integración (@QuarkusTest)
```

### 3.3 Responsabilidades por capa

**Domain**
- Modelo rico, no anémico: `Employee` valida sus invariantes en su fábrica/constructor y expone comportamiento (p. ej. `update(...)`), no solo getters/setters.
- Value objects (`record`) con validación en el constructor compacto: `Document` (tipo + número: identidad del empleado, con `DocumentType` CC/CE/PA), `Email`.
- `WorkShift` representa una jornada laboral (ver 4). Debe encapsular el cálculo de horas trabajadas, el desglose diurno/nocturno, la clasificación del día según el calendario colombiano y el intervalo real (incluido el cruce de medianoche).
- Los puertos (`EmployeeRepository`, `WorkShiftRepository`) se definen aquí, en términos del dominio.
- Sin dependencias externas. Se prueba con JUnit puro.

**Application**
- Un caso de uso = una clase = una intención = un método público (`execute`).
- Orquesta: carga del repositorio, invocación de lógica de dominio, persistencia. No contiene reglas de negocio que pertenezcan al dominio.
- Valida reglas que requieren consultar datos (p. ej. unicidad de email/documento) y lanza excepciones de dominio.
- Recibe/devuelve DTOs propios (`record`) o modelos de dominio; **nunca** DTOs REST ni entidades JPA.
- Se prueba con **Mockito** mockeando los puertos del dominio.

**Infrastructure**
- REST: recursos delgados, validación de formato con Bean Validation, mapeo manual, manejo centralizado de errores.
- Persistencia: entidades JPA **separadas** del modelo de dominio, con mapper explícito. Los adaptadores implementan los puertos.
- Cualquier detalle de Quarkus, SQL o HTTP vive aquí.

> Justificación para la sustentación: separar entidad JPA y modelo de dominio añade un mapper, pero mantiene el dominio independiente del ORM y demuestra la regla de dependencia. Es una decisión consciente de este ejercicio.

---

## 4. Modelo de dominio

### Employee

| Campo | Tipo | Reglas |
|---|---|---|
| `documentType` | `DocumentType` (CC, CE, PA) | Obligatorio. Parte de la identidad: inmutable tras creación. Case-insensitive, normalizado a mayúsculas |
| `documentNumber` | String | Obligatorio, inmutable tras creación. CC/CE: solo dígitos, 3–20. PA: alfanumérico, 5–20. `UNIQUE(document_type, document_number)` |
| `firstName` | String | Obligatorio, 1–100 |
| `lastName` | String | Obligatorio, 1–100 |
| `email` | String | Obligatorio, formato válido, único |
| `position` | String | Obligatorio, 1–100 |
| `hireDate` | LocalDate | Obligatorio, no futura. **Inmutable tras creación**: el PUT la exige idéntica; si difiere → 400 |

- **Identidad**: la pareja `(documentType, documentNumber)` es el identificador de API y dominio (rutas, puertos, excepciones). El UUID queda como PK técnica interna de `employees`, **nunca expuesta**.
- **PUT** actualiza solo los campos enviados (`firstName`, `lastName`, `email`, `position` opcionales; omitido = conserva el valor actual; se exige al menos un campo → 400); tipo y número de documento van en la URL, no en el body, y `hireDate` es inmutable (si se envía debe coincidir; si difiere → 400).
- Eliminación: **borrado físico real**, coherente con la operación *Delete* de un CRUD; las jornadas asociadas se eliminan en cascada vía la FK a la clave natural. No implementes borrado lógico.

### WorkShift (jornada laboral)

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | UUID | Generado; único UUID visible del modelo de jornadas |
| `employeeDocument` | `Document` | Clave natural del empleado dueño (tipo + número de documento) |
| `date` | LocalDate | Obligatorio (día de inicio). Puede ser pasada o futura (permite programar jornadas) |
| `startTime` | LocalTime | Obligatorio |
| `endTime` | LocalTime | Obligatorio. `endTime < startTime` = termina al día siguiente (turno nocturno). `endTime == startTime` → 400 |
| `workedHours` | Duration | **Calculado** por el dominio, no persistido |
| `daytimeHours` | Duration | Calculado: horas dentro de la ventana diurna vigente en `date` (06:00–21:00 hasta 2025-12-24; 06:00–19:00 desde 2025-12-25) |
| `nighttimeHours` | Duration | Calculado: horas dentro de la ventana nocturna vigente en `date` (21:00–06:00 hasta 2025-12-24; 19:00–06:00 desde 2025-12-25) |
| `dayType` | `DayType` | Calculado: ORDINARIO / DOMINICAL / FESTIVO (día de inicio, según `ColombianHolidayCalendar`) |
| `shiftType` | `ShiftType` | Calculado: DIURNA (sin horas nocturnas) / NOCTURNA (sin diurnas) / MIXTA |

Reglas de negocio (horarios laborales de Colombia):
- Una jornada pertenece a un único empleado, que debe existir.
- **Solapamiento por intervalo real**: dos jornadas del mismo empleado no pueden solaparse aunque crucen la medianoche. La validación vive en el dominio/caso de uso con consultas extendidas un día hacia atrás y devuelve 409.
- **Horas semanales y horas extra** (ver 4.1): la suma de horas por **semana ISO** (lunes a domingo, cortando los turnos en lunes 00:00) se compara con el límite ordinario vigente; el exceso es `overtimeHours` y se permite hasta 12 h por semana. Superar el límite ordinario + 12 h → `WeeklyHoursExceededException` → 409.
- `ColombianHolidayCalendar` calcula los festivos colombianos del año en Java puro: fijos (1-ene, 1-may, 20-jul, 7-ago, 8-dic, 25-dic), Semana Santa (jueves y viernes santo), pascuales trasladadas al lunes (Ascensión +43, Corpus Christi +64, Sagrado Corazón +71) y Emiliani (6-ene, 19-mar, 29-jun, 15-ago, 12-oct, 1-nov, 16-nov → lunes siguiente, Ley 904/2004).
- Las pausas (almuerzo) no se modelan: se registran como dos jornadas sin solape (p. ej. 08:00–12:00 y 13:00–17:00). Sin pausas modeladas, un 08:00–17:00 cuenta 9 horas de intervalo; documéntalo en el README.
- Al eliminar un empleado, sus jornadas se eliminan en cascada.

**Origen de los datos:** los datos ingresan exclusivamente por la API (ver 5.1): jornada individual, carga agrupada por franja (`bulk`) o datos semilla. `V2__seed_data.sql` mantiene el mínimo histórico, **`V4__colombian_work_shifts.sql`** agrega semillas realistas dentro del tope (jornada partida L–V de 40 h, nocturno 22:00–06:00 y un festivo colombiano real de 2026) y **`V5__overtime_seed_data.sql`** (Frente 3) agrega una semana de 2026 con horas extra, para que la demo muestre clasificación y el reporte de extra desde el primer arranque.

### 4.1 Parámetros legales por fecha (Colombia)

Los límites cambian con el tiempo; **no los codifiques como constantes únicas**. Implementa tablas inmutables de vigencias en el dominio (Java puro; p. ej. `record Vigencia(LocalDate desde, valor)` en listas ordenadas) con nombres de constantes explícitos.

**Límite semanal ordinario** (Ley 2101 de 2021):

| Vigente desde | Horas por semana |
|---|---|
| (antes del 2023-07-15) | 48 |
| 2023-07-15 | 47 |
| 2024-07-15 | 46 |
| 2025-07-15 | 44 |
| 2026-07-15 | **42** |

**Ventana diurna/nocturna** (Código Sustantivo del Trabajo, art. 160, modificado por la Ley 2466 de 2025, art. 10):

| Vigente desde | Diurna | Nocturna |
|---|---|---|
| (antes del 2025-12-25) | 06:00–21:00 | 21:00–06:00 |
| 2025-12-25 | 06:00–19:00 | 19:00–06:00 |

**Horas extra**: máximo **12 por semana** (art. 22 de la Ley 50 de 1990, modificado por la Ley 2466 de 2025).

**Reglas de aplicación (simplificaciones documentadas en el README):**
- El límite ordinario de una semana ISO es el **vigente en su domingo** (criterio conservador en semanas de transición; p. ej. la semana 2026-W29, del 13 al 19 de julio de 2026, usa 42 h).
- La ventana diurna/nocturna de una jornada completa es la **vigente en su `date`** (día de inicio), incluso si cruza la medianoche.
- Por semana ISO: `scheduledHours` = suma de todas las jornadas de la semana (aunque el filtro `from`/`to` la corte); `overtimeHours = max(0, scheduledHours − ordinaryLimitHours)`; `remainingOrdinaryHours = max(0, ordinaryLimitHours − scheduledHours)`. Si `overtimeHours > 12` → 409.
- **Fuera de alcance** (decláralo en el README): límite diario de 2 h extra, regímenes especiales (vigilancia, Ley 1920 de 2018; salud), distribuciones flexibles de jornada, tiempo parcial y todo cálculo monetario. La solución es una **aproximación técnica**; valida las reglas con el área legal/RR. HH. antes de usarla como referencia de nómina.


---

## 5. Contrato de la API

Prefijo base: `/api/v1`. Formato: JSON. Errores: `application/problem+json` (RFC 7807).

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/employees` | Crear empleado. Body: `documentType`, `documentNumber`, `firstName`, `lastName`, `email`, `position`, `hireDate` | 201 (+ `Location: /employees/{documentType}/{documentNumber}`), 400, 409 |
| GET | `/employees` | Listar con paginación (`page`, `size`) | 200 |
| GET | `/employees/{documentType}/{documentNumber}` | Obtener por documento | 200, 400, 404 |
| PUT | `/employees/{documentType}/{documentNumber}` | Actualizar campos editables opcionales (`firstName`, `lastName`, `email`, `position`: se actualizan solo los enviados); `hireDate`, si se envía, debe coincidir | 200, 400, 404, 409 |
| DELETE | `/employees/{documentType}/{documentNumber}` | Eliminar | 204, 404 |
| POST | `/employees/{documentType}/{documentNumber}/work-shifts` | Agregar jornada individual | 201 (+ `Location`), 400, 404, 409 |
| GET | `/employees/{documentType}/{documentNumber}/work-shifts` | Listar jornadas del empleado, filtro opcional `from` y `to` (ISO-8601, `yyyy-MM-dd`) | 200, 400, 404 |
| GET | `/employees/{documentType}/{documentNumber}/work-shifts/{shiftId}` | Obtener una jornada | 200, 404 |
| PUT | `/employees/{documentType}/{documentNumber}/work-shifts/{shiftId}` | Actualizar jornada (reemplazo completo) | 200, 400, 404, 409 |
| DELETE | `/employees/{documentType}/{documentNumber}/work-shifts/{shiftId}` | Eliminar jornada | 204, 404 |
| POST | `/employees/{documentType}/{documentNumber}/work-shifts/bulk` | Carga agrupada: rango `from`/`to` + `weekdays` + una franja horaria | 201, 400, 404, 409 |

Reglas:
- El empleado se referencia siempre por su documento: `documentType` ∈ {CC, CE, PA} y `documentNumber` validados por el dominio (tipo inválido o número con formato incorrecto → 400; no existe → 404). No expongas el UUID técnico interno en ninguna respuesta.
- Cuerpo de `POST`/`PUT` de jornada: `{ "date": "2026-10-15", "startTime": "08:00", "endTime": "17:00" }`. Un `endTime` anterior a `startTime` = jornada que cruza la medianoche. La respuesta incluye `id`, `date`, `startTime`, `endTime` (formato `HH:mm`), `dayType`, `shiftType`, `workedHours`, `daytimeHours` y `nighttimeHours` (horas decimales, ver 5.2). No repite el documento del empleado: ya viaja en la URL.
- `GET .../work-shifts` devuelve las jornadas ordenadas por fecha y hora de inicio ascendente, `totalWorkedHours` (suma exacta de las jornadas listadas, es decir, del rango filtrado) y **`weeklySummaries`**: una entrada por semana ISO completa tocada por el rango (aunque `from`/`to` la corten) con los campos de 5.2. Sin `from`/`to`, resume todas las semanas con jornadas del empleado. No existen totales diurno/nocturno del rango: el desglose vive en cada jornada.
- **Bulk todo-o-nada**: genera las fechas de `from` a `to` que caigan en `weekdays` (omitido = todos los días) y valida TODAS antes de persistir; si alguna solapa o rompe el tope semanal (límite ordinario + 12 h extra, ver 4.1) → 409 con las fechas conflictivas en el `detail` y no se crea nada. Rango máximo 92 días; `weekdays` vacío, rango inválido o cero jornadas generadas → 400. Respuesta 201: `{ "createdShifts": [...], "count", "scheduledHours" }` con `Location` al listado filtrado.
- Si `from > to` → 400. Si el empleado no existe → 404 (un empleado existente sin jornadas devuelve 200 con lista vacía).
- Una jornada solo es accesible a través de su empleado: si `shiftId` existe pero pertenece a otro empleado → 404.
- Jornada solapada con otra del mismo empleado (aunque cruce medianoche) → 409; semana que supere el límite ordinario + 12 h extra → 409. Superar solo el límite ordinario **no** es error: se informa en `overtimeHours`.
- La paginación de `/employees` devuelve un objeto con `items`, `page`, `size`, `totalItems`. Valores por defecto `page=0`, `size=20`, máximo `size=100`.
- Nunca expongas entidades JPA ni objetos de dominio directamente en la respuesta.
- Toda ruta debe estar documentada con OpenAPI (resumen, parámetros, códigos de respuesta y ejemplos, incluidos el bulk y jornadas con cruce de medianoche).

### 5.1 Interfaz de uso: no hay front-end

La solución es una **API REST pura**; el enunciado no pide interfaz gráfica. Los datos entran de tres formas:

1. **Swagger UI** (`/q/swagger-ui`): es la "interfaz" de la demo. Permite ver todos los endpoints, completar los cuerpos JSON y ejecutar las peticiones desde el navegador. Debe quedar habilitado también en el contenedor Docker, con ejemplos precargados en los esquemas OpenAPI (`@Schema(example=...)`).
2. **Clientes HTTP** (`curl`, Postman, Insomnia): el README incluye un flujo de ejemplo completo con `curl` (crear empleado → agregar jornada → carga agrupada con `bulk` → consultar jornadas con resumen semanal → actualizar → eliminar).
3. **Datos semilla** (`V2__seed_data.sql`, `V4__colombian_work_shifts.sql` y, tras el Frente 3, `V5__overtime_seed_data.sql`): para que la API tenga contenido clasificado, con resumen semanal y horas extra demostrables desde el primer arranque.

No crees un front-end (React, Angular, HTML, etc.) ni dependencias asociadas.

### 5.2 Formato de la respuesta de jornadas (contrato objetivo)

Ejemplo de `GET /api/v1/employees/CC/10000003/work-shifts?from=2026-10-12&to=2026-10-18`:

```json
{
  "shifts": [
    {
      "id": "650e8400-e29b-41d4-a716-446655440004",
      "date": "2026-10-15",
      "startTime": "10:00",
      "endTime": "18:00",
      "dayType": "ORDINARIO",
      "shiftType": "DIURNA",
      "workedHours": 8.00,
      "daytimeHours": 8.00,
      "nighttimeHours": 0.00
    }
  ],
  "totalWorkedHours": 8.00,
  "weeklySummaries": [
    {
      "week": "2026-W42",
      "startDate": "2026-10-12",
      "endDate": "2026-10-18",
      "scheduledHours": 8.00,
      "ordinaryLimitHours": 42.00,
      "overtimeHours": 0.00,
      "remainingOrdinaryHours": 34.00
    }
  ]
}
```

Diccionario de campos:

| Campo | Significado |
|---|---|
| `workedHours`, `daytimeHours`, `nighttimeHours` | Horas decimales (2 decimales, `HALF_UP`) calculadas desde minutos; `workedHours = daytimeHours + nighttimeHours`. Sin pausas modeladas (ver 4) |
| `totalWorkedHours` | Suma de `workedHours` de las jornadas **listadas** (rango filtrado) |
| `week` | Semana ISO (`yyyy-Www`) |
| `scheduledHours` | Horas programadas en **toda** la semana ISO (no depende del filtro) |
| `ordinaryLimitHours` | Límite ordinario semanal vigente para esa semana (Ley 2101 de 2021; p. ej. 47 en 2024-W03, 42 en 2026-W42) |
| `overtimeHours` | Horas por encima del límite ordinario (`0.00` si no hay); máximo permitido 12 |
| `remainingOrdinaryHours` | Horas ordinarias aún disponibles (`0.00` si ya hay horas extra) |

Ejemplo de semana con horas extra en 2026: 46 h programadas → `ordinaryLimitHours` 42.00, `overtimeHours` 4.00, `remainingOrdinaryHours` 0.00.

Implementación: el dominio sigue usando `Duration`; la conversión a horas decimales vive en un mapper REST (`Duration` → `BigDecimal`, escala 2), y el formato `HH:mm` en la configuración de serialización del DTO. Documenta cada campo con `@Schema(description, example)` en OpenAPI.


---

## 6. Convenciones de Clean Code

- **Nombres** en inglés para código (clases, métodos, variables, tablas, columnas); mensajes de error al cliente en español claro. Comentarios y Javadoc solo cuando aporten el *por qué*, nunca el *qué*.
- Métodos cortos (idealmente < 20 líneas) con una sola responsabilidad; clases pequeñas y cohesivas.
- Sin números ni strings mágicos: usa constantes con nombre.
- Inmutabilidad por defecto: `record`, campos `final`, colecciones inmutables.
- Prefiere inyección por constructor. Sin inyección por campo (también facilita instanciar clases en pruebas unitarias con Mockito).
- Sin `null` como valor de retorno de negocio: usa `Optional` en puertos de lectura.
- Sin código muerto, sin `TODO` sin dueño, sin `System.out`; usa logging (JBoss Logging / `Logger`) con niveles adecuados y **sin datos personales en logs**.
- Evita comentarios que desactiven código; borra lo que no se usa.
- Formato consistente (Google Java Style o el formateador por defecto del IDE); sin imports no usados.

---

## 7. Manejo de errores

Excepciones de dominio (sin dependencia de HTTP), por ejemplo:

- `EmployeeNotFoundException` (referencia por documento) → 404
- `WorkShiftNotFoundException` → 404
- `DuplicateEmployeeException` (email o combinación `documentType`+`documentNumber`) → 409
- `WorkShiftOverlapException` (jornadas solapadas, incluso cruzando medianoche) → 409
- `WeeklyHoursExceededException` (horas extra de la semana ISO por encima de 12, es decir, total > límite ordinario vigente + 12 h) → 409. El `detail` indica la semana, las horas programadas y el máximo permitido
- `InvalidEmployeeDataException` (incluye tipo/número de documento inválido e intento de cambiar `hireDate` en PUT) / `InvalidWorkShiftException` / `InvalidDateRangeException` (incluye rango del bulk inválido o vacío) → 400

`infrastructure/rest/error` contiene `ExceptionMapper`s que traducen estas excepciones y las de Bean Validation a `application/problem+json` con campos: `type`, `title`, `status`, `detail`, `instance` y, para validaciones, la lista de `errors` por campo. Nunca devuelvas stack traces ni mensajes internos de SQL al cliente. Un mapper genérico captura errores inesperados (500) y registra el detalle solo en logs.

---

## 8. Persistencia

- Esquema administrado **solo** por Flyway; `quarkus.hibernate-orm.database.generation=none`.
- Tablas: `employees` (PK técnica UUID interna, no expuesta) y `work_shifts`. Índices únicos en `email` y `(document_type, document_number)`.
- `work_shifts` referencia al empleado por su **clave natural**: FK compuesta `(employee_document_type, employee_document_number)` → `employees(document_type, document_number)` con `ON DELETE CASCADE`; la columna `employee_id` UUID se elimina. Índice en `(employee_document_type, employee_document_number, date)` para solapes y listados.
- Constraints tras V4: `CHECK (end_time <> start_time)` en `work_shifts` (permite cruce de medianoche, rechaza jornada de 0 horas) y `CHECK (document_type IN ('CC','CE','PA'))` en `employees` como defensa en profundidad (la validación real vive en el dominio).
- Migraciones: **V3** `employee_document_identifier` (columna `document_type` + backfill 'CC' + UNIQUE compuesta + rewire de la FK de `work_shifts`), **V4** `colombian_work_shifts` (CHECK de horario + semillas realistas dentro del tope semanal) y **V5** `overtime_seed_data` (Frente 3 — semana de 2026 con horas extra). **V4 ya fue aplicada** en la base activa `employees_api_v2`: no la edites (rompería el checksum de Flyway); la semilla de extra va obligatoriamente en V5. No toques V1 ni V2.
- Credenciales y URLs por variables de entorno con valores por defecto solo para desarrollo local. **Nunca** commitear secretos.
- Perfiles: Dev Services está habilitado por configuración para desarrollo/pruebas; el IT registrado se ejecutó con PostgreSQL externo por la incompatibilidad Docker descrita en el estado implementado. `%prod` usa variables de entorno (las que inyecta `docker-compose`).

---

## 9. Estrategia de pruebas y cobertura

### 9.1 Niveles

| Nivel | Qué prueba | Herramientas | Convención |
|---|---|---|---|
| Unitarias de dominio | Invariantes de `Employee` y `Document`, calendario de festivos, desglose diurno/nocturno, cruce de medianoche y tope semanal de `WorkShift` | JUnit 5 (sin Quarkus) | `*Test` |
| Unitarias de aplicación | Cada caso de uso, mockeando los puertos (`EmployeeRepository`, `WorkShiftRepository`) | JUnit 5 + **Mockito** | `*Test` |
| Unitarias de infraestructura | Mappers (REST y persistencia), `ExceptionMapper`s, recursos REST instanciados con casos de uso mockeados, adaptadores de repositorio con `EntityManager` mockeado cuando sea razonable | JUnit 5 + **Mockito** | `*Test` |
| Integración esperada | Endpoints completos contra PostgreSQL real, códigos HTTP y formato de errores | `@QuarkusTest` + RestAssured; preferir Dev Services cuando sea compatible | `*IT` |

**Cobertura de integración actual:** 2 IT en `EmployeeResourceIT` (`@QuarkusTest` + PostgreSQL de Compose con `TEST_DB_DEV_SERVICES=false`): (1) flujo completo de empleado por documento — alta, consulta por documento, listado, borrado — y una jornada creada/consultada con 404 tras borrar al empleado (cascada); (2) bulk, resumen semanal y rechazo 409 atómico ante solape (sin persistencia parcial). **No cubren aún los once endpoints ni sus errores HTTP** (pendiente heredado, ronda posterior al Frente 3). Las pruebas `EmployeeResourceTest` y `WorkShiftResourceTest` son unitarias: instancian los recursos y mockean los casos de uso.

### 9.2 Cobertura mínima: 80 % con pruebas unitarias

- Las **pruebas unitarias (`*Test`, Mockito/JUnit, sin levantar Quarkus) deben cubrir como mínimo el 80 %** de líneas del código de `src/main`. Las de integración son complementarias y **no cuentan** para cumplir este umbral.
- Configura `jacoco-maven-plugin` con: `prepare-agent`, `report` (fase `test`) y `check` con regla `BUNDLE` → `LINE COVEREDRATIO >= 0.80`. Idealmente añade también `BRANCH >= 0.70` en `domain` y `application`.
- Surefire ejecuta solo `*Test`; Failsafe ejecuta `*IT` (configurarlo siguiendo el `pom.xml` generado por Quarkus: `java.util.logging.manager`, `maven.home`, etc.).
- Exclusiones permitidas del cálculo (y solo estas): `infrastructure/config/**`, `infrastructure/persistence/entity/**` (POJOs de mapeo JPA sin lógica) y clases generadas. Los `record` de DTO no requieren exclusión.
- Si cumplir el 80 % obliga a pruebas frágiles y excesivamente acopladas sobre los adaptadores de repositorio, **consulta al usuario** antes de excluirlos; no los excluyas por tu cuenta.
- El build debe **fallar** si la cobertura baja de 80 %. En la configuración actual `jacoco:check` está ligado a `verify`; ejecuta `mvnw.cmd verify` (o `./mvnw verify` donde exista el script Unix) para aplicar el umbral. `test` genera el reporte, pero no ejecuta ese `check`.
- Reporta la cobertura real alcanzada (porcentaje por capa) en la sección de pruebas del `README.md`.

### 9.3 Reglas de las pruebas

- Estilo *Given/When/Then* o *Arrange/Act/Assert*; nombres que describan el comportamiento (`shouldRejectDuplicateEmail`).
- Objetivo de pruebas: cubrir caminos felices y de error (400, 404, 409) de cada caso de uso y endpoint. El estado actual de la cobertura HTTP de integración se describe arriba; no la presentes como completa.
- Casos obligatorios de la reestructuración: festivos colombianos exactos de 2026 (fijos, Semana Santa y Emiliani); desglose diurno/nocturno (08:00–17:00, 22:00–06:00, 05:00–23:00) y cambio de ventana por fecha (18:00–22:00 el 2025-12-24 → 1 h nocturna; el 2025-12-25 → 3 h nocturnas); solapes que cruzan la medianoche; límite ordinario por vigencia (47 h en 2024-W03; 47 h en 2024-W28 y 46 h en 2024-W29; 44 h en 2025-W29; 42 h en 2026-W29 y 2026-W42), horas extra (46 h programadas en 2026 → 4 h extra, 0 restantes), 409 al superar límite + 12 h y 200 con horas extra ≤ 12 (incluida la jornada domingo→lunes que reparte horas entre dos semanas ISO); `hireDate` inmutable (PUT con fecha distinta → 400); validación de documento por tipo y duplicados compuestos (CC-111 y CE-111 coexisten); bulk todo-o-nada con 409 por solape/tope y 400 por rango inválido, `weekdays` vacío o cero jornadas generadas.
- Con Mockito: usa `@ExtendWith(MockitoExtension.class)`, `@Mock`/`@InjectMocks` o construcción explícita; verifica interacciones relevantes (`verify`) pero evita sobre-especificar; usa `ArgumentCaptor` cuando importe lo que se persiste.
- Las pruebas deben ser deterministas (inyecta `Clock` si el código depende de la fecha actual, p. ej. validar que `hireDate` no sea futura) e independientes entre sí.
- No persigas cobertura vacía: cada prueba debe afirmar comportamiento, no solo ejecutar líneas.
- Antes de dar algo por terminado: `./mvnw verify` debe pasar en verde.

---

## 10. Docker (entregable opcional, recomendado)

- `Dockerfile` basado en la imagen JVM de Quarkus (puede partir del generado por `code.quarkus.io` en `src/main/docker/`).
- `docker-compose.yml` con dos servicios: `postgres` (con healthcheck) y `app` (depende de `postgres` healthy), variables de entorno parametrizadas y puerto 8080 expuesto.
- Debe funcionar con un único comando: `docker compose up --build`, y dejar disponibles Swagger UI (`/q/swagger-ui`), OpenAPI (`/q/openapi`) y salud (`/q/health`).
- La migración de datos de ejemplo debe permitir probar el endpoint de jornadas inmediatamente tras levantar los contenedores.

---

## 11. Entregables y documentación: todo en `README.md`

No crees una carpeta `docs/` ni archivos de documentación adicionales. El **`README.md`** es el único documento y debe contener, en este orden:

1. **Descripción** del proyecto y del alcance (CRUD de empleados + consulta de jornadas).
2. **Requisitos y ejecución**: local (`./mvnw quarkus:dev`) y Docker (`docker compose up --build`), URLs de Swagger UI y health.
3. **Ejemplos de uso** con `curl` para cada endpoint.
4. **Pruebas y cobertura**: cómo correr `./mvnw test` y `./mvnw verify`, y la cobertura unitaria real alcanzada (≥ 80 %).
5. **Arquitectura y diseño**: diagramas en **Mermaid** (renderizan nativamente en GitHub) que expliquen lo realizado:
   - Diagrama de capas/componentes con la regla de dependencia.
   - Diagrama de secuencia de dos flujos representativos (`POST /employees` y `GET /employees/{id}/work-shifts`).
   - Modelo entidad-relación simple.
   - Estructura de paquetes comentada.
   - Lista corta de **decisiones de diseño con su justificación** (apoyo para la sustentación): por qué Clean Architecture, entidad JPA separada del dominio, casos de uso sin anotaciones de framework, borrado físico, Flyway, problem+json, parámetros legales colombianos por vigencia (tabla con fuentes, simplificaciones y aviso de que no es asesoría legal), etc.
6. **Uso de IA**: respuestas a las 6 preguntas de la sección 12.

Además entrega: código fuente completo y compilable, `Dockerfile` y `docker-compose.yml`.

---

## 12. Uso de IA: sección obligatoria del `README.md`

La sección **"Uso de IA"** del README debe responder exactamente estas preguntas, cada una como subtítulo:

1. ¿Qué herramientas de IA utilizaste para desarrollar software?
2. ¿Cómo verificaste que el código generado por IA sea correcto con tu propuesta de solución?
3. ¿Qué riesgos ves en utilizar código generado por IA?
4. ¿Qué información nunca deberías enviarle a una IA?
5. ¿En qué etapas del ciclo de desarrollo utilizas IA?
6. ¿Qué parte de la solución generada o asistida por IA decidiste modificar o rechazar y por qué?

Instrucciones para el agente:
- No inventes una biografía ni experiencia fuera de esta conversación. Responde en primera persona delimitando los hechos a esta sesión: se usó GitHub Copilot en VS Code (GPT-6 Luna) para análisis/revisión, cambios de código, pruebas y documentación. No dejes marcadores `<!-- COMPLETAR POR EL CANDIDATO -->` en la versión final del README; si un dato no se puede afirmar, limita explícitamente la respuesta a esta sesión.
- En la pregunta 2 referencia evidencia concreta y acotada: última ejecución integral registrada (59 pruebas unitarias + 2 IT, JaCoCo 95,36 % — post-reestructuración; refrescar con el `verify` del Frente 3 cuando ocurra); IT de empleados/jornadas/bulk con PostgreSQL externo de Compose; revisión manual de imports; despliegue Docker con health, OpenAPI y Swagger en HTTP 200. Aclara que la integración no cubre aún todos los endpoints y que Dev Services no se validó por incompatibilidad Docker.
- Cada vez que el usuario **corrija o rechace** una sugerencia tuya, agrega una fila a la tabla de la pregunta 6 (`Sugerencia de la IA | Decisión | Motivo`). No mantengas un archivo de log aparte.
- Filas registradas (2026-10-15): (1) UUID como identidad → documento como identificador en toda la API; (2) endpoint de resumen semanal → carga semanal en el `GET` de jornadas existente; (3) `hireDate` actualizable → inmutable tras creación; (4) jornadas no aterrizadas a Colombia → reestructuración aprobada (clasificación, cruce de medianoche, control semanal). **Fila (5) pendiente** por registrar al ejecutar el Frente 3: la IA fijó el tope en 42 h y la ventana nocturna en 21:00 para cualquier fecha, devolvía duraciones en segundos y totales redundantes → el usuario lo señaló y se ajustó a límites por vigencia, horas extra, horas decimales y respuesta simplificada.
- Para las preguntas 3 y 4 incluye al menos: alucinaciones/APIs inexistentes, dependencias o versiones inseguras, código plausible pero incorrecto, fuga de datos, licencias; y nunca enviar secretos, credenciales, datos personales reales, datos de clientes ni código propietario sin autorización.

---

## 13. Reglas de trabajo para el agente

**Flujo**
1. Antes de implementar, resume brevemente tu plan y espera confirmación si el cambio es grande o toca la arquitectura.
2. Trabaja en **incrementos pequeños y verificables**; tras cada uno, compila y corre las pruebas relevantes.
3. Si algo es ambiguo, **pregunta al usuario** en lugar de asumir en silencio. Si asumes, deja la suposición documentada en el README (sección de decisiones).
4. Explica brevemente el *por qué* de las decisiones de diseño; el usuario deberá sustentarlas en persona.
5. Respeta el alcance: no agregues funcionalidades fuera del Desafío 1 (no autenticación, no front-end, no caché, no mensajería) salvo petición explícita.
6. **Escribe las pruebas unitarias junto con cada pieza de código**, no al final; verifica la cobertura de JaCoCo en cada fase.

**Estado de implementación y próximos pasos (actualizado 2026-10-16, post-ronda de remediación de code review)**

Implementado y verificado (Frentes 1–2, Frente 3 y ronda de remediación clean code/architectura): estructura Clean Architecture, dominio con `Document`/`DocumentType` como identidad, `ColombianHolidayCalendar`, `WeeklyWorkloadPolicy` con **vigencias + horas extra** y `DayNightPolicy` por fecha, casos de uso por documento, adaptadores JPA/REST, once endpoints OpenAPI, errores `problem+json` (sin `timestamp`), REST en horas decimales/`HH:mm`, Flyway V1–V5, Docker Compose desplegado sobre `employees_api_v2` y `verify` en verde (**75 unitarias + 2 IT, JaCoCo 96,87 % de líneas — domain 95,52 %, application 98,43 %, infrastructure 97,06 %**). La ronda de remediación (`38b631c`…`29c9e5b`) ejecutó: excepciones de dominio con mensajes en español (`InvalidWorkShiftException`, `InvalidDateRangeException` en `from > to`), puerto sin código muerto, `DocumentNumber`/`Date`/`Time`/overloads eliminados, `WorkShift` inmutable con `with(...)`, `Clock` inyectado en `CreateEmployee` con `currentDate` en fábricas de `Employee`, constantes nombradas (offsets pascuales documentados), REST delgado (`WorkShiftListResult.totalWorkedHours`, semana ISO y `parseIsoDate` en `WorkShiftMapper`, `WorkShiftResponse` con enums como `String`), guardas compartidas `WorkShift.rejectOverlap`/`weekStartOf` y commands de aplicación con `Document`. Detalle completo en la sección 1 y en las decisiones del README.

Estado del trabajo pendiente:

Incrementos 1–7 de la reestructuración y los cuatro incrementos del Frente 3 (3.1–3.4, detallados a continuación): **completados y verificados**; ver auditoría en el estado implementado de la sección 1. El detalle original se conserva como sustento del trabajo ya realizado.

**Incrementos del Frente 3 (ajuste normativo y de respuesta, 2026-10-16)** — cada uno compiló y pasó sus pruebas antes de continuar; hoy están cerrados:

- **3.1 Dominio — vigencias** ✅: `WeeklyWorkloadPolicy` (límite ordinario por vigencia, tope de 12 h extra, cálculo de `overtimeHours`) y `DayNightPolicy` (ventana por vigencia) con pruebas en las fechas frontera de 4.1.
- **3.2 Dominio/aplicación** ✅: `WorkShift` usa `DayNightPolicy`; el resumen semanal calcula límite, extra y restante en el caso de uso; `WeeklyHoursExceededException` y `BulkCreateWorkShifts` adoptan el criterio nuevo.
- **3.3 REST** ✅: DTOs y mappers con horas decimales, `HH:mm`, sin totales diurno/nocturno ni documento por jornada y `weeklySummaries` renombrado; OpenAPI con ejemplos y descripciones de campo.
- **3.4 Semillas y verificación** ✅: `V5__overtime_seed_data.sql` aplicada (`CC/10000001`, sábado 2026-10-17 06:00–12:00 → 2026-W42 con 46 h → 4 h extra), `mvnw.cmd verify` en verde (69 + 2), README y `memory.md` actualizados; demo Docker reconstruida, verificada con la API en vivo y detenido con `docker compose down` sin borrar el volumen.

Pendiente heredado (ronda posterior al Frente 3, según confirmación del usuario): IT HTTP de los once endpoints y sus errores; los 2 IT existentes mitigan esto pero no lo cubren por completo. Dev Services queda habilitado pero sin validar en este entorno (usar PostgreSQL de Compose con `TEST_DB_DEV_SERVICES=false`).
- Las respuestas de IA en el README están redactadas solo con evidencia de esta sesión; deben mantenerse sin placeholders.

**Prohibiciones**
- No romper la regla de dependencia (sección 3.1).
- No exponer entidades JPA ni objetos de dominio en la API.
- No poner lógica de negocio en recursos REST ni en entidades JPA.
- No usar `@Entity` en el dominio ni anotaciones de Quarkus/Jakarta en `domain`/`application`.
- El `README.md` es la documentación de entrega. `AGENTS.md` y el `memory.md` existente son archivos operativos para instrucciones/seguimiento, no documentos adicionales del entregable; no crear una carpeta `docs/` ni dispersar documentación de usuario.
- No bajar el umbral de cobertura ni agregar exclusiones no previstas en 9.2 para "hacer pasar" el build.
- No hardcodear secretos, no registrar datos personales en logs.
- No ejecutar comandos destructivos (borrado masivo, `git push --force`, `docker system prune`) sin confirmación.
- No inventar APIs de Quarkus ni de Mockito: ante duda, consulta la documentación oficial antes de usarlas.

**Git**
- Commits pequeños con **Conventional Commits** (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`).
- Un `.gitignore` adecuado para Java/Maven/IDE desde el primer commit.

---

## 14. Definición de terminado (checklist final)

- [x] Existe evidencia de `verify` verde **post-ronda de remediación (2026-10-16)**: 75 unitarias + 2 IT; JaCoCo 96,87 % de líneas (867/895) — domain 95,52 %, application 98,43 %, infrastructure 97,06 %.
- [x] Cobertura unitaria registrada ≥ 80 % y JaCoCo está configurado para fallar bajo el umbral durante `verify`.
- [x] Pruebas unitarias usan Mockito para aislar puertos y colaboradores.
- [x] Docker Compose construyó y arrancó app/BD; URLs health, OpenAPI y Swagger respondieron HTTP 200 en la ejecución registrada.
- [ ] IT de los once endpoints y de sus errores: pendiente (ronda posterior al Frente 3); hoy los 2 IT de `EmployeeResourceIT` cubren el flujo de empleados por documento, una jornada y bulk con 409, pero no la totalidad de endpoints/errores.
- [x] **Reestructuración (plan 2026-10-15)**: clasificación colombiana, cruce de medianoche, control semanal, `bulk` y `weeklySummaries` implementados y en verde en `verify`.
- [x] **Frente 3 (2026-10-16)**: límite semanal y ventana nocturna por vigencia (`WeeklyWorkloadPolicy`, `DayNightPolicy`; probados en fechas frontera), horas extra con tope de 12 h, duraciones en horas decimales, respuesta de jornadas sin totales redundantes y README con tabla de vigencias, fuentes y aviso de que no es asesoría legal.
- [x] **Ronda de subsanación del code review (2026-10-16)**: excepciones de dominio nombradas y mensajes en español, `Problem` sin `timestamp`, `WorkShift` inmutable con `with(...)`, `Clock` determinista inyectado, `Date`/`Time`/`DocumentNumber`/overloads muertos eliminados, constantes nombradas (pascuales con justificación de lunes), REST delgado y desacoplado (totales/semana ISO/parseo en mapper, enums como `String`), guardas compartidas de solape y semana ISO, y commands de aplicación con `Document`; verificada con `mvnw.cmd verify` (75 unitarias + 2 IT) en verde y demo Docker reconstruida (health/OpenAPI/Swagger 200, semana extra en vivo).
- [x] Identidad por documento (CC/CE/PA) en toda la API; UUID técnico no expuesto; `hireDate` inmutable (PUT distinto → 400).
- [x] Migraciones V1–V5 aplicadas (Flyway en V5 sobre `employees_api_v2`, 3 empleados y 18 jornadas; V5 semilla de horas extra, 46 h → 4 h extra demostrables vía API).
- [x] README actualizado para el Frente 3: tabla de vigencias, fuentes, curl con horas decimales y `overtimeHours`, fila (5) de la tabla de IA y cobertura/`verify` del Frente 3. (La versión actual ya documenta el contrato de 11 endpoints, ER con clave natural, ejemplos curl y las primeras filas de IA).
- [x] Las diez operaciones del contrato pre-reestructuración están documentadas en Swagger/OpenAPI, con ejemplos en los esquemas de entrada. Tras la reestructuración: once.
- [x] El README incluye ejemplos curl para las operaciones de empleados y jornadas.
- [x] Errores HTTP se documentan/representan con `application/problem+json`.
- [x] La revisión manual registrada no encontró imports de Jakarta/Quarkus/Jackson en `domain`/`application`; no hay prueba automatizada de arquitectura.
- [x] No se usa Lombok; la revisión y el build registrado no reportaron secretos.
- [x] `README.md` contiene ejecución, ejemplos, cobertura, diagramas Mermaid y las seis respuestas de IA sin placeholders.
- [ ] Confirmar nuevamente que no haya carpeta `docs/` antes de una entrega final.
- [ ] El candidato debe revisar que puede sustentar las decisiones y confirmar que las respuestas redactadas describen fielmente su experiencia.