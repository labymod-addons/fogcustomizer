import net.labymod.labygradle.common.extension.model.GameVersion

// should be the same as https://github.com/labymod-addons/optifine/blob/master/core/src/main/resources/assets/optifine/versions.json
val optiFineVersions = mapOf(
    "1.8.9" to "HD U M5",
    "1.12.2" to "HD U G5",
    "1.16.5" to "HD U G8",
    "1.17.1" to "HD U H1",
    "1.18.2" to "HD U H9",
    "1.19.2" to "HD U I2",
    "1.19.3" to "HD U I3",
    "1.19.4" to "HD U I4",
    "1.20.1" to "HD U I6",
    "1.20.2" to "HD U I7 pre1",
    "1.20.4" to "HD U I7",
    "1.20.6" to "HD U I9 pre1",
    "1.21" to "HD U J1 pre9",
    "1.21.1" to "HD U J1",
)

plugins {
    id("net.labymod.labygradle")
    id("net.labymod.labygradle.addon")
    id("org.cadixdev.licenser") version ("0.6.1")
}

val versions = providers.gradleProperty("net.labymod.minecraft-versions").get().split(";")

group = "net.labymod.addons"
version = providers.environmentVariable("VERSION").getOrElse("1.0.0")

labyMod {
    defaultPackageName = "net.labymod.addons.fogcustomizer" //change this to your main package name (used by all modules)

    minecraft {
        registerVersion(versions.toTypedArray()) {
            installOptiFine(false)

            runs {
                getByName("client") {
                    // When the property is set to true, you can log in with a Minecraft account
                    devLogin = true
                }
            }
        }
    }

    addonInfo {
        namespace = "fogcustomizer"
        displayName = "fogcustomizer"
        author = "LabyMedia GmbH"
        minecraftVersion = "*"
        version = rootProject.version.toString()
    }
}

subprojects {
    plugins.apply("net.labymod.labygradle")
    plugins.apply("net.labymod.labygradle.addon")
    plugins.apply("org.cadixdev.licenser")

    group = rootProject.group
    version = rootProject.version

    license {
        header(rootProject.file("gradle/LICENSE-HEADER.txt"))
        newLine.set(true)
    }
}

fun GameVersion.installOptiFine(install: Boolean) {
    if (!install) {
        return
    }

    optiFineVersions[versionId]?.apply {
        optiFineVersion.set(this);
    }
}