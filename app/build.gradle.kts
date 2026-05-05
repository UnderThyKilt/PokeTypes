plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.underthykilt.poketypes"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.underthykilt.poketypes"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        compose = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

afterEvaluate {
    val baseTask = tasks.named<Test>("testDebugUnitTest").get()

    mapOf(
        "testQuizLogic" to "com.underthykilt.poketypes.QuizLogicTest",
        "testEnrichWithPokemonUseCase" to "com.underthykilt.poketypes.domain.EnrichWithPokemonUseCaseTest",
        "testGenerateQuizQuestionsUseCase" to "com.underthykilt.poketypes.domain.GenerateQuizQuestionsUseCaseTest",
    ).forEach { (taskName, className) ->
        tasks.register<Test>(taskName) {
            group = "verification"
            description = "Runs $className"
            classpath = baseTask.classpath
            testClassesDirs = baseTask.testClassesDirs
            dependsOn(baseTask.taskDependencies)
            filter { includeTestsMatching(className) }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.coil3.compose)
    implementation(libs.coil3.network.okhttp)
    debugImplementation(libs.androidx.ui.tooling)
    // unit tests (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)

    // instrumented tests (device / emulator)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
