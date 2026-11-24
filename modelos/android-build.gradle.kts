// Modelo do "Estrutura Universal para Vibe Coding" (seções 1 e 7.4).
// Trechos para app/build.gradle.kts (Android). Somar ao arquivo do projeto,
// sem apagar o que já existe.

// Versão: uma fonte só (gradle.properties: APP_VERSION=1.2.0)
val appVersion = providers.gradleProperty("APP_VERSION").get()
val (maj, min, pat) = appVersion.split(".").map { it.toInt() }

android {
    defaultConfig {
        versionName = appVersion
        versionCode = maj * 10000 + min * 100 + pat   // esquema fixo; nunca desce
    }
    signingConfigs {
        create("release") {
            // O CI decodifica a keystore dos secrets e exporta ANDROID_KEYSTORE_PATH.
            System.getenv("ANDROID_KEYSTORE_PATH")?.let {
                storeFile = file(it)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true      // R8: remove código sem uso e otimiza
            isShrinkResources = true    // remove recursos sem uso
            // proguard-rules.pro começa com -dontobfuscate (sem ofuscação)
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (System.getenv("ANDROID_KEYSTORE_PATH") != null)
                signingConfig = signingConfigs.getByName("release")
        }
    }
}
