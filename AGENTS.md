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

### Estado implementado (2026-10-15)

- La API implementa CRUD de empleados y jornadas, PostgreSQL/Flyway, DTOs REST, errores `problem+json`, paginación y los diez endpoints descritos en la sección 5.
- Stack fijado en el repositorio: Java 21, Quarkus 3.8.5, Maven Wrapper 3.9.9 y `quarkus-resteasy-reactive-jackson`. No describas esta versión como la LTS más reciente ni cambies el stack sin revisar compatibilidad y ejecutar la suite.
- Las pruebas unitarias usan JUnit 5/Mockito. La última ejecución completa registrada en `memory.md` fue `verify` con 39 unitarias, 1 IT y JaCoCo 96,82 % (608/628 líneas). Después se corrigieron avisos en pruebas: las dos clases de repositorio afectadas pasaron individualmente y `EmployeeResourceIT` se recompiló; repite `verify` para una validación final integral.
- Actualmente existe un solo `*IT`, `EmployeeResourceIT`, que recorre creación, consulta, listado y eliminación de empleados contra PostgreSQL. Los recursos de empleados y jornadas tienen pruebas unitarias con casos de uso mockeados; no equivalen a pruebas HTTP de integración para los diez endpoints. Ampliar esa cobertura sigue pendiente si se exige probar cada endpoint/error de extremo a extremo.
- Dev Services está configurado por defecto, pero no se pudo validar en el entorno registrado por incompatibilidad de versión de API Docker. El IT se verificó con PostgreSQL de Compose mediante `TEST_DB_DEV_SERVICES=false`; no afirmes que Dev Services pasó.
- Docker Compose se construyó y probó; `health`, OpenAPI y Swagger UI devolvieron HTTP 200. Según `memory.md`, los contenedores permanecen activos. Deténlos con `docker compose down` cuando termine la demostración, sin borrar el volumen.
- Las seis respuestas de uso de IA deben quedar completas en `employees-api/README.md`, limitadas a hechos comprobables de la sesión y sin marcadores de completar.

**Alcance decidido:**
- **CRUD de empleados** (Create, Retrieve, Update, Delete reales).
- **CRUD de jornadas laborales de un empleado**: agregar, consultar (una y listado con filtro por fechas), actualizar y eliminar jornadas. Esto cubre el requisito de "consultar jornadas" y además resuelve cómo ingresan los datos a la aplicación.
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
│   ├── model/            # Employee, WorkShift, value objects (EmployeeId, Email, DocumentNumber)
│   ├── repository/       # Puertos de salida: EmployeeRepository, WorkShiftRepository (interfaces)
│   └── exception/        # Excepciones de dominio (ver 7)
├── application/
│   ├── usecase/          # Empleados: CreateEmployee, GetEmployee, ListEmployees,
│   │                     #   UpdateEmployee, DeleteEmployee
│   │                     # Jornadas: CreateWorkShift, GetWorkShift, ListEmployeeWorkShifts,
│   │                     #   UpdateWorkShift, DeleteWorkShift
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
└── db/migration/         # V1__create_schema.sql, V2__seed_data.sql

src/test/java/...         # *Test  → unitarias (JUnit 5 + Mockito, sin Quarkus)
                          # *IT    → integración (@QuarkusTest)
```

### 3.3 Responsabilidades por capa

**Domain**
- Modelo rico, no anémico: `Employee` valida sus invariantes en su fábrica/constructor y expone comportamiento (p. ej. `update(...)`), no solo getters/setters.
- Value objects (`record`) con validación en el constructor compacto: `EmployeeId` (UUID), `Email`, `DocumentNumber`.
- `WorkShift` representa una jornada laboral (ver 4). Debe encapsular el cálculo de horas trabajadas.
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
| `id` | UUID | Generado por el sistema |
| `documentNumber` | String | Obligatorio, único, 5–20 caracteres alfanuméricos |
| `firstName` | String | Obligatorio, 1–100 |
| `lastName` | String | Obligatorio, 1–100 |
| `email` | String | Obligatorio, formato válido, único |
| `position` | String | Obligatorio, 1–100 |
| `hireDate` | LocalDate | Obligatorio, no puede ser futura |

Eliminación: **borrado físico real**, coherente con la operación *Delete* de un CRUD; las jornadas asociadas se eliminan en cascada a nivel de base de datos. No implementes borrado lógico.

### WorkShift (jornada laboral)

| Campo | Tipo | Reglas |
|---|---|---|
| `id` | UUID | Generado |
| `employeeId` | UUID | FK a empleado |
| `date` | LocalDate | Obligatorio. Puede ser pasada o futura (permite programar jornadas) |
| `startTime` | LocalTime | Obligatorio |
| `endTime` | LocalTime | Obligatorio, posterior a `startTime` (no se permiten jornadas que crucen la medianoche) |
| `workedHours` | Duration/decimal | **Calculado** por el dominio, no persistido |

Reglas de negocio:
- Una jornada pertenece a un único empleado, que debe existir.
- **Un empleado no puede tener dos jornadas que se solapen en el mismo día** (la validación vive en el dominio/caso de uso y devuelve 409).
- Al eliminar un empleado, sus jornadas se eliminan en cascada.

**Origen de los datos:** los datos ingresan exclusivamente por la API (ver 5.1). Adicionalmente, la migración `V2__seed_data.sql` carga datos de ejemplo (al menos 3 empleados con varias jornadas) para que la demostración funcione apenas levanten los contenedores.

---

## 5. Contrato de la API

Prefijo base: `/api/v1`. Formato: JSON. Errores: `application/problem+json` (RFC 7807).

| Método | Ruta | Descripción | Respuestas |
|---|---|---|---|
| POST | `/employees` | Crear empleado | 201 (+ header `Location`), 400, 409 |
| GET | `/employees` | Listar con paginación (`page`, `size`) | 200 |
| GET | `/employees/{id}` | Obtener por id | 200, 404 |
| PUT | `/employees/{id}` | Actualizar (reemplazo completo) | 200, 400, 404, 409 |
| DELETE | `/employees/{id}` | Eliminar | 204, 404 |
| POST | `/employees/{employeeId}/work-shifts` | Agregar jornada a un empleado | 201 (+ `Location`), 400, 404, 409 |
| GET | `/employees/{employeeId}/work-shifts` | Listar jornadas del empleado, filtro opcional `from` y `to` (ISO-8601, `yyyy-MM-dd`) | 200, 400, 404 |
| GET | `/employees/{employeeId}/work-shifts/{shiftId}` | Obtener una jornada | 200, 404 |
| PUT | `/employees/{employeeId}/work-shifts/{shiftId}` | Actualizar jornada (reemplazo completo) | 200, 400, 404, 409 |
| DELETE | `/employees/{employeeId}/work-shifts/{shiftId}` | Eliminar jornada | 204, 404 |

Reglas:
- Cuerpo de `POST`/`PUT` de jornada: `{ "date": "2026-10-15", "startTime": "08:00", "endTime": "17:00" }`. La respuesta incluye `id`, `employeeId`, `date`, `startTime`, `endTime` y `workedHours`.
- `GET .../work-shifts` devuelve las jornadas ordenadas por fecha y hora de inicio ascendente, con `workedHours` por jornada y un `totalWorkedHours` del rango consultado.
- Si `from > to` → 400. Si el empleado no existe → 404 (un empleado existente sin jornadas devuelve 200 con lista vacía).
- Una jornada solo es accesible a través de su empleado: si `shiftId` existe pero pertenece a otro empleado → 404.
- Jornada solapada con otra del mismo empleado y día → 409.
- La paginación de `/employees` devuelve un objeto con `items`, `page`, `size`, `totalItems`. Valores por defecto `page=0`, `size=20`, máximo `size=100`.
- Nunca expongas entidades JPA ni objetos de dominio directamente en la respuesta.
- Toda ruta debe estar documentada con OpenAPI (resumen, parámetros, códigos de respuesta y ejemplos).

### 5.1 Interfaz de uso: no hay front-end

La solución es una **API REST pura**; el enunciado no pide interfaz gráfica. Los datos entran de tres formas:

1. **Swagger UI** (`/q/swagger-ui`): es la "interfaz" de la demo. Permite ver todos los endpoints, completar los cuerpos JSON y ejecutar las peticiones desde el navegador. Debe quedar habilitado también en el contenedor Docker, con ejemplos precargados en los esquemas OpenAPI (`@Schema(example=...)`).
2. **Clientes HTTP** (`curl`, Postman, Insomnia): el README incluye un flujo de ejemplo completo con `curl` (crear empleado → agregar jornada → consultar jornadas → actualizar → eliminar).
3. **Datos semilla** (`V2__seed_data.sql`): para que la API tenga contenido desde el primer arranque.

No crees un front-end (React, Angular, HTML, etc.) ni dependencias asociadas.

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

- `EmployeeNotFoundException` → 404
- `WorkShiftNotFoundException` → 404
- `DuplicateEmployeeException` (email o documento) → 409
- `WorkShiftOverlapException` (jornada solapada) → 409
- `InvalidEmployeeDataException` / `InvalidWorkShiftException` / `InvalidDateRangeException` → 400

`infrastructure/rest/error` contiene `ExceptionMapper`s que traducen estas excepciones y las de Bean Validation a `application/problem+json` con campos: `type`, `title`, `status`, `detail`, `instance` y, para validaciones, la lista de `errors` por campo. Nunca devuelvas stack traces ni mensajes internos de SQL al cliente. Un mapper genérico captura errores inesperados (500) y registra el detalle solo en logs.

---

## 8. Persistencia

- Esquema administrado **solo** por Flyway; `quarkus.hibernate-orm.database.generation=none`.
- Tablas: `employees` y `work_shifts` (FK con `ON DELETE CASCADE`), índices únicos en `email` y `document_number`, índice en `work_shifts(employee_id, date)`.
- Credenciales y URLs por variables de entorno con valores por defecto solo para desarrollo local. **Nunca** commitear secretos.
- Perfiles: Dev Services está habilitado por configuración para desarrollo/pruebas; el IT registrado se ejecutó con PostgreSQL externo por la incompatibilidad Docker descrita en el estado implementado. `%prod` usa variables de entorno (las que inyecta `docker-compose`).

---

## 9. Estrategia de pruebas y cobertura

### 9.1 Niveles

| Nivel | Qué prueba | Herramientas | Convención |
|---|---|---|---|
| Unitarias de dominio | Invariantes de `Employee`, value objects, cálculo de horas de `WorkShift` | JUnit 5 (sin Quarkus) | `*Test` |
| Unitarias de aplicación | Cada caso de uso, mockeando los puertos (`EmployeeRepository`, `WorkShiftRepository`) | JUnit 5 + **Mockito** | `*Test` |
| Unitarias de infraestructura | Mappers (REST y persistencia), `ExceptionMapper`s, recursos REST instanciados con casos de uso mockeados, adaptadores de repositorio con `EntityManager` mockeado cuando sea razonable | JUnit 5 + **Mockito** | `*Test` |
| Integración esperada | Endpoints completos contra PostgreSQL real, códigos HTTP y formato de errores | `@QuarkusTest` + RestAssured; preferir Dev Services cuando sea compatible | `*IT` |

**Cobertura de integración actual:** `EmployeeResourceIT` valida el flujo de crear, obtener, listar y borrar empleado. No hay actualmente IT equivalente para jornadas ni cobertura HTTP de integración de los diez endpoints. Las pruebas `EmployeeResourceTest` y `WorkShiftResourceTest` son unitarias: instancian los recursos y mockean los casos de uso.

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
   - Lista corta de **decisiones de diseño con su justificación** (apoyo para la sustentación): por qué Clean Architecture, entidad JPA separada del dominio, casos de uso sin anotaciones de framework, borrado físico, Flyway, problem+json, etc.
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
- En la pregunta 2 referencia evidencia concreta y acotada: última ejecución integral registrada (39 pruebas unitarias, 1 IT, JaCoCo 96,82 %); IT de empleados con PostgreSQL externo; revisión manual de imports; prueba Docker y endpoints HTTP 200. Aclara que la integración no cubre aún todos los endpoints y que Dev Services no se validó por incompatibilidad Docker.
- Cada vez que el usuario **corrija o rechace** una sugerencia tuya, agrega una fila a la tabla de la pregunta 6 (`Sugerencia de la IA | Decisión | Motivo`). No mantengas un archivo de log aparte.
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

**Estado de implementación y próximos pasos**
- Implementadas: estructura, dominio, casos de uso, adaptadores JPA/REST, CRUD de empleados y jornadas, Flyway/seed, OpenAPI, errores `problem+json`, pruebas unitarias, umbral JaCoCo, Docker Compose y README con diagramas y flujo curl.
- Parcial: integración HTTP; solo hay un `EmployeeResourceIT` para un flujo de empleados. Agregar IT para jornadas y respuestas de error si se requiere cumplir la cobertura de integración especificada en 9.3.
- Parcial: la ruta con Dev Services está configurada, pero el entorno probado requirió PostgreSQL Compose como fallback.
- Antes de declarar cierre técnico final: correr `mvnw.cmd verify` después de los cambios recientes de tests; revisar la migración de Quarkus/REST solo si el usuario decide alinear las coordenadas al stack moderno; detener contenedores de demostración sin borrar el volumen.
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

- [x] Existe evidencia de `verify` verde: 39 unitarias + 1 IT; JaCoCo 96,82 %. **Pendiente repetir `verify` tras los últimos cambios de tests.**
- [x] Cobertura unitaria registrada ≥ 80 % y JaCoCo está configurado para fallar bajo el umbral durante `verify`.
- [x] Pruebas unitarias usan Mockito para aislar puertos y colaboradores.
- [x] Docker Compose construyó y arrancó app/BD; URLs health, OpenAPI y Swagger respondieron HTTP 200 en la ejecución registrada.
- [ ] IT de los diez endpoints y de sus errores: pendiente; hoy solo `EmployeeResourceIT` prueba el flujo CRUD básico de empleados.
- [x] Las diez operaciones están documentadas en Swagger/OpenAPI, con ejemplos en los esquemas de entrada.
- [x] El README incluye ejemplos curl para las operaciones de empleados y jornadas.
- [x] Errores HTTP se documentan/representan con `application/problem+json`.
- [x] La revisión manual registrada no encontró imports de Jakarta/Quarkus/Jackson en `domain`/`application`; no hay prueba automatizada de arquitectura.
- [x] No se usa Lombok; la revisión y el build registrado no reportaron secretos.
- [x] `README.md` contiene ejecución, ejemplos, cobertura, diagramas Mermaid y las seis respuestas de IA sin placeholders.
- [ ] Confirmar nuevamente que no haya carpeta `docs/` antes de una entrega final.
- [ ] El candidato debe revisar que puede sustentar las decisiones y confirmar que las respuestas redactadas describen fielmente su experiencia.
