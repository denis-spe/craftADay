plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    // Optional, provides the @Serialize annotation for autogeneration of Serializers.
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.den.craftaday"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.den.craftaday"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            optimization {
                enable = false
            }
        }

        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true // Add this line
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Firebase ->
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Firestore ->
    implementation(libs.firebase.firestore)

    // Firebase Auth ->
    implementation(libs.firebase.ui.auth)

    // Firebase Auth google ->
    // For Google Sign-In, it is recommended to include the Credential Manager SDK
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.android.libraries.identity.googleid)

    // Navigation 3 ->
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.material3.adaptive.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // Vico chart ->
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.compose.glance)

    // Kuiver chart ->
    implementation(libs.kuiver.android)

    // Hilt ->
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler) // KSP handles the code gen
    implementation(libs.hilt.navigation.compose) // Gives you hiltViewModel()

    // Hilt Testing ->
    // For Robolectric tests.
    testImplementation(libs.hilt.android.testing)
    kspTest(libs.dagger.hilt.android.compiler)

    // For instrumented tests.
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.dagger.hilt.android.compiler)

    // Compose UI test rule.
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // DataStore ->
    implementation(libs.androidx.datastore.preferences)

    // Material icons ->
    implementation(libs.androidx.compose.material.icons.extended)

    // Coil ->
    // Coil Core Compose implementation
    implementation(libs.coil.compose)
    // Network factory integration required by Coil 3 to fetch remote URLs
    implementation(libs.coil.network)
}