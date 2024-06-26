import net.labymod.gradle.core.minecraft.provider.VersionProvider

plugins {
    id("java-library")
    id("net.labymod.gradle")
    id("net.labymod.gradle.addon")
    id("org.cadixdev.licenser") version ("0.6.1")
}

group = "net.labymod"
version = "1.0.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

labyMod {
    defaultPackageName = "net.labymod.addons.fogcustomizer" //change this to your main package name (used by all modules)
    addonInfo {
        namespace = "fogcustomizer"
        displayName = "fogcustomizer"
        author = "LabyMedia GmbH"
        version = System.getenv().getOrDefault("VERSION", "0.0.1")
    }

    minecraft {
        registerVersions(
                "1.8.9",
                "1.12.2",
                "1.16.5",
                "1.17.1",
                "1.18.2",
                "1.19.2",
                "1.19.3",
                "1.19.4",
                "1.20.1",
                "1.20.2",
                "1.20.4",
                "1.20.5",
                "1.20.6",
                "1.21"
        ) { version, provider ->
            configureRun(provider, version)

            provider.applyOptiFine(version, false)
        }

        subprojects.forEach {
            if (it.name != "game-runner") {
                filter(it.name)
            }
        }
    }

    addonDev {
        productionRelease()
    }
}

fun VersionProvider.applyOptiFine(version: String, useOptiFine: Boolean) {
    if (!useOptiFine) {
        return
    }

    optiFineVersion = when (version) {
        "1.16.5" -> {
            "HD U G8"
        }

        "1.17.1" -> {
            "HD U H1"
        }

        "1.18.2" -> {
            "HD U H7"
        }

        "1.19.2" -> {
            "HD U I1"
        }

        "1.19.3" -> {
            "HD U I3"
        }

        "1.19.4" -> {
            "HD U I4"
        }

        "1.20.1" -> {
            "HD U I5"
        }

        else -> {
            null
        }
    }
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("net.labymod.gradle")
    plugins.apply("net.labymod.gradle.addon")
    plugins.apply("org.cadixdev.licenser")

    repositories {
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://jitpack.io")
        mavenLocal()
    }

    license {
        header(rootProject.file("gradle/LICENSE-HEADER.txt"))
        newLine.set(true)
    }
}

fun configureRun(provider: VersionProvider, gameVersion: String) {
    provider.runConfiguration {
        mainClass = "net.minecraft.launchwrapper.Launch"
        jvmArgs("-Dnet.labymod.running-version=${gameVersion}")
        jvmArgs("-Dmixin.debug=true")
        jvmArgs("-Dnet.labymod.debugging.all=true")
        jvmArgs("-Dmixin.env.disableRefMap=true")

        args("--tweakClass", "net.labymod.core.loader.vanilla.launchwrapper.LabyModLaunchWrapperTweaker")
        args("--labymod-dev-environment", "true")
        args("--addon-dev-environment", "true")
    }

    provider.javaVersion = when (gameVersion) {
        else -> {
            JavaVersion.VERSION_21
        }
    }

    provider.mixin {
        val mixinMinVersion = when (gameVersion) {
            "1.8.9", "1.12.2", "1.16.5" -> {
                "0.6.6"
            }

            else -> {
                "0.8.2"
            }
        }

        minVersion = mixinMinVersion
    }
}
