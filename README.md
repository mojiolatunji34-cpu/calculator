# Calculator — Diffy 01

A modern Android scientific calculator focused on mathematical accuracy, speed, accessibility, and a polished mobile experience.

## Vision
Build a dependable everyday and scientific calculator with a clear interface, a safe expression engine, useful history, and a maintainable Android codebase.

## Features in this first implementation
- Standard arithmetic: addition, subtraction, multiplication, division, percentages
- Scientific operations: sine, cosine, tangent, inverse trig, logarithms, natural logarithms, square root, square, powers, factorial, absolute value
- Constants: pi and Euler's number
- Degree and radian angle modes
- Parentheses and operator precedence
- Calculation history saved locally
- Dark and light appearance toggle
- Responsive Jetpack Compose interface
- Input validation and readable calculation errors
- Offline-first operation; no account or network permission required

## Technology
- Kotlin
- Native Android
- Jetpack Compose and Material 3
- Gradle Kotlin DSL
- JUnit for expression-engine tests

## Project structure
```
app/
  src/main/
    AndroidManifest.xml
    java/com/diffy01/calculator/
      MainActivity.kt
      CalculatorEngine.kt
      CalculationHistory.kt
    res/values/strings.xml
    res/values/themes.xml
  src/test/java/com/diffy01/calculator/
    CalculatorEngineTest.kt
build.gradle.kts
settings.gradle.kts
gradle.properties
```

## Build requirements
- Android Studio with a compatible Android SDK
- JDK 17
- Android Gradle Plugin 8.7.3
- Gradle 8.9 or newer compatible Gradle distribution

Open the repository as a project in Android Studio and allow Gradle sync to finish. Select the app run configuration to install it on an emulator or connected Android device.

## Build an APK
From the project root, run:

```bash
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

For a release, configure a private signing key in your local Gradle properties or Android Studio. Never commit signing keys, passwords, or local secrets.

## Quality principles
1. Mathematical functions must have automated tests.
2. Invalid input must show a useful error rather than crash.
3. The calculator must not evaluate expressions as executable code.
4. Existing working features must be preserved when adding new ones.
5. Test on multiple screen sizes and both portrait and landscape layouts before release.
6. Do not claim a release is tested until the APK has actually been installed and exercised on a device or emulator.

## Roadmap
- [x] Native Android project foundation
- [x] Standard and scientific expression engine
- [x] DEG/RAD mode
- [x] Local calculation history
- [x] Dark/light appearance
- [x] Automated expression-engine tests
- [ ] Fraction and unit-conversion tools
- [ ] Matrix and statistics modes
- [ ] Graph plotting and equation-solving tools
- [ ] Signed release build and device QA

## Privacy
Core calculations run locally. This project does not request internet access and does not transmit calculation history.

## Developer
**Diffy 01**

## License
MIT. See [LICENSE](LICENSE).
