# Feature Specification: Structured Logging

**Feature Branch**: `000-structured-logging`
**Created**: 2026-04-04
**Status**: Draft
**Input**: User description: "Adopt structured logging (JSON format) with correlation IDs as the production standard, replacing plain text logs. Reference: https://x.com/gutsOfDarkness8/status/2040108325996732444"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - JSON-Formatted Log Output (Priority: P1)

As an operations engineer investigating a production incident, I need all application logs emitted in a consistent JSON format so observability tools (ELK, Loki, Datadog, Splunk) can parse, index, and query them without custom grok patterns or fragile regex extractors.

**Why this priority**: JSON-structured output is the foundation that every other structured logging capability depends on. Without it, correlation IDs and contextual key-value pairs are trapped in unstructured text.

**Independent Test**: Start the application and confirm that every log line written to stdout is valid JSON containing at minimum `timestamp`, `level`, `logger_name`, and `message` fields.

**Acceptance Scenarios**:

1. **Given** the application starts with the default profile, **When** any component emits a log statement, **Then** the output is a single-line JSON object parseable by standard JSON tools.
2. **Given** a log statement includes an exception, **When** the JSON output is examined, **Then** the stack trace appears as a structured field (e.g., `stack_trace`) rather than multi-line plain text interleaved with other log lines.
3. **Given** an observability pipeline ingests application logs, **When** an operator queries by `level`, `logger_name`, or any MDC key, **Then** the query returns matching entries without custom parsing configuration.

---

### User Story 2 - Correlation ID Propagation (Priority: P1)

As an operations engineer tracing a request across modules, I need every log line produced while handling a request to carry a unique correlation ID so I can reconstruct the full request lifecycle with a single query.

**Why this priority**: Correlation IDs are the primary mechanism for connecting log lines across modules and async boundaries during incident investigation. Without them, structured JSON is searchable but not traceable.

**Independent Test**: Send an HTTP request and confirm that every log line emitted during its processing contains the same `correlationId` value, including lines from cross-module event listeners.

**Acceptance Scenarios**:

1. **Given** an inbound HTTP request arrives without an `X-Correlation-ID` header, **When** the request is processed, **Then** the system generates a unique correlation ID and attaches it to every log line via MDC.
2. **Given** an inbound HTTP request carries an `X-Correlation-ID` header, **When** the request is processed, **Then** the system uses the provided value as the correlation ID in all log lines.
3. **Given** a request triggers a cross-module domain event via `ApplicationEventPublisher`, **When** the `@ApplicationModuleListener` handles the event, **Then** log lines in the listener carry the same correlation ID as the originating request.
4. **Given** the application uses Spring WebFlux reactive pipelines, **When** execution hops across threads in a reactive chain, **Then** the correlation ID remains available in MDC through Reactor Context propagation.

---

### User Story 3 - Structured Key-Value Context per Log Line (Priority: P2)

As a developer diagnosing a payment failure, I need log statements to carry domain-specific key-value pairs (e.g., `paymentId`, `customerId`, `amount`) as first-class structured fields so I can filter and aggregate logs by business attributes without parsing message text.

**Why this priority**: Key-value context transforms logs from human-readable messages into machine-queryable events, enabling faster root-cause analysis and richer alerting rules.

**Independent Test**: Trigger a payment flow and confirm that log lines contain structured fields like `paymentId` and `amount` as discrete JSON keys rather than interpolated into the message string.

**Acceptance Scenarios**:

1. **Given** a service method logs a business event, **When** the developer uses `StructuredArguments.keyValue()` or equivalent, **Then** the key-value pair appears as a top-level JSON field in the log output.
2. **Given** a log statement uses the kotlin-logging fluent API, **When** structured arguments are passed, **Then** they appear both in the human-readable message fallback and as discrete JSON fields.

---

### User Story 4 - Unified Logging Facade (Priority: P2)

As a contributor writing new code, I need a single, consistent logging pattern across the entire codebase so I do not have to choose between competing conventions or wonder which import to use.

**Why this priority**: The codebase currently mixes `org.slf4j.LoggerFactory` and `mu.KotlinLogging` patterns. A single standard reduces cognitive load and ensures all log lines benefit from structured output uniformly.

**Independent Test**: Search the codebase and confirm all production classes use the same logging facade and initialization pattern.

**Acceptance Scenarios**:

1. **Given** a contributor creates a new Kotlin class that needs logging, **When** they consult repository standards, **Then** they find a single prescribed pattern using `KotlinLogging.logger {}`.
2. **Given** existing classes use `LoggerFactory.getLogger(javaClass)`, **When** the structured logging migration is complete, **Then** those classes have been updated to the standard `KotlinLogging.logger {}` pattern.

---

### User Story 5 - Development-Friendly Console Output (Priority: P3)

As a developer running the application locally, I need the option to see human-readable log output during development so structured JSON does not hinder local debugging.

**Why this priority**: Pure JSON output is hard to scan visually during development. A dev-friendly fallback improves daily developer experience without compromising production observability.

**Independent Test**: Start the application with the `dev` profile and confirm log output is human-readable, then start with the default profile and confirm JSON output.

**Acceptance Scenarios**:

1. **Given** the application runs with a `dev` or `local` Spring profile, **When** a log statement is emitted, **Then** console output uses a human-readable pattern (timestamp, level, logger, message) instead of JSON.
2. **Given** the application runs without an explicit dev profile (production default), **When** a log statement is emitted, **Then** console output is structured JSON.

---

### Edge Cases

- What happens when a reactive chain forks into parallel paths (e.g., `Flux.merge`)? The correlation ID must propagate into each fork independently.
- How does the system handle extremely long stack traces in JSON format? The structured output must truncate or limit stack depth to avoid excessive log volume.
- What happens when an `@ApplicationModuleListener` runs asynchronously on a different thread pool? MDC context must still propagate via Reactor Context or explicit MDC copy.
- How does the system behave when the `X-Correlation-ID` header contains invalid or excessively long values? The system should sanitize or reject malformed correlation IDs.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The application MUST emit all log output in structured JSON format by default using Spring Boot's native structured logging support (`logging.structured.format.console`) or `logstash-logback-encoder`.
- **FR-002**: Every log line MUST include at minimum: `timestamp`, `level`, `logger_name`, `message`, `thread_name`, and all active MDC entries.
- **FR-003**: The application MUST generate or propagate a correlation ID for every inbound HTTP request and store it in MDC under a consistent key (e.g., `correlationId`).
- **FR-004**: Inbound requests carrying an `X-Correlation-ID` header MUST use the provided value; requests without the header MUST receive a generated UUID.
- **FR-005**: MDC context, including correlation IDs, MUST propagate across reactive thread boundaries using Micrometer Context Propagation (`io.micrometer:context-propagation`).
- **FR-006**: All production Kotlin classes MUST use `io.github.microutils:kotlin-logging-jvm` (`KotlinLogging.logger {}`) as the logging facade. Direct `LoggerFactory.getLogger()` usage MUST be replaced.
- **FR-007**: Developers SHOULD use `StructuredArguments.keyValue()` (or the kotlin-logging equivalent) to attach domain-specific key-value pairs to log statements for machine-queryable context.
- **FR-008**: Exception stack traces MUST appear as a structured JSON field rather than multi-line plain text.
- **FR-009**: A development Spring profile MUST provide human-readable console output as an alternative to JSON.
- **FR-010**: The structured logging standard MUST be documented as policy in `CONSTITUATION.md` and `CONTRIBUTING.md`.

### Key Entities

- **Correlation ID**: A unique identifier (UUID) assigned to each inbound request and propagated through all log lines produced during that request's lifecycle.
- **MDC (Mapped Diagnostic Context)**: A thread-local (or Reactor Context-bridged) map of key-value pairs automatically included in every log line.
- **Structured Argument**: A domain-specific key-value pair attached to an individual log statement for per-event context (e.g., `paymentId`, `invoiceId`).
- **Log Format Profile**: A Spring profile-driven configuration that switches between JSON (production) and human-readable (development) log output.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of log lines emitted by the application in default configuration are valid single-line JSON objects.
- **SC-002**: 100% of log lines produced during an HTTP request lifecycle contain a `correlationId` field with the same value.
- **SC-003**: An operator can query logs by any MDC key or structured argument key in an observability tool without custom parsing rules.
- **SC-004**: 0 production Kotlin files use `LoggerFactory.getLogger()` directly; all use the `KotlinLogging.logger {}` standard.
- **SC-005**: Developers can switch to human-readable output by activating a single Spring profile with no code changes.
- **SC-006**: New contributors can identify the correct logging pattern and structured argument usage by consulting repository standards alone.

## Assumptions

- Spring Boot 4.x native structured logging support (`logging.structured.format.console=ecs` or `logstash`) is sufficient for the initial implementation. Migration to `logstash-logback-encoder` is deferred unless the native support proves insufficient for key-value argument embedding.
- The existing `kotlin-logging-jvm` dependency (v3.0.5) will serve as the unified logging facade. Version upgrade may be needed if the fluent structured argument API requires a newer release.
- Micrometer Context Propagation is available on the classpath (via `spring-boot-starter-webflux` transitive dependencies in Boot 4.x) or will be added explicitly.
- The `dev` / `local` profile for human-readable output is additive and does not affect production behavior.
- This spec covers application logging only. Access logs, audit logs, and external service client logs are out of scope for this iteration.
