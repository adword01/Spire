# Spire - Product Catalog & Offline Cart

An Android application built with Kotlin and Jetpack Compose that allows users to browse a product catalog, search for items, view details, and manage a shopping cart. The app features a robust offline-first architecture, ensuring the shopping cart remains fully functional and persistent even without an internet connection.

## Setup & Build Instructions

1. **Prerequisites:** 
   - Android Studio (Rabbit or newer recommended).
   - JDK 17 (or newer).
2. **Clone & Open:** Open the project directory in Android Studio.
3. **Gradle Sync:** The project uses Kotlin 2.2.10 and KSP. Wait for the initial Gradle sync to complete. 
   - *Note:* If you encounter a Kotlin Source Set error during build, ensure `android.disallowKotlinSourceSets=false` is present in your `gradle.properties` file.
4. **Run:** Select an emulator or physical device (API 30+) and click **Run** (`Shift + F10`). No API keys or accounts are required, as the app connects directly to the public DummyJSON API.

## Architecture Used

The application strictly follows the **MVVM (Model-View-ViewModel)** architectural pattern combined with a **Unidirectional Data Flow (UDF)**.

*   **UI Layer (Compose):** Stateless composable screens observe UI state from `StateFlow`s exposed by the ViewModels. All user actions (clicks, text input) are routed up to the ViewModel.
*   **Domain/ViewModel Layer:** `ProductViewModel` and `CartViewModel` act as state holders. They manage background coroutines, handle debouncing for search inputs, and map raw data into UI-friendly states (e.g., handling Loading, Success, and Error states).
*   **Data/Repository Layer:** 
    *   `ProductRepository` handles network requests and safely catches exceptions (like `SocketTimeoutException` or `IOException`) to return structured `Resource` wrappers.
    *   `CartRepository` acts as the **Single Source of Truth (SSOT)** for cart data, mediating between the ViewModel and the Room DAO.

## Libraries Used

*   **Jetpack Compose:** For building a declarative, modern, and responsive UI.
*   **Navigation Compose:** For type-safe routing between the List, Details, and Cart screens.
*   **ViewModel & Coroutines/Flow:** For asynchronous programming, background threading, and lifecycle-aware state management.
*   **Room (SQLite):** Local database implementation using KSP for annotation processing.
*   **Retrofit2 & OkHttp3:** For REST API network requests, JSON parsing (via Gson), and strict timeout management.
*   **Coil:** For asynchronous image loading, memory caching, and disk caching (allowing images to load offline).

## Local Storage Approach

The shopping cart relies on an **Offline-First / Single Source of Truth (SSOT)** approach powered by **Room Database**.

1.  **Reactive Streams:** The UI does not hold a static copy of the cart. Instead, `CartViewModel` exposes a continuous `StateFlow` directly mapped from `cartDao.getAllCartItems()`. 
2.  **Write-Only Operations:** When a user clicks "Add to Cart", "+", "-", or "Remove", the app performs a fire-and-forget write operation directly to the local Room database on the IO thread. 
3.  **Instant UI Updates:** Because the UI observes the database flow, any write operation instantly triggers a UI recomposition. This means the cart logic, quantity adjustments, and price calculations work flawlessly with zero network dependency.
4.  **Persistence:** Cart data is saved natively to the device's disk via SQLite, ensuring it survives app kills and device reboots.

## Important Design Decisions

*   **Debounced Search:** To prevent network spam and UI flickering, the search bar implementation utilizes Coroutine Jobs. It waits for a 500ms pause in user typing before dispatching the API request.
*   **Graceful Error Handling:** Network drops, timeouts, and API 404s are caught explicitly in the repository and mapped to localized UI states. The app displays specific error icons (e.g., a crossed-out Wi-Fi symbol for offline drops) and provides a clear "Retry" mechanism without crashing.
*   **Sticky Call-To-Action (CTA):** The "Add to Cart" button on the Details screen is decoupled from the scrollable column and placed in the Scaffold's `bottomBar`. This ensures the primary user action is always visible, regardless of screen size or description length.
*   **Kotlin Code Generation for Room:** The project forces Room to generate `.kt` files instead of `.java` files via KSP (`arg("room.generateKotlin", "true")`). This bypasses JVM signature clashes that occur when mixing Java-generated DAOs with modern Kotlin 2.x Coroutine Continuations.
