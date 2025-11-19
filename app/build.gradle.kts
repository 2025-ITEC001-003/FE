import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

// local.properties 파일 로드
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.jesafe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.jesafe"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = localProperties.getProperty("KAKAO_NATIVE_APP_KEY", "")
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")

        // local.properties에서 서버 URL을 읽어오거나, 없으면 빈 문자열을 기본값으로 설정
        val serverUrl = localProperties.getProperty("SERVER_URL", "")
        buildConfigField("String", "SERVER_URL", "\"$serverUrl\"")
    }

    buildTypes {
        debug {
            // 로컬 테스트를 위해 에뮬레이터에서 호스트 PC의 localhost를 가리키는 10.0.2.2를 사용합니다.
            buildConfigField("String", "SERVER_URL", "\"http://10.0.2.2:8000/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "SERVER_URL", "\"http://3.37.68.172:8000/\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3) // 아이콘 라이브러리 추가
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)

    // Maps
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.maps.utils.ktx)
    implementation(libs.maps.utils)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Kakao
    implementation(libs.kakao.user)
}


secrets {
    defaultPropertiesFileName = "app/secrets.properties"
}
