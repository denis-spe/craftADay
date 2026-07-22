// Great id the LORD GOD of hosts
package com.den.craftaday

import com.den.craftaday.backend.blueprints.AccountService
import com.den.craftaday.backend.blueprints.DataStorage
import com.den.craftaday.backend.repositories.AccountServiceRepo
import com.den.craftaday.backend.repositories.DataStorageRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.LocalCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.MemoryCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    // Changed to your computer's local IP to allow physical device connection
    private const val EMULATOR_IP = "192.168.10.142"

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        val auth = FirebaseAuth.getInstance()
        if (BuildConfig.DEBUG) {
            // FIXED PORT: Corrected from 9090 to 9099
            auth.useEmulator(EMULATOR_IP, 9099)
        }
        return auth
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()

        // Turn off persistent disk caching during local debugging
        // to avoid conflicts with your local emulator data
        val cacheSettings: LocalCacheSettings = if (BuildConfig.DEBUG) {
            MemoryCacheSettings.newBuilder().build()
        } else {
            PersistentCacheSettings.newBuilder()
                .setSizeBytes(100 * 1024 * 1024) // 100 MB cache
                .build()
        }

        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(cacheSettings)
            .build()

        firestore.firestoreSettings = settings
        FirebaseFirestore.setLoggingEnabled(true)

        if (BuildConfig.DEBUG) {
            // FIXED CONNECTION: Calling directly on the instance inside the correct lifecycle order
            firestore.useEmulator(EMULATOR_IP, 8080)
        }

        return firestore
    }

    @Singleton
    @Provides
    fun provideDataStorage(firestore: FirebaseFirestore): DataStorage {
        return DataStorageRepo(firestore)
    }

    @Singleton
    @Provides
    fun provideAccountService(auth: FirebaseAuth): AccountService {
        return AccountServiceRepo(auth)
    }
}
