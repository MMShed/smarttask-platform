# Deployment Guide — DevOps Handoff

## Prerequisites

- Docker 24+, Docker Compose v2
- `oc` CLI (OpenShift) or `kubectl` (Kubernetes)
- Access to the target OpenShift cluster
- Secrets provisioned (see below)

---

## Environment Variables / Secrets

### Backend (`smarttask-secrets` Kubernetes Secret)

| Key                    | Description                              |
|------------------------|------------------------------------------|
| `anthropic-api-key`    | Anthropic API key for Claude             |
| `aws-access-key-id`    | AWS credential with Bedrock access       |
| `aws-secret-access-key`| AWS credential secret                    |
| `mssql-password`       | SQL Server SA password                   |

### Backend (`smarttask-config` ConfigMap)

| Key         | Example value   |
|-------------|-----------------|
| `aws-region`| `us-east-1`     |
| `mssql-host`| `db.internal`   |

---

## Local Development

```bash
# Copy and fill in credentials
cp .env.example .env

# Start all services
docker compose -f docker/docker-compose.yml up --build

# Access points
# Frontend:  http://localhost:3000
# Backend:   http://localhost:8080
# Gateway:   http://localhost:3001
# Analytics: http://localhost:5000
# H2 Console: http://localhost:8080/h2-console
```

---

## Database Setup (Production)

### MS SQL Server

```bash
# Run schema creation
sqlcmd -S $MSSQL_HOST -U sa -P $MSSQL_PASSWORD \
       -d SmartTask -i scripts/db/schema-mssql.sql

# Deploy stored procedures
sqlcmd -S $MSSQL_HOST -U sa -P $MSSQL_PASSWORD \
       -d SmartTask -i scripts/db/stored-procedures-mssql.sql
```

### Oracle

```bash
sqlplus $ORACLE_USER/$ORACLE_PASSWORD@$ORACLE_HOST:1521/$ORACLE_SERVICE \
    @scripts/db/schema-oracle.sql

sqlplus $ORACLE_USER/$ORACLE_PASSWORD@$ORACLE_HOST:1521/$ORACLE_SERVICE \
    @scripts/db/stored-procedures-oracle.sql
```

---

## OpenShift Deployment

```bash
# 1. Authenticate
oc login --token=$OC_TOKEN --server=$OC_SERVER

# 2. Create namespace
oc apply -f k8s/namespace.yaml

# 3. Create secrets
oc create secret generic smarttask-secrets \
  --from-literal=anthropic-api-key=$ANTHROPIC_API_KEY \
  --from-literal=aws-access-key-id=$AWS_ACCESS_KEY_ID \
  --from-literal=aws-secret-access-key=$AWS_SECRET_ACCESS_KEY \
  --from-literal=mssql-password=$MSSQL_PASSWORD \
  -n smarttask

# 4. Create configmap
oc create configmap smarttask-config \
  --from-literal=aws-region=us-east-1 \
  --from-literal=mssql-host=db.internal \
  -n smarttask

# 5. Deploy services
oc apply -f k8s/backend-deployment.yaml
oc apply -f k8s/gateway-deployment.yaml
oc apply -f k8s/ingress.yaml

# 6. Verify rollout
oc rollout status deployment/smarttask-backend -n smarttask
oc rollout status deployment/smarttask-gateway -n smarttask

# 7. Check pod health
oc get pods -n smarttask
oc logs deployment/smarttask-backend -n smarttask
```

---

## Health Checks

| Endpoint                                  | Expected             |
|-------------------------------------------|----------------------|
| `GET /actuator/health`                    | `{"status":"UP"}`    |
| `GET /health` (gateway)                   | `{"status":"UP"}`    |
| `GET /health` (analytics)                 | `{"status":"UP"}`    |

---

## Rollback

```bash
# Roll back backend to previous revision
oc rollout undo deployment/smarttask-backend -n smarttask

# Or pin to a specific image tag
oc set image deployment/smarttask-backend \
    backend=ghcr.io/org/smarttask-backend:<previous-sha> -n smarttask
```

---

## Scheduled Maintenance Jobs

| Job                         | Trigger                 | Description                                |
|-----------------------------|-------------------------|--------------------------------------------|
| `sp_auto_close_stale_tasks` | SQL Agent / cron weekly | Closes OPEN tasks older than 30 days       |
| Task enrichment scheduler   | Spring `@Scheduled`     | Adds AI suggestions to un-triaged tasks    |
