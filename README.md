# CALCULATOR — DIFFY 01

### A modern, scientific Android calculator

**Calculator — Diffy 01** is an Android calculator project focused on mathematical accuracy, a polished mobile experience, useful scientific functions, and a maintainable codebase.

The goal is to bring everyday arithmetic and scientific calculations together in one clear, reliable, offline-first application.

> **Project status:** Active development. The source code is in the repository. A successful APK build and real-device QA must be confirmed before describing a release as production-ready.

---

## Contents

- [Project vision](#project-vision)
- [Current capabilities](#current-capabilities)
- [Scientific functions](#scientific-functions)
- [Interface and experience](#interface-and-experience)
- [Calculation engine and accuracy](#calculation-engine-and-accuracy)
- [History and local data](#history-and-local-data)
- [Privacy and security](#privacy-and-security)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Build the Android APK](#build-the-android-apk)
- [Automated testing](#automated-testing)
- [Android compatibility](#android-compatibility)
- [Accessibility](#accessibility)
- [Development standards](#development-standards)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Release checklist](#release-checklist)
- [Developer and license](#developer-and-license)

---

## Project vision

Calculator is intended for students, teachers, professionals, and anyone who needs a practical calculator with scientific capabilities.

Our development priorities are:

1. **Accuracy:** mathematical operations should produce correct and predictable results.
2. **Reliability:** invalid input should be handled safely without crashing the app.
3. **Usability:** controls should be clear, readable, and comfortable to use.
4. **Performance:** calculations and interactions should feel responsive.
5. **Maintainability:** the project should remain organized as features grow.
6. **Privacy:** core calculations should work locally without requiring an account.

The application should be useful as a daily calculator while providing a foundation for more advanced mathematical tools.

---

## Current capabilities

The following features are represented in the current source implementation. They still require build and device testing before a release can be considered verified.

### Standard calculator

- Addition, subtraction, multiplication, and division
- Decimal number input
- Percentage operation
- Positive and negative values
- Parentheses
- Clear-all and backspace controls
- Expression display and calculated result
- Operator precedence

### Scientific calculator

- Sine, cosine, and tangent
- Inverse sine, cosine, and tangent
- Hyperbolic sine, cosine, and tangent
- Common logarithm (base 10)
- Natural logarithm
- Square root and cube root
- Powers and exponents
- Factorials
- Absolute value
- Exponential function
- Floor and ceiling functions
- Pi (π) and Euler's number (e)

### Angle settings

- Degree mode (DEG)
- Radian mode (RAD)
- Switch angle mode from the calculator interface

### Convenience features

- Standard and scientific keypad layouts
- Dark and light appearance toggle
- Calculation history stored locally
- Reuse an earlier expression from history
- Clear saved calculation history
- Responsive Compose-based screen layout
- Offline operation for core calculations

---

## Scientific functions

The expression engine currently recognizes these function names:

| Function | Meaning |
|---|---|
| `sin(x)` | Sine |
| `cos(x)` | Cosine |
| `tan(x)` | Tangent |
| `asin(x)` | Inverse sine |
| `acos(x)` | Inverse cosine |
| `atan(x)` | Inverse tangent |
| `sinh(x)` | Hyperbolic sine |
| `cosh(x)` | Hyperbolic cosine |
| `tanh(x)` | Hyperbolic tangent |
| `log(x)` | Base-10 logarithm |
| `ln(x)` | Natural logarithm |
| `sqrt(x)` | Square root |
| `cbrt(x)` | Cube root |
| `abs(x)` | Absolute value |
| `exp(x)` | Exponential function |
| `floor(x)` | Round down to an integer |
| `ceil(x)` | Round up to an integer |

Constants: `pi`, `π`, and `e`.

Examples of supported expression syntax:

```text
2 + 3 * 4
(2 + 3) * 4
sqrt(81)
sin(30)
log(100)
5!
2^8
50%
```

Trigonometric calculations use the selected DEG or RAD setting. Inverse trigonometric results are expressed in the selected angle unit.

---

## Interface and experience

The interface is built with Jetpack Compose and Material 3.

Design goals:

- Clean, modern calculator screen
- Clear expression and result display
- Large, comfortable touch controls
- Consistent spacing and typography
- Distinct styling for number and operation keys
- Dark and light appearances
- Scientific and standard layouts
- Support for different Android screen dimensions
- Appropriate behavior around system status and navigation bars

The interface will continue to be refined through actual testing on phones and emulators.

---

## Calculation engine and accuracy

Mathematical correctness is a core requirement.

The current calculation engine uses a dedicated recursive-descent parser. It does not pass user-entered expressions to a programming-language evaluator.

The parser is designed to handle:

- Arithmetic precedence
- Parenthesized expressions
- Unary positive and negative values
- Exponents
- Scientific function calls
- Constants
- Postfix factorial and percentage operations
- Invalid expression detection

### Error handling goals

The app should display understandable errors for cases such as:

- Division by zero
- Missing or unmatched parentheses
- Unknown function names
- Invalid numbers
- Invalid square-root input
- Undefined tangent values
- Invalid factorial input
- Results outside the supported numeric range

Floating-point calculations have finite precision. Results should be formatted clearly, and numerical edge cases should be covered by tests.

---

## History and local data

Calculation history is stored on the device using Android local preferences.

Current history behavior:

- Save completed expressions and results
- Display recent calculations
- Select an earlier calculation to reuse its expression
- Clear saved history
- Limit stored history to a reasonable number of recent entries

The app does not need a user account or cloud service for its core calculator functionality.

---

## Privacy and security

Privacy is a design priority.

- Core calculations run locally on the device.
- The application does not require an account.
- The current manifest does not request internet access.
- Calculation history is stored locally.
- Mathematical input is parsed as an expression, not executed as arbitrary application code.
- No analytics or advertising SDK is included in the current dependency list.

Any future feature that introduces networking, analytics, backups, or cloud synchronization must explain what data is used and obtain appropriate user consent.

---

## Technology stack

| Technology | Purpose |
|---|---|
| Kotlin | Main application language |
| Native Android | Mobile platform |
| Jetpack Compose | Declarative user interface |
| Material 3 | UI components and theming |
| Gradle Kotlin DSL | Project and build configuration |
| JUnit 4 | Unit testing |
| Android SharedPreferences | Local calculation history |

### Development environment

- Android Studio
- JDK 17
- Android SDK 35
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9

Use a compatible Android Studio installation and allow Gradle to download the required dependencies.

---

## Project structure

```text
calculator/
├── .github/
│   └── workflows/
│       └── android-ci.yml
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/diffy01/calculator/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── CalculatorEngine.kt
│   │   │   │   └── CalculationHistory.kt
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       └── values/
│   │   └── test/
│   │       └── java/com/diffy01/calculator/
│   │           └── CalculatorEngineTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .gitignore
├── LICENSE
└── README.md
```

The structure may evolve as the app is separated into additional modules.

---

## Getting started

1. Install Android Studio and JDK 17.
2. Clone or download this repository.
3. Open the project root in Android Studio.
4. Allow Gradle sync to complete.
5. Install an Android emulator or connect an Android device with developer debugging enabled.
6. Select the app run configuration.
7. Build and run the application.

Clone command:

```bash
git clone https://github.com/mojiolatunji34-cpu/calculator.git
cd calculator
```

---

## Build the Android APK

From the project root, run:

```bash
gradle assembleDebug
```

The debug APK should be generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To run the local unit tests:

```bash
gradle testDebugUnitTest
```

To run both checks:

```bash
gradle testDebugUnitTest assembleDebug
```

A debug APK is intended for development and testing. A distributable release requires a properly configured signing key and release build configuration. Keep signing keys and passwords private; never commit them to GitHub.

---

## Automated testing

The repository includes unit tests for core expression-engine behavior.

Testing should cover:

### Arithmetic

- Addition, subtraction, multiplication, and division
- Operator precedence
- Nested parentheses
- Decimal values
- Exponents

### Scientific mathematics

- Trigonometric functions in degrees
- Trigonometric functions in radians
- Logarithms
- Roots
- Constants
- Factorials
- Percentages

### Invalid input

- Division by zero
- Negative square roots
- Unmatched parentheses
- Invalid expressions
- Unsupported function names

### Android application checks

- App launch and installation
- Keypad interaction
- Scientific/standard switching
- DEG/RAD switching
- Theme switching
- History saving and clearing
- Portrait and landscape layouts
- Different screen sizes
- Reopening the app after closing it

---

## Android compatibility

The current Android configuration targets:

- Minimum SDK: Android 6.0 (API 23)
- Compile SDK: Android 15 (API 35)
- Target SDK: Android 15 (API 35)

Compatibility must be verified on supported Android versions and different device manufacturers. The actual release target SDK should be reviewed against current Google Play requirements before publishing.

---

## Accessibility

Accessibility improvements are part of the development plan:

- Readable text sizes
- Sufficient contrast
- Clear labels for calculator controls
- Comfortable touch targets
- Predictable focus and navigation
- Screen-reader support
- Important information not communicated by color alone

These requirements should be checked during UI testing, not only documented.

---

## Development standards

Every change should follow these principles:

1. Keep code modular and readable.
2. Use clear names for classes, functions, and variables.
3. Avoid duplicated calculation logic.
4. Validate user input before evaluating it.
5. Handle errors without crashing the app.
6. Add tests for new mathematical functionality.
7. Keep dependencies deliberate and maintained.
8. Preserve existing working features when adding new ones.
9. Review build and test results before merging changes.
10. Do not mark a feature complete until it has been implemented and tested.

---

## Roadmap

The roadmap distinguishes existing source functionality from features that have not yet been implemented.

### Phase 1 — Foundation

- [x] Native Android project configuration
- [x] Kotlin and Jetpack Compose setup
- [x] Main calculator screen
- [x] Standard arithmetic keypad
- [x] Scientific expression parser
- [x] DEG/RAD selection
- [x] Local calculation history
- [x] Dark/light appearance toggle
- [x] Initial unit tests
- [x] GitHub Actions build workflow configuration
- [ ] Confirm successful CI build
- [ ] Install and test APK on a real Android device

### Phase 2 — Scientific improvements

- [ ] Improve expression editing and cursor placement
- [ ] Add explicit multiplication and division display formatting
- [ ] Add fraction calculations
- [ ] Add scientific notation controls
- [ ] Add memory functions (MC, MR, M+, M−)
- [ ] Add configurable decimal precision
- [ ] Expand numerical edge-case tests

### Phase 3 — Advanced mathematics

- [ ] Statistics functions
- [ ] Permutations and combinations
- [ ] Greatest common divisor and least common multiple
- [ ] Matrix calculations
- [ ] Complex numbers
- [ ] Equation-solving tools
- [ ] Unit conversion tools

### Phase 4 — Visual tools

- [ ] Function graph plotting
- [ ] Coordinate and graph interaction
- [ ] Table of function values
- [ ] Improved landscape scientific layout

### Phase 5 — Product quality

- [ ] Refine application icon and launch experience
- [ ] Add UI and accessibility tests
- [ ] Test across multiple Android devices
- [ ] Profile performance and memory usage
- [ ] Configure signed release builds
- [ ] Prepare store listing materials
- [ ] Complete release review

---

## Contributing

Contributions, bug reports, and suggestions are welcome.

When reporting a bug, include:

- Android version and device model
- Steps to reproduce the issue
- The expression or operation involved, if relevant
- Expected behavior
- Actual behavior
- Screenshots or logs when useful

For code contributions:

1. Create a focused branch.
2. Make a clear, limited change.
3. Add or update tests.
4. Run the unit tests and Android build.
5. Submit a pull request describing the change.

Do not include passwords, signing keys, personal data, or private device information in issues or commits.

---

## Release checklist

A release is not complete until:

- [ ] Gradle sync succeeds.
- [ ] Debug or release APK builds successfully.
- [ ] Automated unit tests pass.
- [ ] APK installs on an Android device or emulator.
- [ ] Standard arithmetic is verified.
- [ ] Scientific functions are verified in DEG and RAD modes.
- [ ] History and settings behave correctly.
- [ ] No critical crashes remain.
- [ ] Layouts are checked on different screen sizes.
- [ ] Release signing is configured securely.
- [ ] Release notes accurately describe implemented features.

**A successful source commit is not the same as a successful APK build. A successful build is not the same as a tested release.**

---

## Developer and license

**Project:** Calculator  
**Developer / Project identity:** Diffy 01  
**Platform:** Android  
**Category:** Education / Productivity / Mathematics  
**Language:** Kotlin  
**Status:** Active development  
**License:** MIT

See the [MIT License](LICENSE) for details.

---

## Our development principle

**Build it correctly. Test it properly. Improve it carefully.**

Calculator — Diffy 01 aims to make advanced mathematics accessible through a reliable, attractive, and practical Android application.
