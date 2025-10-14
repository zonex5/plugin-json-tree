plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get())) }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

intellij {
    type.set("IC")
    version.set(providers.gradleProperty("intellijVersion").get())
    // JSON support is a platform module; no Marketplace plugin dependency is needed.
    // Do not set 'plugins' here.
}

tasks {
    patchPluginXml {
        sinceBuild.set(providers.gradleProperty("pluginSinceBuild"))
        // Do not set untilBuild to avoid accidental "requires build *"
    }
    buildSearchableOptions { enabled = false }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}
