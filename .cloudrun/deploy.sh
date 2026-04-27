#!/usr/bin/env bash
set -euo pipefail

# --- LOAD ENVIRONMENT VARIABLES ---
if [ -f .env.sh ]; then
  source .env.sh
fi

# --- CONFIGURATION (Required) ---
: "${PROJECT_ID:?PROJECT_ID is required. Set it in .env.sh}"
: "${REGION:?REGION is required. Set it in .env.sh}"

SERVICE_NAME="every-club-be"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"

echo "🚀 Deploying $SERVICE_NAME to $REGION..."

# 1. Build & Push Image using Cloud Build
gcloud builds submit --tag "$IMAGE" --project "$PROJECT_ID"

service_exists=false
service_json_file="$(mktemp)"
existing_profiles=""
if gcloud run services describe "$SERVICE_NAME" \
  --region "$REGION" \
  --project "$PROJECT_ID" \
  --format=json >"$service_json_file" 2>/dev/null; then
  service_exists=true
  existing_profiles="$(
    python3 - <<'PY'
import json
from pathlib import Path

path = Path("'"$service_json_file"'")
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
  # 2. Update the existing service without touching the other env vars.
  gcloud run services update "$SERVICE_NAME" \
    --image "$IMAGE" \
    --region "$REGION" \
    --update-env-vars "SPRING_PROFILES_ACTIVE=${spring_profiles_active}" \
    --project "$PROJECT_ID"
else
  # 2. First deploy path for a missing service.
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
    --set-env-vars "SPRING_PROFILES_ACTIVE=${spring_profiles_active}" \
    --timeout 300 \
    --project "$PROJECT_ID"
fi

rm -f "$service_json_file"

echo "✨ Deployment Finished!"
