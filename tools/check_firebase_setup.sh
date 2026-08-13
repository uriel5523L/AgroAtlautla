#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GOOGLE_SERVICES="$ROOT_DIR/app/google-services.json"

if [[ -f "$GOOGLE_SERVICES" ]]; then
  echo "OK: app/google-services.json existe."
else
  echo "FALTA: app/google-services.json"
  echo "Descargalo desde Firebase Console usando el paquete com.agroatlautla.app"
fi

FIREBASE_BIN=""
if command -v firebase >/dev/null 2>&1; then
  FIREBASE_BIN="$(command -v firebase)"
elif [[ -x "$HOME/.local/bin/firebase" ]]; then
  FIREBASE_BIN="$HOME/.local/bin/firebase"
fi

if [[ -n "$FIREBASE_BIN" ]]; then
  echo "OK: Firebase CLI instalado ($($FIREBASE_BIN --version))."
  LOGIN_OUTPUT="$($FIREBASE_BIN login:list 2>&1 || true)"
  if [[ "$LOGIN_OUTPUT" == *"No authorized accounts"* ]]; then
    echo "FALTA: inicia sesion con Firebase CLI usando: $FIREBASE_BIN login"
  else
    echo "OK: Firebase CLI tiene una cuenta autorizada."
  fi
else
  echo "FALTA: Firebase CLI no esta instalado. Es opcional si pegas reglas manualmente."
fi

echo "Proyecto Android: $ROOT_DIR"
