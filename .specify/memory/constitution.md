<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.1.0
- Modified principles:
  - Delivery Workflow & Quality Gates -> added commit hygiene and squashing guidance
- Added sections:
  - None
- Removed sections:
  - None
- Templates requiring updates:
  - None
- Follow-up TODOs:
  - None
-->
# BlitzPay Constitution

## Core Principles

### I. Secrets and Key Material Are Never Exposed
- Credentials, private keys, and signing artifacts MUST NOT be committed to version control.
- Local development MUST use environment variables or approved secret stores; production MUST use
  platform-managed secrets.
- Logging MUST redact sensitive fields (tokens, client secrets, private keys, account identifiers).
Rationale: BlitzPay handles payment credentials and signatures; exposure creates immediate financial and
compliance risk.

### II. Payment Requests Must Be Cryptographically Verifiable
- Outbound payment requests MUST be signed using approved key material and verified against provider
  requirements before release.
- Any change to payment payload structure, signing inputs, or provider integration MUST include an updated
  contract definition and regression tests.
- Replay protection, idempotency handling, and request validation MUST be enforced at integration boundaries.
Rationale: Payment correctness depends on deterministic, verifiable request construction.

### III. Every Behavior Change Requires Automated Verification
- Any code change that can alter runtime behavior MUST include automated tests in the lowest effective layer
  (unit, integration, or contract), and tests MUST fail before the fix/feature is implemented.
- Bug fixes MUST include a regression test that fails on pre-fix behavior.
- Pull requests MUST pass the full CI test suite before merge.
Rationale: Financial workflows require repeatable proof that behavior is preserved.

### IV. Contracts and Failure Modes Must Be Explicit
- Public APIs, webhooks, and cross-module interfaces MUST have explicit schemas, validation rules, and
  documented error behavior.
- Features MUST define deterministic handling for boundary and failure cases (timeouts, upstream failures,
  invalid signatures, and duplicate requests).
- Backward-incompatible interface changes MUST trigger a MAJOR version impact assessment in affected artifacts.
Rationale: Explicit contracts prevent silent breakage between clients, modules, and environments.

### V. Production Behavior Must Be Observable
- Every externally visible operation MUST emit structured logs with correlation identifiers.
- Health, readiness, and critical domain metrics MUST be exposed and monitored in all deployed environments.
- Incident-prone flows (payments, invoicing, and third-party calls) MUST include traceable audit events.
Rationale: Fast diagnosis and auditability are mandatory for payment operations.

## Operational & Security Standards

- Primary runtime stack is Kotlin + Spring Boot (WebFlux/JPA) with PostgreSQL.
- Configuration MUST be environment-driven; no environment-specific constants in application code.
- New dependencies MUST include a justification in the feature plan and a security/licensing review note.
- Deployment manifests and CI/CD workflows MUST preserve health probes, secrets isolation, and rollback paths.

## Architecture References

- Architecture expectations are defined in [.specify/references/architecture_guidelines.md](../references/architecture_guidelines.md) and are mandatory for module
  design, boundaries, and API exposure.

## Delivery Workflow & Quality Gates

- Feature work MUST include `spec.md`, `plan.md`, and `tasks.md` artifacts aligned to this constitution.
- The Implementation Plan Constitution Check MUST pass before research/design starts and again before
  implementation.
- Feature and bug-fix branches MAY contain iterative work-in-progress commits while changes are under active
  development, but before merge they MUST be reduced to a small, reviewable set of commits that reflects the
  logical change set.
- Multiple fixup, checkpoint, or noisy intermediate commits SHOULD be squashed or otherwise cleaned up before
  merge so repository history remains clear, auditable, and easy to review.
- Code review MUST explicitly confirm:
  - principle compliance,
  - test evidence,
  - contract impact (if any),
  - observability impact.
- Release readiness requires passing CI, updated operational docs for changed behavior, and no unresolved
  critical constitution violations.

## Governance

This constitution supersedes conflicting local practices for this repository.

Amendment procedure:
- Propose changes via pull request that includes rationale, migration impact, and affected template updates.
- Obtain approval from at least one repository maintainer.
- Merge only after dependent templates and guidance files are synchronized or explicitly deferred.

Versioning policy:
- MAJOR: Backward-incompatible principle/governance redefinitions or removals.
- MINOR: New principle or materially expanded mandatory guidance.
- PATCH: Clarifications, wording improvements, and non-semantic edits.

Compliance review expectations:
- Every feature plan MUST document constitution gate results.
- Every pull request review MUST verify compliance with all MUST statements.
- Violations MUST be tracked in writing with owner and remediation date before merge approval.

**Version**: 1.1.0 | **Ratified**: 2026-03-06 | **Last Amended**: 2026-03-24
