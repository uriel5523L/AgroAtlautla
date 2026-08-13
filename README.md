# AgroAtlautla

Aplicacion Android nativa en Kotlin + Jetpack Compose para apoyo agricola en Atlautla.

## Caracteristicas

- Login, registro y recuperacion con Firebase Auth.
- Base global persistente con Firestore.
- Base local offline con Room.
- Sincronizacion asincrona con WorkManager.
- Pantallas del prototipo: inicio, cultivos, agregar/detalle de cultivo, calendario, plagas, detalle de plaga, gastos, reportes y perfil.

## APK Debug

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Descarga y F-Droid

- Pagina de descarga: https://uriel5523l.github.io/AgroAtlautla/
- Repositorio F-Droid (agregar en F-Droid > Ajustes > Repositorios > +):

```text
https://uriel5523l.github.io/AgroAtlautla/fdroid/repo
```

Para publicar una nueva version: incrementar `versionCode`/`versionName` en `app/build.gradle.kts`,
compilar `assembleRelease` y regenerar el indice con `tools/generate_index.py` (consulta `tools/RELEASE.md`).

## Configuracion Firebase

Consulta `FIREBASE_SETUP.md`.
