# Verified Android test build — 27 October 2026

Source commit: `1e18ef9eeedd7bbc57191d4f9085384e8e26a4f3`.

Successful workflow: [40584758699, attempt 2](https://github.com/mahirozcan63-crypto/klarmathe-android/actions/runs/40584758699/attempts/2).

APK: [KlarLernen.apk](https://github.com/mahirozcan63-crypto/klarmathe-android/releases/download/debug-40584758699-2/KlarLernen.apk), 77,405 bytes.

SHA-256: `2e706abfd1e0b20602bc92f0ae4f607f95dada10310b04d9d30f378192a219f2`.

## Actually executed

- 9 native SafeMath console checks passed.
- 11 JUnit tests passed with zero failures/errors. These check all 134 authored answer keys and the math sample steps, equivalent methods, negative values, decimals and fractions, rejection of a bad step even with the correct final answer, unsupported input, assisted retry, hints, duplicate submission prevention, saved draft restore, similar-question review, skipped exam accounting, adaptation and invalid backup rejection.
- Android lint, debug APK compilation and test APK compilation passed. Lint passing is not a statement that the app has no warnings.
- `apksigner verify --verbose` passed using APK Signature Scheme v2. `aapt dump badging` confirmed package `de.klarlernen.app`, version `0.2.0`, minimum SDK 26 and target SDK 36.
- The native UI instrumentation flow passed on an Android 16 emulator with a 1080 × 2400 pixel screen at 480 dpi (360 × 800 dp). It uses actual touch injection for the app buttons and the Android accessibility action for the initial dialog. The test checks profile setup, a six-question assessment, incorrect practice retry, a hint, the math keypad after redraw, activity restart draft persistence, assisted versus independent results, a different review question, progress, modern system back navigation and a five-question exam without help.
- Downloaded APK SHA-256 matches the published release asset digest. ZIP integrity passed and the bundled question catalog exactly matches the committed catalog.

## Not represented as complete

No physical phone testing, Android 8–15 device testing, formal accessibility audit, subject-expert content approval, full NRW curriculum verification or Play Store release review has been performed. The Android file picker for export/import, sharing sheet, optional timer and every settings combination were not covered by the phone smoke test.

The build has no live AI connection, cloud account, cross-device synchronization or payment. Its local prepared explanations are labeled accordingly. All 134 content items remain `human-pending`.

This is a debug device test build. A persistent production signing key and a store-ready release process remain to be configured. Export data before switching to a build with another debug signature.
