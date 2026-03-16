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

ZONE="${REGION}-b"
SA_NAME="github-actions-deployer"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "🚀 Starting Infrastructure Setup for $PROJECT_ID..."

# 1. Enable Required Services
echo "📦 Enabling GCP Services..."
gcloud services enable \
  compute.googleapis.com \
  iam.googleapis.com \
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

# 4. Service Account for GitHub Actions
echo "🔐 Setting up GitHub Actions Service Account..."
gcloud iam service-accounts create "${SA_NAME}" \
  --project="${PROJECT_ID}" \
  --display-name="GitHub Actions Deployment SA" 2>/dev/null || echo "✅ SA already exists."

echo "🔑 Granting Permissions..."
for ROLE in roles/run.admin roles/iam.serviceAccountUser roles/cloudbuild.builds.builder roles/storage.admin; do
  gcloud projects add-iam-policy-binding "${PROJECT_ID}" \
    --member="serviceAccount:${SA_EMAIL}" --role="$ROLE" --quiet >/dev/null
done

# 5. Generate SA Key
echo "🔑 Generating Service Account Key..."
KEY_FILE="sa-key.json"
gcloud iam service-accounts keys create "$KEY_FILE" \
  --iam-account="${SA_EMAIL}" --project="${PROJECT_ID}"

echo "------------------------------------------------"
echo "✨ Setup Complete!"
echo ""
echo "📋 Add this GitHub Secret:"
echo "  GCP_SA_KEY = (paste the entire contents of $KEY_FILE)"
echo ""
echo "⚠️  Then DELETE $KEY_FILE from your machine!"
echo "  rm $KEY_FILE"
echo "------------------------------------------------"
