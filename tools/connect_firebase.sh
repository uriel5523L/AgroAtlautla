#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PACKAGE_NAME="com.agroatlautla.app"
GOOGLE_SERVICES="$ROOT_DIR/app/google-services.json"

find_firebase() {
  if command -v firebase >/dev/null 2>&1; then
    command -v firebase
  elif [[ -x "$HOME/.local/bin/firebase" ]]; then
    printf '%s\n' "$HOME/.local/bin/firebase"
  else
    printf '%s\n' ""
  fi
}

FIREBASE_BIN="$(find_firebase)"

if [[ -z "$FIREBASE_BIN" ]]; then
  echo "Firebase CLI no esta instalado."
  echo "Instalalo con: npm install --prefix \"$HOME/.local\" firebase-tools"
  exit 1
fi

echo "Firebase CLI: $FIREBASE_BIN ($($FIREBASE_BIN --version))"

login_output="$($FIREBASE_BIN login:list 2>&1 || true)"
if [[ "$login_output" == *"No authorized accounts"* ]]; then
  echo "No hay sesion de Google activa en Firebase CLI."
  echo "Se abrira el flujo de login. Inicia sesion con la cuenta donde crearas AgroAtlautla."
  "$FIREBASE_BIN" login
else
  echo "Firebase CLI ya tiene una cuenta autorizada."
fi

echo
echo "Necesito el Project ID de Firebase."
echo "Ejemplo: agroatlautla-12345"
read -r -p "Project ID: " PROJECT_ID

if [[ -z "$PROJECT_ID" ]]; then
  echo "Project ID vacio. Cancelo."
  exit 1
fi

cat > "$ROOT_DIR/.firebaserc" <<EOF
{
  "projects": {
    "default": "$PROJECT_ID"
  }
}
EOF

echo "Archivo .firebaserc creado para el proyecto: $PROJECT_ID"

echo
read -r -p "Quieres intentar crear/descargar google-services.json desde Firebase CLI? [s/N]: " DOWNLOAD_JSON

if [[ "$DOWNLOAD_JSON" == "s" || "$DOWNLOAD_JSON" == "S" ]]; then
  echo "Creando app Android en Firebase si es necesario..."
  echo "Si Firebase dice que ya existe, no pasa nada; despues puedes usar el App ID existente."
  "$FIREBASE_BIN" apps:create ANDROID "AgroAtlautla Android" \
    --package-name "$PACKAGE_NAME" \
    --project "$PROJECT_ID" || true

  echo
  echo "Apps registradas en Firebase:"
  "$FIREBASE_BIN" apps:list --project "$PROJECT_ID" || true
  echo
  echo "Copia el App ID de la app Android. Suele empezar con algo como 1:...:android:..."
  read -r -p "Firebase Android App ID: " APP_ID

  if [[ -n "$APP_ID" ]]; then
    "$FIREBASE_BIN" apps:sdkconfig ANDROID "$APP_ID" --project "$PROJECT_ID" > "$GOOGLE_SERVICES"
    echo "Archivo descargado: $GOOGLE_SERVICES"
  else
    echo "No se ingreso App ID. Debes descargar google-services.json manualmente."
  fi
fi

if [[ ! -f "$GOOGLE_SERVICES" ]]; then
  echo
  echo "Aun falta: $GOOGLE_SERVICES"
  echo "Descargalo desde Firebase Console > Project settings > Android app ($PACKAGE_NAME)."
  echo "Luego vuelve a ejecutar este script o compila con ./gradlew :app:assembleDebug."
  exit 1
fi

echo
read -r -p "Quieres subir las reglas Firestore ahora? [s/N]: " DEPLOY_RULES
if [[ "$DEPLOY_RULES" == "s" || "$DEPLOY_RULES" == "S" ]]; then
  "$FIREBASE_BIN" deploy --only firestore:rules --project "$PROJECT_ID"
fi

echo
echo "Compilando app con Firebase activo..."
cd "$ROOT_DIR"
./gradlew :app:assembleDebug

echo
echo "Listo. APK:"
echo "$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
