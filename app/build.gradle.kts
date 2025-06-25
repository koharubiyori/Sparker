import com.google.protobuf.gradle.id
import java.util.Properties
import java.io.FileInputStream

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.protobuf)
  id("kotlin-kapt")
  alias(libs.plugins.google.gms.google.services)
  alias(libs.plugins.firebase.crashlytics)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()

if (keystorePropertiesFile.exists()) {
  keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
  }
}

android {
  namespace = "koharubiyori.sparker"
  compileSdk = 36

  defaultConfig {
    applicationId = "koharubiyori.sparker"
    minSdk = 30
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//    ndkVersion = "27.2.12479018"
//
//    ndk {
//      val jniLibsDirectory = File(project.projectDir, "src/main/jniLibs")
//      if (File(jniLibsDirectory, "arm64-v8a/libfreerdp3.so").exists()) abiFilters.add("arm64-v8a")
//      if (File(jniLibsDirectory, "armeabi-v7a/libfreerdp3.so").exists()) abiFilters.add("armeabi-v7a")
//      if (File(jniLibsDirectory, "x86_64/libfreerdp3.so").exists()) abiFilters.add("x86_64")
//      if (File(jniLibsDirectory, "x86/libfreerdp3.so").exists()) abiFilters.add("x86")
//    }
//
//    externalNativeBuild {
//      cmake {
//        arguments.add("-DWITH_CLIENT_CHANNELS=ON")
//      }
//    }
  }

  splits {
    abi {
      reset()
      isEnable = true
      isUniversalApk = false
      //noinspection ChromeOsAbiSupport
      include("arm64-v8a", "armeabi-v7a")
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file(keystoreProperties["KEYSTORE_FILE"] as String)
      storePassword = keystoreProperties["KEYSTORE_PASSWORD"] as String
      keyAlias = keystoreProperties["KEY_ALIAS"] as String
      keyPassword = keystoreProperties["KEY_PASSWORD"] as String
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }

    debug {
      isJniDebuggable = true
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
//  externalNativeBuild {
//    cmake {
//      path = file("src/main/cpp/CMakeLists.txt")
//    }
//  }
}

kapt {
  correctErrorTypes = true
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:4.31.1"
  }
  generateProtoTasks {
    all().forEach { task ->
      task.builtins {
        id("java") { option("lite") }   // The generated Kotlin code based on the Java code.
        id("kotlin") { option("lite") }
      }
    }
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.hilt.android)
  implementation(libs.androidx.tools.core)
  kapt(libs.hilt.android.compiler)

  implementation(libs.timber)
  implementation(libs.fulvius31.ip.neigh.sdk30)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.gson)
  implementation(libs.okhttp)
  implementation(libs.okhttp3.logging.interceptor)
  implementation(libs.protobuf.kotlin.lite)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.crashlytics)


  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

