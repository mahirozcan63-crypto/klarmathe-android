# KlarLernen Android

Native Android learning prototype in Java. Eight foundation units and 134 original exercises, with German questions and optional Turkish explanations. Mathematics uses a bounded, exact rational expression parser; student input is never executed as code.

## Download the Android test build

[KlarLernen.apk](https://github.com/mahirozcan63-crypto/klarmathe-android/releases/download/debug-40584758699-2/KlarLernen.apk) · [Source and download checks](https://github.com/mahirozcan63-crypto/klarmathe-android/actions/runs/40584758699)

This build passed 9 native mathematics checks, 11 unit tests, Android lint and APK signature verification. It is a debug build for device testing. The main learning flow also passed on an Android 16 phone emulator at 360 × 800 dp, including the modern system back action. Physical device testing and untested settings/import/share flows remain separate acceptance work.

See [Turkish phone installation instructions](TELEFON-KURULUMU.md).

## Build and install

The **Android APK** GitHub Actions workflow runs on a standard public Linux runner. It runs the native mathematics checks, JUnit tests and Android lint, builds a debug APK, verifies its signature and publishes `KlarLernen.apk` and `SHA256.txt` in GitHub Releases. No GitHub Pages site, paid runner, Actions artifact storage or shared cache is used.

Requirements for a local build: JDK 17, Gradle 8.13, Android SDK platform 36 and Build-Tools 35.0.0. Run:

```sh
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The APK output is `app/build/outputs/apk/debug/app-debug.apk`. Android 8.0 (API 26) or later is required. Download the APK on the phone and open it from Downloads. This is a debug build for device testing, not a Play Store release. A new CI run may use a different debug signing key; export local progress before installing a differently signed build.

## Available locally

Guest profile, study goal, optional exam date, six-question startcheck, daily practice, written solution steps, staged hints, prepared explanations, similar-question review, 5/10-question tests, local progress/history, dark theme, data export/import and deletion. Drafts and progress are saved on the device. No Internet permission is requested.

There is no live AI provider, cloud account, cross-device sync, payment or subscription. Pro is an inactive, configurable test offer; no purchase can be made. The content is introductory and has not received human subject-expert review. It does not claim complete NRW curriculum or exam coverage. Legal and Play Store release review are still pending. Real phone acceptance testing must be performed before a production release.

Original exercise content is covered by `LICENSE-CONTENT.txt`. Build and test results must be checked in the actual workflow run; prepared tests alone are not evidence of passing.

