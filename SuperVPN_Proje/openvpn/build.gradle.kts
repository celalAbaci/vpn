import com.android.build.gradle.api.LibraryVariant

/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android") version "1.9.22"
}

android {
    buildFeatures {
        aidl = true
        buildConfig = true
    }
    namespace = "de.blinkt.openvpn"
    compileSdk = 35
    //compileSdkPreview = "UpsideDownCake"

    // Also update runcoverity.sh
    ndkVersion = "29.0.14206865"

    defaultConfig {
        minSdk = 21
        targetSdk = 34
        //targetSdkPreview = "UpsideDownCake"
        externalNativeBuild {
            cmake {
                //arguments+= "-DCMAKE_VERBOSE_MAKEFILE=1"
            }
        }
    }


    //testOptions.unitTests.isIncludeAndroidResources = true

    externalNativeBuild {
        cmake {
            path = File("${projectDir}/src/main/cpp/CMakeLists.txt")
        }
    }

    sourceSets {
        getByName("main") {
            assets.srcDirs("src/main/assets", "build/ovpnassets")

        }

        create("ui") {
        }

        create("skeleton") {
        }

        getByName("debug") {
        }

        getByName("release") {
        }
    }

    signingConfigs {
        create("release") {
            // ~/.gradle/gradle.properties
            val keystoreFile: String? by project
            storeFile = keystoreFile?.let { file(it) }
            val keystorePassword: String? by project
            storePassword = keystorePassword
            val keystoreAliasPassword: String? by project
            keyPassword = keystoreAliasPassword
            val keystoreAlias: String? by project
            keyAlias = keystoreAlias
            enableV1Signing = true
            enableV2Signing = true
        }

        create("releaseOvpn2") {
            // ~/.gradle/gradle.properties
            val keystoreO2File: String? by project
            storeFile = keystoreO2File?.let { file(it) }
            val keystoreO2Password: String? by project
            storePassword = keystoreO2Password
            val keystoreO2AliasPassword: String? by project
            keyPassword = keystoreO2AliasPassword
            val keystoreO2Alias: String? by project
            keyAlias = keystoreO2Alias
            enableV1Signing = true
            enableV2Signing = true
        }

    }

    lint {
        enable += setOf("BackButton", "EasterEgg", "StopShip", "IconExpectedSize", "GradleDynamicVersion", "NewerVersionAvailable")
        checkOnly += setOf("ImpliedQuantity", "MissingQuantity")
        disable += setOf("MissingTranslation", "UnsafeNativeCodeLocation")
    }


    flavorDimensions += listOf("implementation", "ovpnimpl")

    productFlavors {
        create("ui") {
            dimension = "implementation"
        }

        create("skeleton") {
            dimension = "implementation"
        }

        create("ovpn23")
        {
            dimension = "ovpnimpl"
            buildConfigField("boolean", "openvpn3", "true")
        }

        create("ovpn2")
        {
            dimension = "ovpnimpl"
            buildConfigField("boolean", "openvpn3", "false")
        }
    }

    buildTypes {
        getByName("release") {
            if (project.hasProperty("icsopenvpnDebugSign")) {
                logger.warn("property icsopenvpnDebugSign set, using debug signing for release")
                signingConfig = android.signingConfigs.getByName("debug")
            } else {
                productFlavors["ovpn23"].signingConfig = signingConfigs.getByName("release")
                productFlavors["ovpn2"].signingConfig = signingConfigs.getByName("releaseOvpn2")
            }
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
        sourceCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

var swigcmd = "swig"
// Workaround for macOS(arm64) and macOS(intel) since it otherwise does not find swig and
// I cannot get the Exec task to respect the PATH environment :(
if (file("/opt/homebrew/bin/swig").exists())
    swigcmd = "/opt/homebrew/bin/swig"
else if (file("/usr/local/bin/swig").exists())
    swigcmd = "/usr/local/bin/swig"


fun registerGenTask(variantName: String, variantDirName: String): File {
    val baseDir = File(buildDir, "generated/source/ovpn3swig/${variantDirName}")
    val genDir = File(baseDir, "net/openvpn/ovpn3")

    tasks.register<Exec>("generateOpenVPN3Swig${variantName}")
    {

        doFirst {
            mkdir(genDir)
        }
        commandLine(listOf(swigcmd, "-outdir", genDir, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
                "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
                "-DOPENVPN_PLATFORM_ANDROID",
                "-o", "${genDir}/ovpncli_wrap.cxx", "-oh", "${genDir}/ovpncli_wrap.h",
                "src/main/cpp/openvpn3/client/ovpncli.i"))
        inputs.files( "src/main/cpp/openvpn3/client/ovpncli.i")
        outputs.dir( genDir)

    }
    return baseDir
}

android.libraryVariants.all(object : Action<LibraryVariant> { // Değişti
    override fun execute(variant: LibraryVariant) { // Değişti
        val sourceDir = registerGenTask(variant.name, variant.baseName.replace("-", "/"))
        val task = tasks.named("generateOpenVPN3Swig${variant.name}").get()

        variant.registerJavaGeneratingTask(task, sourceDir)
    }
})


dependencies {
    // Version Catalog (libs) yerine doğrudan sürümler kullanıyoruz
    implementation("androidx.annotation:annotation:1.7.0")
    implementation("androidx.core:core-ktx:1.12.0")

    // "uiImplementation" yerine "implementation" kullanabilirsiniz veya özel tanımlar kalabilir
    // Ancak en garantisi hepsini implementation yapmaktır.
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Hata veren preference, recyclerview vb.
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("androidx.webkit:webkit:1.9.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.22")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Eğer "uiImplementation" hatası alırsanız, yukarıdakilerin hepsini
    // implementation yaptığımız için sorun kalmayacaktır.
}
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

