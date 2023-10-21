import de.undercouch.gradle.tasks.download.Download

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("de.undercouch.download") version "5.5.0"
}

android {
    namespace = "com.yveskalume.instaglow"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yveskalume.instaglow"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        externalNativeBuild {
            cmake {
                arguments.add("-DANDROID_STL=c++_shared")
            }
        }
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a","arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isJniDebuggable = true
            packagingOptions {
                doNotStrip.add("**//.so")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    aaptOptions {
        noCompress += "tflite"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(
                "../libraries/tensorflowlite/jni",
                "../libraries/tensorflowlite-gpu/jni"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.guava:guava:32.1.3-android")

    val nav_version = "2.7.4"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    implementation("androidx.compose.material:material-icons-extended:1.5.4")
}

tasks.register<Download>("downloadESRGANModelFile") {
    src("https://storage.googleapis.com/download.tensorflow.org/models/tflite/esrgan/ESRGAN.tflite")
    dest("/src/main/assets/ESRGAN.tflite")
    overwrite(false)
}

tasks.register<Download>("downloadTFLiteJARFile") {
    download {
        src("https://repo1.maven.org/maven2/org/tensorflow/tensorflow-lite/2.3.0/tensorflow-lite-2.3.0.aar")
        dest("${project.rootDir}/libraries/tensorflow-lite-2.3.0.aar")
        overwrite(false)
        retries(5)
    }
}

tasks.register<Download>("downloadTFLiteGPUDelegateJARFile") {
    download {
        src("https://repo1.maven.org/maven2/org/tensorflow/tensorflow-lite-gpu/2.3.0/tensorflow-lite-gpu-2.3.0.aar")
        dest("${project.rootDir}/libraries/tensorflow-lite-gpu-2.3.0.aar")
        overwrite(false)
        retries(5)
    }
}

tasks.register("fetchTFLiteLibs") {
    doLast {
        copy {
            from(zipTree("${project.rootDir}/libraries/tensorflow-lite-2.3.0.aar"))
            into("${project.rootDir}/libraries/tensorflowlite/")
            include("headers/tensorflow/lite/c/*h")
            include("headers/tensorflow/lite/*h")
            include("jni/**/libtensorflowlite_jni.so")
        }
        copy {
            from(zipTree("${project.rootDir}/libraries/tensorflow-lite-gpu-2.3.0.aar"))
            into("${project.rootDir}/libraries/tensorflowlite-gpu/")
            include("headers/tensorflow/lite/delegates/gpu/*h")
            include("jni/**/libtensorflowlite_gpu_jni.so")
        }
    }
}