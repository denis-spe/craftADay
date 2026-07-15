# Fix Unresolved Reference 'HiltAndroidTest'

The user is encountering an `Unresolved reference 'HiltAndroidTest'` in `LandingScreenTest.kt`. This is due to missing Hilt testing dependencies for instrumented tests (`androidTest`) and missing imports in the test file.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///mnt/b44e42b9-497a-47b6-9d82-210124fd3ab8/Programming/android/kotlin/craftaday/app/build.gradle.kts)
- Add `androidTestImplementation(libs.hilt.android.testing)` to the dependencies block.
- Add `kspAndroidTest(libs.dagger.hilt.android.compiler)` to ensure Hilt code generation works for instrumented tests.
- (Optional but recommended) Update `testInstrumentationRunner` to a custom Hilt test runner if needed, but I'll start with just the dependencies to resolve the reference.

### Instrumented Tests

#### [MODIFY] [LandingScreenTest.kt](file:///mnt/b44e42b9-497a-47b6-9d82-210124fd3ab8/Programming/android/kotlin/craftaday/app/src/androidTest/java/com/den/craftaday/LandingScreenTest.kt)
- Add missing imports:
    - `dagger.hilt.android.testing.HiltAndroidTest`
    - `dagger.hilt.android.testing.HiltAndroidRule`
- Update `createAndroidComposeRule<HiltTestActivity>()` to `createAndroidComposeRule<MainActivity>()` since `MainActivity` is already an `@AndroidEntryPoint` and `HiltTestActivity` doesn't exist yet.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleAndroidTest` to verify that the test source compiles.
- Run the test via Android Studio or `./gradlew :app:connectedDebugAndroidTest`.

### Manual Verification
- Check that the IDE no longer shows red squiggles for `HiltAndroidTest`.
