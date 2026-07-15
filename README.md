# SmartTask Platform

An AI-powered enterprise incident and task management system. When a production issue is logged, the platform automatically triages it using Claude (Anthropic) or Amazon Bedrock — suggesting a priority level and recommended resolution steps.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 11, Spring Boot 2.7, Spring JDBC, Maven |
| AI | Claude (Anthropic API), Amazon Bedrock (AWS SDK v2) |
| Frontend | React 18, Material UI, Recharts |
| Gateway | Node.js, Express, express-rate-limit |
| Analytics | Python 3.11, Flask, Pandas, SQLAlchemy |
| Database | H2 (dev), Oracle, MS SQL Server, DB2 |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes / OpenShift |
| CI/CD | GitHub Actions |

## Running Locally

The only prerequisite is [Docker](https://www.docker.com/get-started).

```bash
# 1. Clone the repo
git clone https://github.com/MMShed/smarttask-platform.git
cd smarttask-platform

# 2. Add your credentials
cat > .env << 'EOF'
ANTHROPIC_API_KEY=your_key_here
AWS_ACCESS_KEY_ID=your_key_here
AWS_SECRET_ACCESS_KEY=your_key_here
AWS_REGION=us-east-1
EOF

# 3. Start all services
docker compose -f docker/docker-compose.yml up --build
```

Then open http://localhost:3000.

> The app runs without AI credentials — task creation and management work fully. AI triage features will show a fallback message if keys are not provided.

## Services

| Service | Port | Description |
|---|---|---|
| Frontend | 3000 | React dashboard |
| Gateway | 3001 | Node.js API gateway with rate limiting |
| Backend | 8080 | Spring Boot REST API |
| Analytics | 5000 | Python reporting service |
| H2 Console | 8080/h2-console | In-memory DB browser (dev only) |

## Features

- **AI Triage** — Submit a task and get an instant priority + resolution suggestion from Claude or Bedrock
- **Provider Comparison** — Call both AI providers in parallel and compare responses side-by-side
- **Background Enrichment** — A scheduled multi-threaded job automatically triages any OPEN tasks that don't yet have AI suggestions
- **Dashboard** — Bar charts showing task breakdown by status and priority
- **Multi-database** — Switch between H2, Oracle, MS SQL Server, or DB2 via Spring profiles

## Project Structure

```
smarttask-platform/
├── backend/          # Spring Boot (Java 11, Maven, JDBC)
├── frontend/         # React 18 + Material UI
├── gateway/          # Node.js / Express API gateway
├── analytics/        # Python / Flask analytics service
├── docker/           # Dockerfiles + docker-compose.yml
├── k8s/              # Kubernetes / OpenShift manifests
├── scripts/db/       # SQL schemas + stored procedures (Oracle, MS SQL)
└── docs/             # Architecture guide + DevOps deployment guide
```

## CI/CD Pipeline

GitHub Actions runs on every push to `main`:

1. **Backend** — Maven build + JUnit tests
2. **Frontend** — `npm ci` + React build
3. **Analytics** — flake8 lint + pytest
4. **Docker** — Multi-image build and push to GitHub Container Registry
5. **Deploy** — OpenShift deploy via `oc` CLI (runs when `OC_TOKEN` secret is configured)

## Database

The default profile uses an H2 in-memory database seeded with sample tasks. To switch databases, set the Spring profile:

```bash
# Oracle
SPRING_PROFILES_ACTIVE=oracle

# MS SQL Server
SPRING_PROFILES_ACTIVE=mssql

# DB2
SPRING_PROFILES_ACTIVE=db2
```

SQL schemas and stored procedures for each database are in `scripts/db/`.
