plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget()

    // iOS-таргеты подключаются на macOS при добавлении iosApp (Room 2.7 + sqlite-bundled).
    // iosArm64()
    // iosSimulatorArm64()
}

android {
    namespace = "com.koreanimmersion.core.database"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines.core)
    commonMainImplementation(libs.androidx.room.runtime)
    commonMainImplementation(libs.androidx.sqlite.bundled)

    add("kspAndroid", libs.androidx.room.compiler)
}
