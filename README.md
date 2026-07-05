# Speed Tracker — Android

Versión nativa en Kotlin + Jetpack Compose de la app de iOS. Misma idea: velocidad
en tiempo real, indicador de GPS, cronómetro, historial de rutas con mapa del
trazado, y compartir por WhatsApp/Mensajes/etc.

## ⚠️ Aviso: generado por IA
Igual que la versión iOS, todo este código ha sido generado por Claude (Anthropic)
sin revisión de un desarrollador Android profesional. Compila y sigue el mismo
patrón de la app de iOS, pero antes de un uso serio o distribución a terceros,
revisa especialmente `LocationTrackingService.kt` y los permisos declarados en
`AndroidManifest.xml`.

## Por qué Android es mucho más simple que iOS aquí

- No hace falta ninguna cuenta de pago (Apple Developer Program).
- No hay AltStore, no hay caducidad de 7 días, no hay certificados que renovar.
- Un APK de "debug" se firma automáticamente con una clave de depuración que
  Gradle genera solo, y se instala directo en el móvil.

## Cómo compilarlo (gratis, desde GitHub, sin Android Studio)

1. Sube toda esta carpeta `SpeedTrackerAndroid/` a un repositorio de GitHub
   (puede ser el mismo que ya tienes para la versión iOS, o uno nuevo — a este
   workflow le da igual, solo mira dentro de `SpeedTrackerAndroid/`).
2. Asegúrate de que también subes la carpeta `.github/workflows/build-android.yml`
   que va dentro de `SpeedTrackerAndroid/.github/workflows/`.
3. En la pestaña **Actions** de tu repo, busca "Build Android APK" → **Run workflow**.
4. Tarda 3-5 minutos (los runners de Ubuntu son mucho más rápidos y ligeros que
   los de macOS que usa la versión iOS).
5. Al terminar, descarga el artefacto `SpeedTracker-debug-apk` → dentro está
   `app-debug.apk`.

## Cómo instalarlo en tu móvil Android

1. Pasa el `.apk` a tu teléfono (Google Drive, email, cable USB, lo que prefieras).
2. Ábrelo desde el propio teléfono. La primera vez Android pedirá activar
   **"Instalar apps de origen desconocido"** para la app que uses para abrirlo
   (Archivos, Chrome, Drive...) — actívalo solo para esa app si te lo pregunta.
3. Instala. Listo — no hay re-firmas semanales ni nada que renovar.
4. Al abrir la app, acepta los permisos de ubicación. Para que funcione con la
   pantalla bloqueada, cuando te lo pida elige **"Permitir todo el tiempo"**
   (en algunos Android aparece como una segunda pantalla después del permiso
   inicial "mientras se usa la app").

## Notas técnicas

- **Mapa del recorrido**: a diferencia de la versión iOS (que usa MapKit con
  mapas reales), aquí se dibuja un trazado esquemático sobre un lienzo en blanco
  (Canvas), sin mapa de fondo con calles. Esto evita tener que dar de alta una
  API key de Google Maps (que requiere cuenta de Google Cloud y, pasado cierto
  uso, puede no ser gratis). Si más adelante quieres el mapa real con calles,
  se puede añadir con `com.google.maps.android:maps-compose`, pero requiere esa
  API key.
- **Segundo plano**: se usa un *Foreground Service* con notificación persistente
  (obligatorio en Android para mantener el GPS activo con la app en background),
  visible mientras grabas una ruta.
- **Persistencia**: el historial se guarda en `SharedPreferences` como JSON
  (mismo enfoque que `UserDefaults` en la versión iOS).
- **minSdk 26** (Android 8.0+), cubre prácticamente cualquier móvil en uso hoy.
