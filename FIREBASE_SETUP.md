# Configurar Firebase Para AgroAtlautla

La app ya esta preparada para usar cuentas globales con Firebase Auth y datos persistentes con Firestore.

## 1. Crear Proyecto

1. Entra a `https://console.firebase.google.com`.
2. Abre o crea el proyecto con ID `agroatlautla`.
3. Agrega una app Android con este paquete:

```text
com.agroatlautla.app
```

4. Descarga `google-services.json`.
5. Coloca el archivo aqui:

```text
/home/kev/AndroidStudioProjects/AgroAtlautla/app/google-services.json
```

## Flujo Recomendado Desde Terminal

Ya deje un script interactivo para terminar la conexion:

```bash
cd /home/kev/AndroidStudioProjects/AgroAtlautla
tools/connect_firebase.sh
```

Ese script hace lo siguiente:

1. Inicia sesion con Firebase CLI.
2. Crea `.firebaserc` con tu Project ID.
3. Puede intentar registrar la app Android y descargar `google-services.json`.
4. Puede subir las reglas Firestore.
5. Compila el APK con Firebase activo.

## 2. Activar Login

En Firebase Console:

1. Abre directamente `https://console.firebase.google.com/project/agroatlautla/authentication/providers`.
2. Entra a `Sign-in method`.
3. Activa `Email/Password`.

Si la consola muestra otro proyecto arriba a la izquierda, cambialo a `agroatlautla`; ese es el proyecto que usa `app/google-services.json`.

## 3. Activar Base De Datos Global

En Firebase Console:

1. Ve a `Firestore Database`.
2. Crea la base de datos.
3. Usa modo `Production`.
4. Selecciona una region cercana.

## 4. Reglas De Seguridad

Este proyecto incluye `firestore.rules`. Las reglas hacen que cada usuario solo pueda leer y escribir sus propios datos:

```text
users/{uid}
users/{uid}/crops/{cropId}
users/{uid}/calendar_activities/{activityId}
users/{uid}/pests/{pestId}
```

Para subir reglas con Firebase CLI:

```bash
~/.local/bin/firebase login
~/.local/bin/firebase use --add
~/.local/bin/firebase deploy --only firestore:rules
```

Si no usas Firebase CLI, copia el contenido de `firestore.rules` y pegalo en `Firestore Database > Rules`.

## 5. Probar En Dos Dispositivos

1. Instala la app en el dispositivo A.
2. Crea una cuenta con correo y contrasena.
3. Instala la app en el dispositivo B.
4. Inicia sesion con el mismo correo.
5. La cuenta debe existir porque ya estara guardada en Firebase Auth.

## Nota Importante

El primer inicio de sesion en un dispositivo nuevo requiere internet. Despues, Room mantiene una copia local para funcionar sin conexion.
