#!/usr/bin/env bash
set -euo pipefail

# --- LOAD ENVIRONMENT VARIABLES ---
if [ -f .env.sh ]; then
  source .env.sh
fi

# --- CONFIGURATION (Required) ---
: "${PROJECT_ID:?PROJECT_ID is required. Set it in .env.sh}"
: "${REGION:?REGION is required. Set it in .env.sh}"
: "${DB_HOST:?DB_HOST is required. Set it in .env.sh}"
: "${DB_USER:?DB_USER is required. Set it in .env.sh}"
: "${DB_PASS:?DB_PASS is required. Set it in .env.sh}"
: "${DB_NAME:?DB_NAME is required. Set it in .env.sh}"
: "${JWT_SECRET:?JWT_SECRET is required. Set it in .env.sh}"

# --- SMTP CONFIG (Required) ---
: "${SMTP_HOST:?SMTP_HOST is required}"
: "${SMTP_PORT:?SMTP_PORT is required}"
: "${SMTP_USERNAME:?SMTP_USERNAME is required}"
: "${SMTP_PASSWORD:?SMTP_PASSWORD is required}"

# --- S3 CONFIG (Required) ---
: "${S3_ENDPOINT:?S3_ENDPOINT is required}"
: "${S3_ACCESS_KEY:?S3_ACCESS_KEY is required}"
: "${S3_SECRET_KEY:?S3_SECRET_KEY is required}"
: "${S3_BUCKET:?S3_BUCKET is required}"

SERVICE_NAME="every-club-be"
IMAGE="gcr.io/${PROJECT_ID}/${SERVICE_NAME}"

echo "🚀 Deploying $SERVICE_NAME to $REGION..."

# 1. Build & Push Image using Cloud Build
gcloud builds submit --tag "$IMAGE" --project "$PROJECT_ID"

# 2. Deploy to Cloud Run (1st Gen, Optimized for Free Tier)
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
  --set-env-vars "SPRING_PROFILES_ACTIVE=prod" \
  --set-env-vars "DATABASE_URL=jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}" \
  --set-env-vars "DATABASE_USERNAME=${DB_USER}" \
  --set-env-vars "DATABASE_PASSWORD=${DB_PASS}" \
  --set-env-vars "JWT_SECRET=${JWT_SECRET}" \
  --set-env-vars "SMTP_HOST=${SMTP_HOST}" \
  --set-env-vars "SMTP_PORT=${SMTP_PORT}" \
  --set-env-vars "SMTP_USERNAME=${SMTP_USERNAME}" \
  --set-env-vars "SMTP_PASSWORD=${SMTP_PASSWORD}" \
  --set-env-vars "S3_ENDPOINT=${S3_ENDPOINT}" \
  --set-env-vars "S3_ACCESS_KEY=${S3_ACCESS_KEY}" \
  --set-env-vars "S3_SECRET_KEY=${S3_SECRET_KEY}" \
  --set-env-vars "S3_BUCKET=${S3_BUCKET}" \
  --project "$PROJECT_ID"

echo "✨ Deployment Finished!"
