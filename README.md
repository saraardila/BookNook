# 📚 BookNook

> *your reading companion 🌿*

A cozy Android app to track your reading journey — log books, write reviews, track progress and visualize your reading history.

<br>

## 🛠 Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Material3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=material-design&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-E65100?style=for-the-badge&logo=google&logoColor=white)
![Room](https://img.shields.io/badge/Room-4CAF50?style=for-the-badge&logo=sqlite&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)

<br>

## ✨ Features

| Feature | Description |
|---|---|
|  **Book Search** | Search millions of books via Google Books API |
|  **Library** | Manage your books by status — Reading, Finished, Want to Read, DNF |
|  **Reading Progress** | Track current page and visualize progress |
|  **Ratings & Reviews** | Rate books and write personal reviews |
|  **Calendar** | Visualize your reading history by month |
|  **Journal** | Scrapbook-style view of all finished books |
|  **Reading Dates** | Log start and finish dates for every book |
|  **Dynamic Themes** | 4 themes — Botanical, Summer, Midnight, Blossom |
|  **Share Progress** | Share your reading progress with friends |
|  **Favorites** | Mark your favorite books |

<br>

## 🏗 Architecture

BookNook follows **Clean Architecture** with **MVVM** pattern, organized in 3 layers:

```
app/
├── data/
│   ├── local/          # Room database, DAOs, entities
│   ├── remote/         # Retrofit API, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Business models (Book, ReadingStatus...)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
└── presentation/
    ├── components/     # Reusable Compose components
    ├── navigation/     # NavGraph, routes
    ├── theme/          # Colors, Typography, Theming
    └── ui/
        ├── home/
        ├── search/
        ├── library/
        ├── bookdetail/
        ├── calendar/
        ├── journal/
        └── settings/
```

### Key patterns
- **StateFlow** + `collectAsStateWithLifecycle` for UI state
- **Hilt** for dependency injection across all layers
- **Repository pattern** abstracting data sources
- **Use Cases** encapsulating business logic

<br>

## 📸 Screenshots

> *Coming soon — app in active development*

| Home | Library | Calendar | Journal |
|------|---------|----------|---------|
| 🏠 | 📚 | 📅 | 📓 |

<br>

## 🚀 Setup

### Requirements
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 26+

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/yourusername/booknook.git
   cd booknook
   ```

2. **Open in Android Studio**
   ```
   File → Open → select the project folder
   ```

3. **Sync Gradle**
   ```
   File → Sync Project with Gradle Files
   ```

4. **Run the app**
   ```
   Run → Run 'app' (Shift + F10)
   ```

> No API keys required — Google Books API is used without authentication for basic search.

<br>

## Dependencies

```kotlin
// UI
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("io.coil-kt:coil-compose")

// Navigation
implementation("androidx.navigation:navigation-compose")

// DI
implementation("com.google.dagger:hilt-android")
implementation("androidx.hilt:hilt-navigation-compose")

// Data
implementation("androidx.room:room-runtime")
implementation("androidx.datastore:datastore-preferences")
implementation("com.squareup.retrofit2:retrofit")

// Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.lifecycle:lifecycle-runtime-compose")

// Splash
implementation("androidx.core:core-splashscreen:1.0.1")
```

<br>

##  Roadmap

- [x] Book search via Google Books API
- [x] Library management with status filters
- [x] Reading progress tracking
- [x] Ratings & reviews
- [x] Reading dates (start / finish)
- [x] Calendar view
- [x] Journal / scrapbook view
- [x] Share reading progress
- [x] Dynamic themes
- [x] Splash screen
- [ ] Reading statistics & yearly wrap
- [ ] Reading challenges & goals
- [ ] Book recommendations
- [ ] Widget for home screen
- [ ] Backup & export data

<br>

##  Sara Ardila

Built with 🍵 and lots of books.

---

*BookNook — because every book deserves to be remembered.*
