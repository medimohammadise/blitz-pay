## Structured Logging — Production Standard

**Spec**: [`specs/000-structured-logging/spec.md`](https://github.com/elegant-software/blitz-pay/blob/main/specs/000-structured-logging/spec.md)
**Reference**: https://x.com/gutsOfDarkness8/status/2040108325996732444

### Problem

BlitzPay currently uses plain text logging with no consistent format, no correlation IDs, and mixed logging facades (`LoggerFactory` vs `KotlinLogging`). This makes production incident investigation slow and prevents effective use of observability tools (ELK, Loki, Datadog, Splunk).

### Solution

Adopt structured logging (JSON format) with correlation IDs as the production standard, following the industry-standard pattern of **SLF4J + Logback JSON layout + MDC for context**.

### Key Changes

1. **JSON-structured log output** — All logs emitted as single-line JSON via Spring Boot native structured logging (`logging.structured.format.console=ecs`) or `logstash-logback-encoder`
2. **Correlation ID propagation** — Every HTTP request gets a unique `correlationId` (from `X-Correlation-ID` header or generated UUID), stored in MDC and included in every log line
3. **Reactive MDC bridging** — Micrometer Context Propagation ensures MDC (including correlation IDs) survives thread hops in WebFlux reactive pipelines
4. **Unified logging facade** — All Kotlin classes standardize on `KotlinLogging.logger {}`, replacing mixed `LoggerFactory.getLogger()` usage
5. **Structured domain context** — Use `StructuredArguments.keyValue()` for domain-specific fields (`paymentId`, `invoiceId`, `amount`) as queryable JSON keys
6. **Dev-friendly fallback** — Human-readable console output available via `dev` Spring profile

### Example Pattern (Kotlin adaptation of reference tweet)

```kotlin
private val logger = KotlinLogging.logger {}

fun processPayment(request: CreatePaymentRequest) {
    val traceId = MDC.get("correlationId")  // Set by WebFilter

    logger.info("Starting payment processing",
        keyValue("correlationId", traceId),
        keyValue("customerId", request.customerId))

    // ... business logic ...

    logger.info("Payment processed successfully",
        keyValue("correlationId", traceId),
        keyValue("paymentId", payment.id),
        keyValue("amount", payment.amount))
}
```

### Governance

- Policy added to `CONSTITUATION.md` (Section VI: Structured Logging)
- Reference added to `CONTRIBUTING.md` coding convention table

### Acceptance Criteria

- [ ] All log lines are valid single-line JSON in default (production) configuration
- [ ] All log lines during a request carry the same `correlationId`
- [ ] MDC propagates through reactive chains (WebFlux)
- [ ] All Kotlin files use `KotlinLogging.logger {}` (no direct `LoggerFactory`)
- [ ] `dev` profile provides human-readable output
- [ ] `CONSTITUATION.md` and `CONTRIBUTING.md` updated with policy
