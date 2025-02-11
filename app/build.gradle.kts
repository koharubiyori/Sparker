plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  id("kotlin-kapt")
  id("com.google.dagger.hilt.android")
}

android {
  namespace = "koharubiyori.sparker"
  compileSdk = 35

  defaultConfig {
    applicationId = "koharubiyori.sparker"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  splits {
    abi {
      reset()
      isEnable = true
      isUniversalApk = false
      //noinspection ChromeOsAbiSupport
      include("arm64-v8a")
    }
  }

  signingConfigs {
    create("release") {
      storeFile = file(project.findProperty("KEYSTORE_FILE") as String)
      storePassword = project.findProperty("KEYSTORE_PASSWORD") as String
      keyAlias = project.findProperty("KEY_ALIAS") as String
      keyPassword = project.findProperty("KEY_PASSWORD") as String
    }
  }

  buildTypes {
    release {
      signingConfig = signingConfigs.getByName("release")
//      isMinifyEnabled = true
//      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }
}

kapt {
  correctErrorTypes = true
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
  kapt(libs.hilt.android.compiler)

  implementation(libs.timber)
  implementation(libs.fulvius31.ip.neigh.sdk30)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.gson)
  implementation(libs.okhttp)
  implementation(libs.okhttp3.logging.interceptor)


  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

