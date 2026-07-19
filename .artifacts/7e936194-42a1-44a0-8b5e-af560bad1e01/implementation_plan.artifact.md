# Fix Firebase Auth Emulator Connection

The anonymous account is not being created in the emulator because the code to connect to the Auth emulator is commented out, the port is incorrect, and the emulator is not configured to allow external connections from your physical device (Samsung A14).

## Proposed Changes

### Firebase Configuration

#### [MODIFY] [firebase.json](file:///mnt/b44e42b9-497a-47b6-9d82-210124fd3ab8/Programming/android/kotlin/craftaday/firebase.json)
- Set `host` to `0.0.0.0` for all emulators. This allows your Samsung A14 to connect to the emulators running on your computer.

### Firebase Module

#### [MODIFY] [FirebaseModule.kt](file:///mnt/b44e42b9-497a-47b6-9d82-210124fd3ab8/Programming/android/kotlin/craftaday/app/src/main/java/com/den/craftaday/FirebaseModule.kt)
- Uncomment the `auth.useEmulator` call.
- Update the Auth emulator port from `9090` to `9099` to match `firebase.json`.
- Refactor `provideFirebaseFirestore` to avoid calling `getInstance()` multiple times.

## Verification Plan

### Manual Verification
1.  Restart the Firebase emulators: `firebase emulators:start`.
2.  Verify the emulators are reachable from your phone's browser at `http://192.168.10.142:8080`.
3.  Run the app and click the "Anonymous" button.
4.  Check the Firebase Emulator UI (usually at `http://localhost:4000`) under the "Authentication" tab to see if a new anonymous user appears.
