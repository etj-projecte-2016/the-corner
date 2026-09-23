plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.thecorner"
    compileSdk = 37

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.thecorner"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.room3.runtime)

    implementation(libs.material)

    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    ksp(libs.androidx.room3.compiler)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
