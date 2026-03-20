# BlitzPay

## Generating key pairs

For instructions on generating the key pairs used by this project (public/private keys and signing requests), see the official TrueLayer documentation:

https://docs.truelayer.com/docs/sign-your-payments-requests

Follow the steps on that page to create and manage your keys; then place your private key (and any config) into the project as appropriate (for example, the repo contains example key files at the project root).

## Environment variables

The application expects the following environment variables to be set before running. Use the placeholders below and replace them with your actual credentials and key path.

- TRUELAYER_CLIENT_ID: TrueLayer client identifier
  - Example: `<YOUR_CLIENT_ID>`
- TRUELAYER_CLIENT_SECRET: TrueLayer client secret (sensitive)
  - Example: `<YOUR_CLIENT_SECRET>`
- TRUELAYER_KEY_ID: Key identifier used when signing requests
  - Example: `<YOUR_KEY_ID>`
- TRUELAYER_MERCHANT_ACCOUNT_ID: Merchant account ID
  - Example: `<YOUR_MERCHANT_ACCOUNT_ID>`
- TRUELAYER_PRIVATE_KEY_PATH: Path (relative to project root or absolute) to the private key file used for signing (PEM)
  - Example: `path/to/your-private-key.pem`

Note: Real credential values have been intentionally redacted in this README. Never commit secrets into the repository.

Suggested minimal `.env` file (do NOT commit this to version control):

```env
TRUELAYER_CLIENT_ID=<YOUR_CLIENT_ID>
TRUELAYER_CLIENT_SECRET=<YOUR_CLIENT_SECRET>
TRUELAYER_KEY_ID=<YOUR_KEY_ID>
TRUELAYER_MERCHANT_ACCOUNT_ID=<YOUR_MERCHANT_ACCOUNT_ID>
TRUELAYER_PRIVATE_KEY_PATH=path/to/your-private-key.pem
```

Example zsh commands to export variables for your current shell session and run the application (replace placeholders with real values locally):

```zsh
export TRUELAYER_CLIENT_ID="<YOUR_CLIENT_ID>"
export TRUELAYER_CLIENT_SECRET="<YOUR_CLIENT_SECRET>"
export TRUELAYER_KEY_ID="<YOUR_KEY_ID>"
export TRUELAYER_MERCHANT_ACCOUNT_ID="<YOUR_MERCHANT_ACCOUNT_ID>"
export TRUELAYER_PRIVATE_KEY_PATH="path/to/your-private-key.pem"

./gradlew bootRun
```

Or run with variables inline (single command):

```zsh
TRUELAYER_CLIENT_ID="<YOUR_CLIENT_ID>" \
TRUELAYER_CLIENT_SECRET="<YOUR_CLIENT_SECRET>" \
TRUELAYER_KEY_ID="<YOUR_KEY_ID>" \
TRUELAYER_MERCHANT_ACCOUNT_ID="<YOUR_MERCHANT_ACCOUNT_ID>" \
TRUELAYER_PRIVATE_KEY_PATH="path/to/your-private-key.pem" \
./gradlew bootRun
```

Security and repo hygiene notes
- Never commit private keys or `.env` files containing secrets into version control. Add entries to `.gitignore` such as:

```
# Ignore local environment files and private keys
.env
*.pem
```

- Prefer storing secrets in a secure vault or CI secret manager for production deployments.

If you'd like, I can also:
- add a `.env.example` file (non-sensitive) to the repo,
- add `.gitignore` entries for the private key and `.env`, or
- create a small Gradle task or startup script that loads the `.env` file securely.

## Invoice Agent (Koog + A2A)

### Architecture rationale

- The existing `invoice` module remains the business authority for invoice generation and data structures.
- A new Modulith adapter package `com.elegant.software.blitzpay.invoiceagent` provides AI/A2A-facing behavior.
- The adapter wraps existing invoice APIs via `InvoiceToolAdapter` and delegates validation/totals to `InvoiceAnalysisService` from the `invoice` module.
- No Koog/A2A classes are added to the invoice domain contracts.

### Compatibility notes (Spring Boot 4)

Koog documentation currently emphasizes Spring Boot integration with `koog-spring-boot-starter`, but this codebase runs Spring Boot `4.0.2`. Keep Koog-specific wiring isolated in `invoiceagent` and use feature flags (`invoice-agent.enabled`) so the core app can run if Koog wiring needs adjustment for Boot 4.

### How to run

```bash
./gradlew bootRun
```

Enable the agent in environment:

```bash
export INVOICE_AGENT_ENABLED=true
export OPENAI_API_KEY=<your-key>
./gradlew bootRun
```

### Environment variables

- `INVOICE_AGENT_ENABLED` – toggles agent endpoints
- `INVOICE_AGENT_MODEL` – model name string used by agent config
- `INVOICE_AGENT_BASE_URL` – externally reachable base URL
- `INVOICE_AGENT_A2A_PATH` – A2A endpoint path
- `OPENAI_API_KEY` – Koog/OpenAI key

### Sample A2A request/response

POST `${INVOICE_AGENT_BASE_URL}${INVOICE_AGENT_A2A_PATH}`

Request:

```json
{
  "id": "req-1",
  "method": "message/send",
  "params": {
    "message": {
      "role": "user",
      "parts": [
        {
          "type": "text",
          "text": "validate this invoice {\"invoiceNumber\":\"INV-1\",\"issueDate\":\"2026-03-01\",\"dueDate\":\"2026-03-31\",\"seller\":{\"name\":\"Seller\",\"street\":\"Main 1\",\"zip\":\"10115\",\"city\":\"Berlin\",\"country\":\"DE\"},\"buyer\":{\"name\":\"Buyer\",\"street\":\"Market 2\",\"zip\":\"20095\",\"city\":\"Hamburg\",\"country\":\"DE\"},\"lineItems\":[{\"description\":\"Service\",\"quantity\":1,\"unitPrice\":100,\"vatPercent\":19}],\"currency\":\"EUR\"}"
        }
      ]
    }
  }
}
```

Response:

```json
{
  "id": "req-1",
  "result": {
    "status": "completed",
    "message": {
      "role": "agent",
      "parts": [
        {
          "type": "text",
          "text": "{\"valid\":true,\"errors\":[]}"
        }
      ]
    }
  }
}
```

### Koog documentation decisions used

- **Spring Boot Integration**: chose `koog-spring-boot-starter` and externalized provider config under `ai.koog.*`.
- **A2A Protocol Overview**: kept A2A functionality opt-in and explicit, because A2A modules are not included by default.
- **A2A and Koog Integration**: modeled AgentCard and JSON-RPC transport path as dedicated agent adapter concerns.
- **Tool integration**: exposed invoice operations behind thin tool adapters that call existing invoice services instead of embedding business logic in prompts.


### Dependency resolution troubleshooting (HTTP 403)

If Gradle cannot download Koog/Ktor dependencies with `HTTP 403`, your environment likely blocks direct outbound Maven traffic.

Recommended approach:

1. Keep Koog disabled by default for core builds (`enableKoog=false`).
2. Configure a reachable Maven mirror/proxy:
   - Gradle property: `-PmavenMirrorUrl=https://<mirror>/maven-public`
   - or environment variable: `MAVEN_MIRROR_URL=https://<mirror>/maven-public`
3. Enable Koog only when mirror access is working:
   - `./gradlew -PenableKoog=true build`

Example:

```bash
export MAVEN_MIRROR_URL="https://artifactory.example.com/maven-public"
./gradlew -PenableKoog=true clean build
```
