# LRA AI Dashboard

A Quarkus application that puts an LLM in front of the Narayana LRA coordinator REST API.
Operators ask natural-language questions; the LLM calls coordinator endpoints as tools, correlates the results, and explains what is happening — including root causes and remediation steps — in plain English.

---

## Architecture

```
Browser / curl
      │  POST /chat  {"message": "Why is LRA X stuck?"}
      ▼
LraAiChatResource          (JAX-RS endpoint on port 8082)
      │  assistant.chat(message)
      ▼
LraAssistant               (LangChain4j AI Service interface)
      │  System prompt encodes the full LRA state machine and recovery protocol
      │  LangChain4j dispatches tool calls to LraTools as needed
      ▼
LLM  (Ollama / OpenAI)
      │  selects and calls one or more tools
      ▼
LraTools                   (@ApplicationScoped CDI bean)
      │  java.net.http.HttpClient — plain HTTP GET requests
      ▼
LRA Coordinator            (http://localhost:8080/lra-coordinator)
```

Tool calls (read or write) execute synchronously before the LLM begins streaming its text
response. The LLM may call multiple tools in sequence, correlating results across calls to
diagnose multi-participant failure cascades that are not apparent from any single API call.

---

## Prerequisites

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 17+ | |
| Maven | 3.9+ | |
| LRA Coordinator | any | `quay.io/jbosstm/lra-coordinator:latest`, running on `localhost:8080` by default |
| Ollama | any | Running on `localhost:11434` by default |
| llama3.1 (or compatible) | — | Must support tool/function calling |

### Install and start Ollama

```bash
# macOS
brew install ollama

# Linux
curl -fsSL https://ollama.com/install.sh | sh

# Pull the configured model
ollama pull llama3.1

# Start the server (if not already running as a service)
ollama serve
```

> **Tool-calling requirement:** The LLM must support Ollama's tool-calling API.
> `llama3.1` (the default) and `llama3.2`, `mistral-nemo`, `qwen2.5` all work.
> `llama3` (without `.1`) does **not** support tool calling and will return HTTP 400.
> To switch model: change `quarkus.langchain4j.ollama.chat-model.model-id` in
> `application.properties` and run `ollama pull <model-name>`.

---

## Quick start

### Step 1 — Start the LRA coordinator

```bash
podman run --network host quay.io/jbosstm/lra-coordinator:latest

# Confirm it is running (should return a JSON array)
curl http://localhost:8080/lra-coordinator
```

### Step 2 — Start Ollama

```bash
ollama serve &   # no-op if already running as a service
```

### Step 3 — Start the AI dashboard

```bash
cd rts/lra/lra-ai-dashboard
mvn quarkus:dev
```

Open **http://localhost:8082** for the browser chat UI, or use curl:

```bash
curl -s -X POST http://localhost:8082/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Are there any stuck transactions?"}' | jq .
```

---

## Configuration

All settings are in `src/main/resources/application.properties`.

| Property | Default | Purpose |
|----------|---------|---------|
| `quarkus.http.port` | `8082` | Avoids conflict with coordinator on 8080 |
| `lra.coordinator.url` | `http://localhost:8080/lra-coordinator` | Injected into `LraTools` as the base URL |
| `quarkus.langchain4j.ollama.chat-model.model-id` | `llama3.1` | Ollama model name (must support tool calling) |
| `quarkus.langchain4j.ollama.base-url` | `http://localhost:11434` | Ollama server URL |
| `quarkus.langchain4j.ollama.timeout` | `120s` | Generous timeout for local inference |

To override at startup without editing the file:

```bash
mvn quarkus:dev \
  -Dlra.coordinator.url=http://coordinator-host:8080/lra-coordinator \
  -Dquarkus.langchain4j.ollama.chat-model.model-id=mistral-nemo
```

---

## Switching to OpenAI

For cloud deployments where Ollama is unavailable:

1. In `pom.xml`, swap the commented/active LangChain4j dependency:

   ```xml
   <!-- comment out: -->
   <dependency>
       <groupId>io.quarkiverse.langchain4j</groupId>
       <artifactId>quarkus-langchain4j-ollama</artifactId>
   </dependency>

   <!-- uncomment: -->
   <dependency>
       <groupId>io.quarkiverse.langchain4j</groupId>
       <artifactId>quarkus-langchain4j-openai</artifactId>
   </dependency>
   ```

2. In `application.properties`, comment out the Ollama block and uncomment the OpenAI block.

3. Export your key and start:

   ```bash
   export LRA_AI_API_KEY=sk-...
   mvn quarkus:dev
   ```

---

## Source files

### `LraTools.java`

Six `@Tool`-annotated methods that form the saga-domain tool schema described in the patent.
Each method makes a single blocking HTTP GET to the coordinator and returns the raw JSON response
for the LLM to reason over.

| Method | Coordinator endpoint | When the LLM calls it |
|--------|---------------------|----------------------|
| `listAllLRAs()` | `GET /lra-coordinator/` | Overview of all transactions |
| `listLRAsByStatus(status)` | `GET /lra-coordinator/?Status=X` | Narrow focus to a specific state |
| `getLRADetails(lraId)` | `GET {lraId}` | Full participant breakdown for one LRA |
| `getLRAStatus(lraId)` | `GET {lraId}/status` | Cheap status-only check |
| `listRecoveringLRAs()` | `GET /lra-coordinator/recovery` | Confirm auto-recovery is running |
| `listFailedLRAs()` | `GET /lra-coordinator/recovery/failed` | Find transactions needing manual action |
| `closeLRA(lraId)` | `PUT {lraId}/close` | Operator-requested completion |
| `cancelLRA(lraId)` | `PUT {lraId}/cancel` | Operator-requested compensation |
| `startLRA(clientId, timeLimitMs)` | `POST /lra-coordinator/start` | Operator-requested new transaction |

The write tools (`closeLRA`, `cancelLRA`) are guarded in the system prompt: the LLM will
only call them when the operator explicitly requests it and will echo the target LRA ID
before acting.

The LRA ID is the full resource URI (e.g. `http://localhost:8080/lra-coordinator/0_ffff...`),
so `getLRADetails` and `getLRAStatus` are plain GETs on that URI with no path manipulation.
`java.net.http.HttpClient` is used directly to avoid classpath conflicts with the `lra-client`
module's RESTEasy dependency.

### `LraAssistant.java`

A LangChain4j AI Service interface. The `@SystemMessage` annotation encodes:
- All LRA lifecycle states and their transitions
- All participant status values and what each means
- The recovery protocol (automatic retry, when manual intervention is needed)
- Nested LRA failure propagation
- How to approach diagnosis (gather data first, then correlate and explain)

LangChain4j binds this interface to the configured LLM at startup and injects `LraTools`
as the tool provider. The result is a CDI bean injectable anywhere in the application.

### `LraAiChatResource.java`

A single `POST /chat` endpoint.
No `@Blocking` is needed: `Multi<String>` is handled natively by RESTEasy Reactive without
occupying the I/O thread.

---

## Example queries

```
Show me all active LRAs.

How many LRAs are currently in each state?

Are there any failed or stuck transactions?

Why is LRA http://localhost:8080/lra-coordinator/0_ffff7f000001_... stuck?

Is the recovery coordinator doing anything right now?

Which LRAs need manual intervention?
```

### Multi-step reasoning example

**Query:** *"Is there anything wrong right now?"*

A typical LLM reasoning chain:
1. Calls `listAllLRAs()` → spots several in `FailedToCancel`
2. Calls `listLRAsByStatus("FailedToCancel")` → gets IDs
3. Calls `getLRADetails(id)` for each → finds one participant with `FailedToCompensate`
4. Calls `listRecoveringLRAs()` → confirms that LRA is in the recovery queue
5. **Response:** *"There are 3 LRAs in FailedToCancel state. LRA `0_ffff...` has been
   stuck since participant `https://payment-service/compensate` returned FailedToCompensate.
   The recovery coordinator is actively retrying it. If the payment service is still down
   you will need to restore it and wait for the next retry cycle (approx. 2 minutes),
   or use the recovery coordinator API to force a terminal state manually."*

---

## Extending the PoC

| Extension | What to add |
|-----------|-------------|
| **forceRecovery tool** | `PUT /lra-coordinator/recovery/{id}` to force a stuck participant to a terminal state |
| **Proactive alerts** | Quarkus `@Scheduled` job calls `listFailedLRAs()` periodically; LLM generates alert if count > threshold |
| **Chat memory** | Add `@MemoryId` parameter for multi-turn operator sessions |
| **Multi-coordinator** | Aggregate state from all cluster nodes (see HA patent) for a cluster-wide view |
