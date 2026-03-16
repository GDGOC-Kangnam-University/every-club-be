#!/usr/bin/env bash
set -euo pipefail

# --- LOAD ENVIRONMENT VARIABLES ---
if [ -f .env.sh ]; then
  source .env.sh
fi

# --- CONFIGURATION (Required) ---
: "${PROJECT_ID:?PROJECT_ID is required. Set it in .env.sh}"
: "${REGION:?REGION is required. Set it in .env.sh (e.g., us-west1)}"
: "${VM_NAME:?VM_NAME is required. Set it in .env.sh (e.g., every-club-db)}"
: "${REPO:?REPO is required. Set it in .env.sh (e.g., owner/repo)}"

ZONE="${REGION}-b"
POOL_NAME="github-pool"
PROVIDER_NAME="github-provider"
SA_NAME="github-actions-deployer"

echo "🚀 Starting Infrastructure Setup for $PROJECT_ID..."

# 1. Enable Required Services
echo "📦 Enabling GCP Services..."
gcloud services enable \
  compute.googleapis.com \
  iam.googleapis.com \
  iamcredentials.googleapis.com \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  --project="$PROJECT_ID"

# 2. Database VM Setup (PostgreSQL)
echo "🐘 Setting up Database VM ($VM_NAME)..."
cat << 'EOF' > startup-script.sh
#!/bin/bash
apt-get update
apt-get install -y postgresql postgresql-contrib
sudo -u postgres psql -c "CREATE USER everyclub WITH PASSWORD 'changeme';"
sudo -u postgres psql -c "CREATE DATABASE \"every-club\" OWNER everyclub;"
echo 'host all all 0.0.0.0/0 md5' >> /etc/postgresql/*/main/pg_hba.conf
sed -i "s/#listen_addresses.*/listen_addresses = '*' /" /etc/postgresql/*/main/postgresql.conf
systemctl restart postgresql
EOF

gcloud compute instances create "$VM_NAME" \
  --project="$PROJECT_ID" \
  --zone="$ZONE" \
  --machine-type=e2-micro \
  --image-family=debian-12 \
  --image-project=debian-cloud \
  --boot-disk-size=20GB \
  --tags=postgres-server \
  --metadata-from-file=startup-script=startup-script.sh 2>/dev/null || echo "✅ VM $VM_NAME already exists."

rm startup-script.sh

# 3. Firewall Rules
echo "🔥 Configuring Firewall..."
gcloud compute firewall-rules create allow-postgres-public \
  --project="$PROJECT_ID" \
  --direction=INGRESS \
  --action=ALLOW \
  --rules=tcp:5432 \
  --source-ranges=0.0.0.0/0 \
  --target-tags=postgres-server 2>/dev/null || echo "✅ Firewall rule already exists."

# 4. GitHub Actions (WIF) Setup
echo "🔐 Setting up GitHub Actions OIDC Auth..."
gcloud iam workload-identity-pools create "${POOL_NAME}" \
  --project="${PROJECT_ID}" --location="global" 2>/dev/null || echo "✅ Pool exists."

gcloud iam workload-identity-pools providers create-oidc "${PROVIDER_NAME}" \
  --project="${PROJECT_ID}" --location="global" \
  --workload-identity-pool="${POOL_NAME}" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.repository=assertion.repository" \
  --issuer-uri="https://token.actions.githubusercontent.com" 2>/dev/null || echo "✅ Provider exists."

gcloud iam service-accounts create "${SA_NAME}" --project="${PROJECT_ID}" 2>/dev/null || echo "✅ SA exists."

SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "🔑 Granting Permissions to SA..."
for ROLE in roles/run.admin roles/iam.serviceAccountUser roles/artifactregistry.writer roles/cloudbuild.builds.builder; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" --member="serviceAccount:${SA_EMAIL}" --role="$ROLE" --quiet >/dev/null
done

PROJECT_NUMBER=${PROJECT_NUMBER:-$(gcloud projects describe "$PROJECT_ID" --format='value(projectNumber)' 2>/dev/null | tr -d '\r' || echo "")}

if [ -z "$PROJECT_NUMBER" ]; then
  echo "❌ Error: Could not retrieve Project Number for ID: $PROJECT_ID"
  echo "Please ensure your PROJECT_ID is correct and you have permission to view it."
  exit 1
fi

echo "🔢 Project Number: $PROJECT_NUMBER"

gcloud iam service-accounts add-iam-policy-binding "${SA_EMAIL}" \
  --project="${PROJECT_ID}" \
  --role="roles/iam.workloadIdentityUser" \
  --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/${POOL_NAME}/attribute.repository/${REPO}" --quiet >/dev/null

echo "------------------------------------------------"
echo "✨ Setup Complete!"
echo "Add these secrets to GitHub Actions:"
echo ""
echo "GCP_PROJECT_ID: ${PROJECT_ID}"
echo "GCP_WIF_PROVIDER: projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/${POOL_NAME}/providers/${PROVIDER_NAME}"
echo "GCP_SA_EMAIL: ${SA_EMAIL}"
echo "------------------------------------------------"
