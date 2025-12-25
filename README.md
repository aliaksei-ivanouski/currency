This is a Kotlin Multiplatform project targeting Android and iOS.

* `/composeApp` houses the shared multiplatform library (Compose UI, data, DI, etc.).  
  - `commonMain` contains code used by every target.  
  - `androidMain`, `iosMain`, etc. contain actual implementations or platform resources.  
  This module now builds as an Android library (`com.fetocan.currency.shared`) that both the Android app
  and the iOS framework consume.

* `/androidApp` is the Android entry point module that applies `com.android.application`, depends on
  `:composeApp`, and contains the manifest, launcher icons, and `MainActivity`.

* `/iosApp` contains the iOS application host. Even when using Compose Multiplatform for UI, the project
  still needs this Swift/Objective‑C entry point.

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

## Configuration

This project expects a CurrencyAPI key to be supplied per developer machine:

- Create or update `local.properties` (not checked in) with `currencyApiKey=YOUR_KEY`.
- Open `iosApp/Configuration/Config.xcconfig` and set `CURRENCY_API_KEY=YOUR_KEY` (Xcode reads this value into `Info.plist`).

Both Android and iOS builds will fail fast if the key is missing so secrets never live inside the repository.

## Building

- Android: `./gradlew androidApp:assembleDebug`
- Shared library checks: `./gradlew composeApp:check`
- iOS framework (for Xcode integration): `./gradlew composeApp:linkReleaseFrameworkIosArm64`
