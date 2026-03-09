#  BookNook

A cozy Android app to track your reading journey — log books, write reviews, track progress and visualize your reading history.

<br>

## Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Material3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=material-design&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-E65100?style=for-the-badge&logo=google&logoColor=white)
![Room](https://img.shields.io/badge/Room-4CAF50?style=for-the-badge&logo=sqlite&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-48B983?style=for-the-badge&logo=square&logoColor=white)

<br>

##  Features

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

##  Screenshots

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/a6e96e27-980b-42fa-8e47-81179ac3f0c1" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/a049235d-93a6-45e4-8ea7-e366e6ab5e60" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/37aa5314-aeea-4b0a-81c9-096185c7887e" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/9119fc29-0d03-4511-9f1a-5c3f7680f505" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/5763c174-dbfc-4f9f-8828-d293bcff0e11" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/ebba3577-a02e-4da1-8054-8f5debd0a6a5" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/c85e62c9-a2a0-49e8-9446-3ba4379c2fab" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/e966a5fe-baba-41cd-b683-1375ac23a238" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/47d3bdf8-51b2-4092-9b6c-afd990e67c8b" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/0602d070-d861-4e8e-9fe0-5bdad716c2db" width="300"/></td>
  </tr>
</table>

<br>

##  Architecture

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

##  Setup

### Requirements
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 26+

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/saraadila/booknook.git
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
