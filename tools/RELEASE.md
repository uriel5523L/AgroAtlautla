# Publicar una nueva version de AgroAtlautla

1. Incrementar `versionCode` y `versionName` en `app/build.gradle.kts`.
2. Compilar:

   ```text
   gradlew.bat :app:assembleRelease
   ```

3. Copiar el APK firmado a `fdroid/repo/` con el nombre que usara el indice:

   ```text
   fdroid/repo/agroatlautla_<versioncode>.apk
   ```

4. Regenerar el indice del repositorio:

   ```text
   python tools/generate_index.py --apk fdroid/repo/agroatlautla_<versioncode>.apk --out fdroid/repo --repo-url "https://uriel5523l.github.io/AgroAtlautla/fdroid/repo" --repo-name "AgroAtlautla" --repo-desc "Repositorio F-Droid de AgroAtlautla: aplicacion de apoyo agricola para Atlautla, Estado de Mexico" --web "https://uriel5523l.github.io/AgroAtlautla/"
   ```

5. Firmar el indice con la llave del repositorio (la misma que firma el APK NO; esta es la llave F-Droid, guardada en `keystore/fdroid-repo.jks`, nunca subirla al repo):

   ```text
   jar --create --file fdroid/repo/index-v1.jar -C fdroid/repo index.xml
   jarsigner -keystore keystore/fdroid-repo.jks -storepass <repoPassword> -keypass <repoPassword> fdroid/repo/index-v1.jar fdroidrepo
   copy fdroid/repo/index-v1.jar fdroid/repo/index.jar
   ```

6. Actualizar la URL del APK en `index.html` y subir todo:

   ```text
   git add -A; git commit -m "vX.Y"; git push
   ```

GitHub Pages publica automaticamente. Los clientes F-Droid detectan la nueva version en el siguiente refresco.

## Notas

- `keystore/` y `keystore.properties` estan en `.gitignore`: nunca subir las llaves.
- La app usa Google Play Services (Firebase Auth/Firestore): en telefonos F-Droid sin GMS
  (LineageOS sin GApps, etc.) el login no funcionara.