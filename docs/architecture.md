# SmartTask Platform — Architecture

## Overview

SmartTask is an AI-powered enterprise incident and task management platform. It classifies, triages, and tracks operational issues using Claude (Anthropic) and Amazon Bedrock as dual AI providers.

## Services

| Service    | Technology              | Port | Responsibility                            |
|------------|-------------------------|------|-------------------------------------------|
| backend    | Spring Boot 2.7 / JDK 11| 8080 | Core REST API, JDBC, AI integration       |
| gateway    | Node.js / Express       | 3001 | API Gateway, rate limiting, routing       |
| frontend   | React 18 / MUI          | 3000 | SPA dashboard                             |
| analytics  | Python 3.11 / Flask     | 5000 | SQL-backed reporting and MTTR metrics     |

## Request Flow

```
Browser → nginx (frontend :80)
              └─ /api/*      → gateway (:3001) → backend (:8080) → DB
              └─ /analytics/* → gateway (:3001) → analytics (:5000) → DB
```

## AI Integration

Two AI providers are supported and can be selected per-request via the `aiProvider` query param:

- **Claude (Anthropic)** — calls `POST https://api.anthropic.com/v1/messages` using `claude-sonnet-4-6`.
- **Amazon Bedrock** — invokes `anthropic.claude-3-sonnet-20240229-v1:0` via the AWS SDK v2 `BedrockRuntimeClient`.

A `/api/ai/triage/compare` endpoint calls both providers in parallel (via `CompletableFuture`) and returns both responses for side-by-side comparison.

## Multi-Threading

`TaskProcessorService` runs on a scheduled fixed-delay (default 5 min) and enriches OPEN tasks with AI suggestions using a `FixedThreadPool(4)` + `CompletableFuture`. A `ConcurrentHashMap` cache prevents redundant API calls within a single enrichment cycle.

## Database

The backend uses Spring's `JdbcTemplate` / `NamedParameterJdbcTemplate` (JDBC). Profiles switch the datasource:

| Profile   | Database    | Driver                          |
|-----------|-------------|---------------------------------|
| (default) | H2 in-memory| Local dev only                  |
| `oracle`  | Oracle 19c+ | `ojdbc11`                       |
| `mssql`   | SQL Server  | `mssql-jdbc`                    |
| `db2`     | IBM DB2     | `jcc`                           |

Stored procedures (`sp_task_summary`, `sp_auto_close_stale_tasks`, `sp_mttr_report`) are provided for both MS SQL and Oracle in `scripts/db/`.

## Containerization

All four services have production-ready multi-stage Dockerfiles. The backend image is based on Red Hat UBI (`openjdk-11`) for OpenShift compatibility.

`docker-compose.yml` provides a local development stack.

## Kubernetes / OpenShift

Manifests in `k8s/` target both standard Kubernetes (Ingress) and OpenShift (Route annotations included as comments). Secrets are externalized via `secretKeyRef`.

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci-cd.yml`) runs:
1. Backend Maven build + JUnit tests
2. React build + tests
3. Python flake8 lint + pytest
4. Docker multi-image build & push (GHCR)
5. OpenShift deploy via `oc` CLI with image-tag substitution
