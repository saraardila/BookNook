# BookNook

App Android para seguir tus lecturas: búsqueda de libros en **Open Library**, biblioteca por estados (Reading, Finished, Want to Read, DNF, favoritos), progreso por páginas, valoraciones y reseñas, fechas de inicio/fin, calendario mensual, diario tipo scrapbook, notas por libro, compartir progreso y 4 temas (Botanical, Summer, Midnight, Blossom).

Autora: Sara (Android, Kotlin + Jetpack Compose). Está aprendiendo Kotlin Multiplatform con esta migración: explica el porqué de cada cambio, no solo el qué.

## Arquitectura actual

Un solo módulo `:app`, paquete `com.nawin.booknook`. Clean Architecture + MVVM:

- `domain/` — Kotlin puro (salvo `javax.inject.Inject`).
  - `model/`: `Book`, `BookNote`, `ReadingStatus`.
  - `repository/BookRepository`: interfaz (remoto: búsqueda; local: biblioteca y notas con `Flow`).
  - `usecase/`: un caso de uso por acción con `operator fun invoke` (`SearchBooks`, `AddBookToLibrary`, `GetMyLibrary`, `GetBookById`, `UpdateBook`, `AddNote`, `GetNotes`).
- `data/`
  - `remote/`: Retrofit + Gson contra `https://openlibrary.org/search.json` (`OpenLibraryApi`, DTOs con `@SerializedName`).
  - `local/`: Room (`AppDatabase` v1, `BookDao`, `BookNoteDao`, entidades) y `PreferencesDataStore` (tema, estilo y tinta del diario).
  - `repository/BookRepositoryImpl`: mapea DTO/entidad ↔ dominio.
- `di/`: módulos Hilt (`Database`, `Network`, `Preferences`, `Repository`), todos en `SingletonComponent`.
- `presentation/`
  - `ui/<pantalla>/`: `XScreen` + `XViewModel` (`@HiltViewModel`, `StateFlow<UiState>` con `stateIn(WhileSubscribed(5000))`). Pantallas: home, search, library, bookdetail (+ `ShareCard`), calendar, journal, settings, main.
  - `navigation/`: Navigation Compose con rutas string (`Screen`, `NavGraph`, `BottomNavItem`).
  - `components/`, `theme/` (temas dinámicos).

Puntos a tener en cuenta (no son bugs, pero afectan a la migración):
- `data/local/PreferencesDataStore` importa enums de `presentation.theme` (`BookTheme`, `JournalStyle`, `JournalInkColor`): `data` depende de `presentation`. Hay que moverlos antes o durante la Fase 3.
- `MainViewModel` usa `PreferencesDataStore` directamente, sin pasar por `domain`.

## Stack actual

Kotlin 2.3.21 (plugin `org.jetbrains.kotlin.plugin.compose`) · AGP 8.13.2 · Gradle 8.14.6 · KSP 2.3.12 · JDK toolchain 21 · Java/JVM target 17 · compileSdk 36 · targetSdk 35 · minSdk 26 · Compose BOM 2026.06.01 (Compose 1.11, Material3 1.4) · core-ktx 1.18 · activity 1.13 · lifecycle 2.10 · Navigation 2.9.8 · Hilt 2.58 (kapt) + hilt-navigation-compose 1.3 · Room 2.8.5 (KSP) · Retrofit 2.12 + Gson · OkHttp 4.12 · Coil 2.7 · DataStore 1.3 · coroutines 1.11 · Lottie 6.7.

Versiones en `gradle/libs.versions.toml`. Hay un `force("com.squareup:javapoet:1.13.0")` en `app/build.gradle.kts` (parche para Hilt/kapt; quitarlo al pasar a Koin si ya no hace falta).

Límites de versión mientras sigamos con Hilt y AGP 8:
- Hilt 2.59+ exige AGP 9, así que Hilt se queda en 2.58.
- Hilt 2.58 lee metadata de Kotlin hasta 2.3, así que Kotlin se queda en 2.3.x (Kotlin 2.4 llega con Koin en la Fase 2).
- Hilt sigue en kapt: su soporte KSP solo es estable desde Dagger 2.60. kapt desaparece al cambiar Hilt por Koin.
- Con AGP 8.13 (máximo API 36.1) no valen las librerías que exigen AGP 9.1 y compileSdk 37: Compose 1.12+ (BOM 2026.08+), core-ktx 1.19+, lifecycle-*-compose 2.11+, navigation 2.10+ (arrastra lifecycle 2.11) y hilt-navigation-compose 1.4+. Antes de subir una AndroidX, mirar `minAndroidGradlePluginVersion` y `minCompileSdk` en su `META-INF/com/android/build/gradle/aar-metadata.properties`.
- Plan: en la Fase 2, una vez quitado Hilt, subir a AGP 9 y a estas versiones.

## Plan de migración a Kotlin Multiplatform + Compose Multiplatform

Por fases. **Al terminar cada paso, la app compila y funciona.** Una rama por fase.

1. **Modernizar** (sin cambiar funcionalidad): ~~Gradle/AGP al día~~, ~~Kotlin 2.x + plugin de Compose~~, ~~Room a KSP~~, ~~Java target 17~~, ~~dependencias al día (dentro de los límites de AGP 8)~~, ~~quitar Accompanist~~.
2. **Librerías KMP dentro de `:app`**: Retrofit + Gson → Ktor + kotlinx.serialization; Hilt → Koin (y después AGP 9, Kotlin 2.4 y AndroidX al día); Room → Room KMP; Coil 2 → Coil 3; DataStore y Navigation en su versión multiplataforma.
3. **Módulo `shared`**: mover domain, data y UI a `commonMain`; `:app` queda como punto de entrada Android.
4. **Target iOS** (cuando haya acceso a un Mac).

### Arreglos pendientes (hacer en el momento adecuado)

- El README dice Google Books, pero el código usa Open Library.
- `HttpLoggingInterceptor` en nivel `BODY` también en release → solo en debug (con Ktor: plugin `Logging` solo en debug).
- `isMinifyEnabled = false` en release → activar R8 y revisar reglas (kotlinx.serialization, Room, Koin).
- No hay tests reales (solo los de ejemplo) → tests de los casos de uso.
- Room: `exportSchema = false`, sin migraciones y con `fallbackToDestructiveMigration()` (borra los datos del usuario al subir la versión) → exportar esquema, migraciones y quitar el fallback antes de publicar.
- `BookNoteEntity.bookId` es clave foránea sin índice (warning de Room) → añadir índice junto con la primera migración.
- Antes de publicar en Google Play: comprobar el `targetSdk` mínimo vigente.
- `MainActivity.kt:47`: el compilador avisa "Condition is always 'false'" → revisar si es código muerto o un bug.
- `BookDetailScreen.kt`: `Icons.Filled.ArrowBack` deprecado → `Icons.AutoMirrored.Filled.ArrowBack`.

## Reglas de trabajo

- Hablar en español, tono directo.
- Paso a paso: proponer **un** cambio, explicar qué es y por qué, esperar confirmación y entonces seguir.
- Corrección técnica antes que dar la razón: si una propuesta es incorrecta, decirlo con el motivo.
- Una rama por fase (`fase-1-modernizacion`, `fase-2-librerias-kmp`, …). Commits pequeños y descriptivos.
- Tras cada cambio: `./gradlew assembleDebug` y tests unitarios en verde antes de commitear.
- Nunca subir secretos: API keys, keystores (`*.jks`, `*.keystore`), `local.properties`, `google-services.json`, ficheros de firma.
- No tocar Cozy Habits (`habit-tracker-compose`) desde aquí.

## Comandos

```bash
./gradlew assembleDebug            # compilar APK debug
./gradlew testDebugUnitTest        # tests unitarios (JVM)
./gradlew connectedDebugAndroidTest # tests instrumentados (emulador/dispositivo)
./gradlew lint                     # lint de Android
./gradlew :app:dependencies --configuration debugRuntimeClasspath  # árbol de dependencias
```

En Windows/PowerShell: `.\gradlew.bat <tarea>`.
