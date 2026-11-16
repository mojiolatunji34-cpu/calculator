# CALCULATOR — DIFFY 01

### A Powerful, Modern Scientific Calculator for Android

A feature-rich scientific calculator project designed to bring reliable everyday arithmetic and advanced mathematical tools into one clean, responsive Android application.

**Project:** Calculator  
**Developer / Project identity:** Diffy 01  
**Platform:** Android  
**Category:** Productivity · Education · Mathematics  
**Status:** Active development  
**License:** MIT

---

## 1. Project vision

Calculator aims to combine the simplicity of a standard calculator with the capabilities of a scientific calculator.

The project is being developed around six priorities:

1. **Mathematical accuracy** — expressions must follow established mathematical rules.
2. **Speed and responsiveness** — calculations and controls should feel immediate.
3. **Excellent usability** — the interface should be clear on small and large phone screens.
4. **Reliability** — invalid input should be handled safely without crashing the app.
5. **Privacy** — core calculations should work offline and calculation history should remain on the device.
6. **Maintainability** — the codebase should be modular, documented, and testable.

This README is the product specification and development guide. A feature described as planned is not considered complete until it has been implemented and tested.

---

## 2. Product goals

The finished application should:

- Provide a dependable standard calculator for daily use.
- Offer scientific functions for school, university, technical, and professional calculations.
- Evaluate complete mathematical expressions with correct operator precedence.
- Support degrees and radians for angle-based functions.
- Keep a useful, locally stored calculation history.
- Offer comfortable light and dark appearances.
- Adapt to different Android screen sizes and orientations.
- Work without an internet connection for its core functions.
- Be tested before any release is described as stable.

---

## 3. Feature specification

### 3.1 Standard calculator

Core arithmetic and input features:

- Addition
- Subtraction
- Multiplication
- Division
- Decimal numbers
- Percentage calculations
- Positive and negative values
- Parentheses and grouped expressions
- Clear-all and backspace controls
- Repeated and chained calculations
- Sensible result formatting
- Scientific notation for very large or very small values

### 3.2 Scientific calculator

Scientific functions planned for the complete application:

- Sine: `sin`
- Cosine: `cos`
- Tangent: `tan`
- Inverse trigonometric functions: `asin`, `acos`, `atan`
- Hyperbolic functions: `sinh`, `cosh`, `tanh`
- Common logarithm: `log`
- Natural logarithm: `ln`
- Exponential function: `exp`
- Square root
- Cube root
- General powers and roots
- Factorials
- Absolute value
- Floor and ceiling
- Mathematical constants pi (`π`) and Euler's number (`e`)

### 3.3 Advanced mathematics — planned

The following are future development targets and are not represented as completed functionality:

- Fraction calculations and fraction/decimal conversion
- Scientific notation controls
- Modulo and remainder operations
- Permutations and combinations
- Greatest common divisor (GCD)
- Least common multiple (LCM)
- Complex-number calculations
- Matrix calculations
- Statistics functions
- Equation-solving tools
- Graph plotting
- Unit conversion utilities

Each advanced module will be added only after its mathematical behavior and user interface can be tested properly.

### 3.4 Angle modes

- Degree mode (DEG)
- Radian mode (RAD)
- A clear on-screen indicator of the active mode
- Correct conversion and evaluation for trigonometric functions

### 3.5 Calculation history

History requirements:

- Save successful calculations locally.
- Show both the original expression and its result.
- Allow a previous calculation to be reused.
- Allow users to clear stored history.
- Preserve history across normal application restarts.
- Handle empty or damaged history data safely.

### 3.6 Appearance and usability

Design goals:

- Modern, uncluttered calculator layout.
- Clear expression and result display.
- Large, comfortable touch controls.
- Consistent spacing, typography, and button styling.
- Light and dark themes.
- Clear visual distinction between number, operator, and action keys.
- Smooth but restrained interaction feedback.
- Responsive layouts for different Android screen sizes.
- Support for portrait and landscape use.

### 3.7 Accessibility

Accessibility goals:

- Readable text and sufficient contrast.
- Descriptive labels for interactive controls.
- Comfortable touch targets.
- Clear focus and interaction states.
- Consistent navigation.
- Important information must not be communicated by color alone.
- Support for Android accessibility services where practical.

---

## 4. Calculation engine and mathematical correctness

The calculation engine is a core part of the application and must be treated as a separate, testable module.

It should provide:

- Correct operator precedence.
- Correct handling of nested parentheses.
- Unary plus and minus.
- Decimal and scientific-notation parsing.
- Correct function argument handling.
- Correct degree/radian behavior.
- Defined rules for percentages and factorials.
- Consistent numerical formatting.
- Clear errors for invalid or undefined expressions.

### Error handling

The app should safely handle cases such as:

- Division by zero.
- Missing or unmatched parentheses.
- Incomplete expressions.
- Invalid characters.
- Unknown functions.
- Invalid function arguments.
- Negative square-root inputs in the real-number calculator.
- Logarithms of zero or negative values.
- Factorials outside the supported integer range.
- Overflow, underflow, and non-finite results.

The application must never crash because a user enters an invalid expression.

### Safe expression processing

User-entered mathematical expressions must be parsed by a dedicated mathematical parser. They must never be passed to a general-purpose code evaluator, shell, or executable-code engine.

Numerical precision and rounding behavior must be documented and tested. Floating-point results should not be presented with misleading precision.

---

## 5. User interface and Android experience

The interface is being built with native Android technologies and Jetpack Compose.

Design and interaction standards:

- Keep the expression display readable when expressions become long.
- Make important controls easy to reach.
- Prevent buttons from becoming too small on compact devices.
- Ensure the result display does not overlap the keypad.
- Support vertical scrolling or an adaptive layout where screen height is limited.
- Preserve useful application state across normal configuration changes where appropriate.
- Keep animations lightweight and avoid unnecessary visual effects.

The final app should feel like a polished Android application, not simply a web page packaged inside an APK.

---

## 6. Technology stack

Current project technology:

- **Kotlin** — primary application language.
- **Native Android** — application platform.
- **Jetpack Compose** — declarative user interface.
- **Material 3** — interface components and theming.
- **Gradle Kotlin DSL** — build configuration.
- **JUnit** — local unit testing.

The project uses a modular separation between the user interface, mathematical engine, and local history storage. New features should follow the same separation.

---

## 7. Project structure

Current and intended source organization:

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

As the application grows, additional packages should be introduced for scientific functions, settings, accessibility, and advanced calculation modes rather than placing all logic in one large source file.

---

## 8. Android compatibility and build requirements

Development environment:

- Android Studio.
- JDK 17.
- Android Gradle Plugin 8.7.3.
- Gradle 8.9.
- Android SDK Platform 35 for the current build configuration.
- Minimum supported Android API level: 23, subject to compatibility testing.

The minimum and target SDK values should be reviewed as Android platform requirements evolve.

### Open and run the project

1. Clone or download this repository.
2. Open the project folder in Android Studio.
3. Allow Gradle synchronization to complete.
4. Install the required Android SDK components if prompted.
5. Start an Android emulator or connect an Android device with USB debugging enabled.
6. Select the app run configuration and run the application.

### Build a debug APK

From the project root, with a compatible JDK and Android SDK installed:

```bash
gradle assembleDebug
```

The expected output location is:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The APK should be installed and tested before it is distributed.

### Release build

A production release must use a properly configured private signing key. Keep signing keys, passwords, API secrets, and local machine configuration out of Git.

A release build is not considered ready until it has been built, signed, installed, and tested.

---

## 9. Testing and quality assurance

Testing is required for every important calculation feature and release.

### Mathematical tests

Test cases should cover:

- Addition, subtraction, multiplication, and division.
- Operator precedence.
- Nested parentheses.
- Decimal arithmetic.
- Negative values and unary operators.
- Powers and roots.
- Trigonometric functions in degrees.
- Trigonometric functions in radians.
- Logarithms and natural logarithms.
- Factorials and percentages.
- Constants.
- Invalid expressions and invalid function arguments.
- Very large and very small results.

### Android application tests

Test the application for:

- Successful installation and startup.
- Standard and scientific key interactions.
- Correct display of long expressions and results.
- History saving, loading, reuse, and clearing.
- Theme switching.
- Portrait and landscape layouts.
- Compact and large screen sizes.
- App backgrounding and resuming.
- Invalid input without application crashes.
- Offline operation of core calculations.

### Release acceptance criteria

A release may be marked stable only when:

1. The project builds successfully.
2. Automated tests pass.
3. The APK is generated successfully.
4. The APK installs on a supported Android device or emulator.
5. Core arithmetic and scientific functions are manually checked.
6. No known critical crash or data-loss issue remains.
7. The release notes accurately describe what is included.

A successful source-code commit or CI configuration alone does not prove that the APK has passed device testing.

---

## 10. Continuous integration

The repository includes a GitHub Actions workflow intended to:

- Check out the source code.
- Set up JDK 17.
- Configure Gradle.
- Run local unit tests.
- Build the debug APK.
- Upload the APK as a workflow artifact.

Workflow results should be reviewed in the repository's **Actions** tab. Failed builds must be investigated and corrected before the build is described as successful.

---

## 11. Privacy and security

The core calculator is designed to work offline.

Privacy principles:

- No account is required for basic calculations.
- Do not request permissions that are not needed.
- Keep calculation history on the device by default.
- Do not transmit calculation expressions or history without a clear feature requirement and user consent.
- Do not include secrets or signing credentials in source control.
- Parse expressions safely; never execute user input as code.

Any future feature that uses network access must explain why it needs that access and what information, if any, is transmitted.

---

## 12. Development roadmap

The roadmap tracks the intended development sequence. Checkboxes should be updated only after implementation and verification.

### Phase 1 — Foundation

- [x] Create the native Android project structure.
- [x] Configure Kotlin, Compose, and Gradle.
- [x] Create the initial calculator interface.
- [x] Add a dedicated expression-engine module.
- [x] Add local calculation-history storage.
- [x] Add initial unit tests.
- [ ] Verify a complete clean build in CI.
- [ ] Install and test the APK on a physical Android device.

### Phase 2 — Scientific calculator

- [x] Initial arithmetic and expression parsing.
- [x] Initial trigonometric and logarithmic engine functions.
- [x] Degree/radian setting.
- [ ] Complete and verify every scientific keypad action.
- [ ] Add scientific notation input and display controls.
- [ ] Expand edge-case and precision tests.

### Phase 3 — User experience

- [x] Initial dark and light appearance.
- [x] Initial responsive Compose screen.
- [ ] Finish adaptive layouts for compact and landscape screens.
- [ ] Improve accessibility labels and touch-target testing.
- [ ] Add refined interaction feedback and settings persistence.

### Phase 4 — Advanced mathematics

- [ ] Fractions and fraction conversion.
- [ ] Unit conversion.
- [ ] Statistics tools.
- [ ] Permutations and combinations.
- [ ] Matrix calculations.
- [ ] Complex numbers.
- [ ] Equation solver.
- [ ] Graph plotting.

### Phase 5 — Quality and release

- [ ] Complete automated test coverage for supported functions.
- [ ] Resolve all critical build and runtime issues.
- [ ] Test on multiple Android devices or emulators.
- [ ] Configure secure release signing.
- [ ] Generate and verify a release APK.
- [ ] Prepare store listing, screenshots, and release notes.

---

## 13. Contribution and development standards

All changes should follow these standards:

- Keep features modular and maintainable.
- Use clear names and consistent formatting.
- Avoid duplicated calculation logic.
- Add tests when introducing or changing mathematical behavior.
- Validate input and handle errors safely.
- Preserve existing working functionality.
- Review dependency changes before adding them.
- Keep documentation aligned with the actual application.
- Do not mark unfinished features as complete.
- Investigate failed CI builds rather than ignoring them.

Changes should be small enough to review and test. Larger features should be divided into logical implementation steps.

---

## 14. Future possibilities

After the core scientific calculator is stable, the project may expand with:

- Programmer calculator mode.
- Binary, octal, decimal, and hexadecimal conversions.
- Matrix and vector tools.
- Statistics and probability tools.
- Graphing and equation-solving tools.
- Unit and measurement conversions.
- Exportable calculation history.
- Customizable keypad layouts.
- Additional mathematical utilities.

These are future possibilities, not promises of features already available.

---

## 15. Project identity

**Name:** Calculator — Diffy 01  
**Developer identity:** Diffy 01  
**Platform:** Android  
**Primary language:** Kotlin  
**Interface:** Jetpack Compose  
**Application type:** Offline-first scientific calculator  
**Repository:** `mojiolatunji34-cpu/calculator`  
**License:** MIT

---

## Our development principle

**Build it correctly. Test it properly. Improve it carefully.**

The goal is not simply to produce an APK quickly. The goal is to build a useful, reliable, accurate, and polished Android calculator with a strong foundation that can grow without repeatedly breaking existing features.

**Calculator — More than basic mathematics.**


---

## Version 1.2.0 implementation update

The current source includes the following implemented capabilities. Items in this section describe code present in the repository; they are not a claim that every feature has completed physical-device QA.

### Calculator and scientific engine
- Standard arithmetic, expression parsing, parentheses, powers, factorials and percentages.
- Trigonometric, inverse trigonometric, hyperbolic, logarithmic and exponential functions.
- Degree/radian modes and scientific-notation number parsing.
- Result formatting, copy/share, fraction conversion and memory controls.
- Operation-by-operation working for evaluated arithmetic and scientific expressions.
- Local calculation history with reuse and clear actions.

### Advanced mathematics and tools
- Linear and quadratic equation solvers.
- Fraction conversion and arithmetic helpers.
- Matrix determinant, multiplication, inverse, transpose, rank, trace and symmetric-matrix eigenvalues.
- Descriptive statistics including mean, median, quartiles, range, mode, population/sample variance and standard deviation.
- Permutations and combinations.
- Complex-number addition, subtraction, multiplication, division, conjugate and magnitude.
- Unit conversions for length, mass, volume, temperature, time, pressure and energy, with configurable decimal display precision.
- Date difference and elapsed-time calculations.
- Scientific constants reference.

### Graphs, privacy and interface
- Function plotter with two expressions, optional second curve, horizontal center control and adjustable x-range.
- Dark/light appearance and degree/radian preferences.
- PIN-gated private notes vault using PBKDF2-HMAC-SHA256 PIN hashing, AES-GCM encryption and Android Keystore key storage.
- Vault PIN change without deleting encrypted notes, five-minute automatic lock and temporary lockout after repeated incorrect PIN attempts.
- One-time 12-digit recovery code, stored only as a salted PBKDF2 hash and rotated whenever the PIN is changed or recovered.
- App backup disabled to reduce the chance of vault data being copied through Android backup.
- Offline-first core calculations and native Jetpack Compose interface.

### Build and release status
- GitHub Actions runs local unit tests, builds the debug APK and uploads the APK artifact.
- The latest known successful baseline build completed tests, APK compilation and artifact upload before this feature expansion. The expanded source must pass its own latest workflow run before this version is considered build-verified.
- Physical Android testing, tablet/foldable review, accessibility review, signed production release and Play Store submission still require separate validation and release setup.

### Vault safety note
The vault is an encrypted private-notes feature, not a complete password manager. Keep highly sensitive account credentials out of it. The user should save the one-time recovery code offline. The recovery code is shown during setup and again after PIN changes or recovery; it is not stored in plaintext by the app. Losing both the PIN and recovery code may make encrypted entries inaccessible. Repeated incorrect PIN attempts trigger a temporary lockout.

### Known limitations
- The graph plotter currently provides manual horizontal pan and x-range zoom controls; it is not a full interactive multi-axis graphing package.
- Eigenvalue calculation is limited to real symmetric matrices.
- The calculator uses floating-point arithmetic for general expressions; displayed decimal precision is formatted and should not be mistaken for arbitrary-precision arithmetic.
- Release signing, physical-device QA and production distribution are not complete until performed and verified.

**Build it correctly. Test it properly. Improve it carefully.**

**Calculator — DIFFY 01. More than basic mathematics.**
