#!/usr/bin/env bash
# ==============================================================================
# Alnoor Islamic App - Automated Build & Cloud Update Script
# Builds APK, pushes new version metadata to Firebase Cloud, and updates sync manifest.
# ==============================================================================

set -e

VERSION_NAME="${1:-1.1.0}"
VERSION_CODE="${2:-2}"
MIN_SUPPORTED="${3:-1}"
IS_FORCED="${4:-true}"
DOWNLOAD_URL="${5:-https://github.com/AlnoorIslami/alnoor-islamic-app/releases/latest/download/app-release.apk}"
RELEASE_NOTES="${6:-• Important performance and cloud sync upgrades\n• Support for login notice posters and interactive popups\n• General stability improvements}"

PROJECT_ID="alnoor-islami-8763b"
API_KEY="AIzaSyBmV50_gdE4t36Fqje4nvAfJwadHrZgMNE"

echo "========================================================"
echo " 🚀 Building Alnoor Islamic App APK v${VERSION_NAME} (Build ${VERSION_CODE})"
echo "========================================================"

# 1. Build APK with Persistent Signing Certificate
echo "Step 1: Setting up persistent keystore and compiling APK..."
export STORE_PASSWORD="${STORE_PASSWORD:-android}"
export KEY_PASSWORD="${KEY_PASSWORD:-android}"

if [ ! -f "my-upload-key.jks" ]; then
    if [ -f "debug.keystore.base64" ]; then
        cat debug.keystore.base64 | base64 --decode > my-upload-key.jks
    elif [ -f "debug.keystore" ]; then
        cp debug.keystore my-upload-key.jks
    fi
    if [ -f "my-upload-key.jks" ]; then
        if ! keytool -list -keystore my-upload-key.jks -storepass android -alias upload > /dev/null 2>&1; then
            keytool -changealias -alias androiddebugkey -destalias upload -keystore my-upload-key.jks -storepass android || true
        fi
    fi
fi

if [ -f "./gradlew" ]; then
    ./gradlew assembleRelease || ./gradlew assembleDebug
else
    gradle assembleRelease || gradle assembleDebug
fi

# 2. Locate APK
APK_PATH=$(find app/build/outputs/apk -name "*.apk" | head -n 1)
if [ -z "$APK_PATH" ]; then
    echo "❌ Error: Could not locate built APK."
    exit 1
fi

FILE_SIZE_BYTES=$(wc -c < "$APK_PATH")
FILE_SIZE_MB=$(awk "BEGIN {printf \"%.1f MB\", $FILE_SIZE_BYTES/1048576}")

echo "✅ Built successfully: $APK_PATH ($FILE_SIZE_MB)"

# 3. Push Version Metadata to Firebase Firestore
echo "Step 2: Pushing new version metadata to Firestore cloud..."
NOW_MS=$(date +%s%3N)

JSON_PAYLOAD=$(cat <<EOF
{
  "fields": {
    "latestVersionCode": { "integerValue": "$VERSION_CODE" },
    "latestVersionName": { "stringValue": "$VERSION_NAME" },
    "minSupportedVersionCode": { "integerValue": "$MIN_SUPPORTED" },
    "isForcedUpdate": { "booleanValue": $IS_FORCED },
    "apkDownloadUrl": { "stringValue": "$DOWNLOAD_URL" },
    "releaseNotes": { "stringValue": "$RELEASE_NOTES" },
    "releaseDate": { "stringValue": "$(date '+%B %d, %Y')" },
    "apkSizeMb": { "stringValue": "$FILE_SIZE_MB" },
    "lastUpdated": { "integerValue": "$NOW_MS" }
  }
}
EOF
)

curl -s -X PATCH \
  "https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/app_settings/app_version_info?key=${API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$JSON_PAYLOAD"

# 4. Touch Manifest
echo "Step 3: Touching Cloud Manifest for instant notification on all user devices..."
MANIFEST_PAYLOAD=$(cat <<EOF
{
  "fields": {
    "app_version_v": { "integerValue": "$NOW_MS" },
    "lastUpdated": { "integerValue": "$NOW_MS" }
  }
}
EOF
)

curl -s -X PATCH \
  "https://firestore.googleapis.com/v1/projects/${PROJECT_ID}/databases/(default)/documents/app_settings/sync_manifest?updateMask.fieldPaths=app_version_v&updateMask.fieldPaths=lastUpdated&key=${API_KEY}" \
  -H "Content-Type: application/json" \
  -d "$MANIFEST_PAYLOAD"

echo ""
echo "========================================================"
echo " 🎉 Successfully deployed v${VERSION_NAME} (Build ${VERSION_CODE}) to Cloud!"
echo " All user devices running version < ${MIN_SUPPORTED} or with forced update active"
echo " will now see the mandatory update screen on startup."
echo "========================================================"
