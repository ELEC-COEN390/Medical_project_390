plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.moodproject"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.moodproject"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // Add these options for handling large model files
    aaptOptions {
        noCompress("model")
    }

    packagingOptions {
        resources {
            excludes += listOf(
                "project.properties",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {
    // Existing dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    implementation(files("libs\\jlibrosa-1.1.8-SNAPSHOT-javadoc.jar"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    // Vosk library for offline speech recognition
    implementation("com.alphacephei:vosk-android:0.3.47")

    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    implementation("com.google.zxing:core:3.5.2")
    implementation ("com.github.bumptech.glide:glide:4.15.1")

    implementation(files("libs/jlibrosa-1.1.8-SNAPSHOT-jar-with-dependencies.jar"))

    implementation(libs.qr.scanner)

    // For handling JSON responses from Vosk
    implementation("org.json:json:20210307")
    implementation(libs.glide)
    annotationProcessor(libs.glideCompiler)


}

