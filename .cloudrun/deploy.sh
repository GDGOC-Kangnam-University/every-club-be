#!/usr/bin/env bash
set -euo pipefail

# --- LOAD ENVIRONMENT VARIABLES ---
if [ -f .env.sh ]; then
  source .env.sh
fi

# --- CONFIGURATION (Required) ---
: "${PROJECT_ID:?PROJECT_ID is required. Set it in .env.sh}"
: "${REGION:?REGION is required. Set it in .env.sh}"
: "${IMAGE:?IMAGE is required. Pass it from CI or set it in .env.sh}"

SERVICE_NAME="every-club-be"

echo "🚀 Deploying $SERVICE_NAME to $REGION..."

service_exists=false
service_json_file="$(mktemp)"
existing_profiles=""
if gcloud run services describe "$SERVICE_NAME" \
  --region "$REGION" \
  --project "$PROJECT_ID" \
  --format=json >"$service_json_file" 2>/dev/null; then
  service_exists=true
  export SERVICE_JSON_FILE="$service_json_file"
  existing_profiles="$(
    python3 - <<'PY'
import json
import os
from pathlib import Path

path = Path(os.environ["SERVICE_JSON_FILE"])
data = json.loads(path.read_text())
containers = (((data.get("spec") or {}).get("template") or {}).get("spec") or {}).get("containers") or []

for container in containers:
    for env in container.get("env") or []:
        if env.get("name") == "SPRING_PROFILES_ACTIVE":
            print(env.get("value", ""))
            raise SystemExit(0)
PY
  )"
fi

spring_profiles_active="cloud-run"
if [ -n "${existing_profiles}" ]; then
  case ",${existing_profiles}," in
    *,cloud-run,*) spring_profiles_active="${existing_profiles}" ;;
    *) spring_profiles_active="${existing_profiles},cloud-run" ;;
  esac
fi

if [ "$service_exists" = true ]; then
  gcloud run services update "$SERVICE_NAME" \
    --image "$IMAGE" \
    --region "$REGION" \
    --update-env-vars "SPRING_PROFILES_ACTIVE=${spring_profiles_active}" \
    --project "$PROJECT_ID"
else
  full_env_vars="SPRING_PROFILES_ACTIVE=${spring_profiles_active},DATABASE_URL=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME},DATABASE_USERNAME=${DB_USER},DATABASE_PASSWORD=${DB_PASS},JWT_SECRET=${JWT_SECRET},SMTP_HOST=${SMTP_HOST},SMTP_PORT=${SMTP_PORT},SMTP_USERNAME=${SMTP_USERNAME},SMTP_PASSWORD=${SMTP_PASSWORD},S3_ENDPOINT=${S3_ENDPOINT},S3_ACCESS_KEY=${S3_ACCESS_KEY},S3_SECRET_KEY=${S3_SECRET_KEY},S3_BUCKET=${S3_BUCKET}"

  gcloud run deploy "$SERVICE_NAME" \
    --image "$IMAGE" \
    --region "$REGION" \
    --platform managed \
    --allow-unauthenticated \
    --port 8080 \
    --cpu 0.25 \
    --memory 512Mi \
    --min-instances 0 \
    --max-instances 1 \
    --cpu-throttling \
    --set-env-vars "$full_env_vars" \
    --timeout 300 \
    --project "$PROJECT_ID"
fi

rm -f "$service_json_file"

echo "✨ Deployment Finished!"
