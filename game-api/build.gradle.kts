plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.maven.publish)
}

group = "work.lclpnet.mods"
version = "${project.property("game_api_version")}+${libs.versions.minecraft.get()}"

repositories {
    mavenCentral()
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    implementation(libs.kibu)
    implementation(libs.gaco)
    implementation(libs.fantasy)
    implementation(libs.kibu.world.api)

    // include these dependencies
    fun includeImpl(dep: Any) {
        implementation(dep)
        include(dep)
    }

    includeImpl(libs.tika.core)
    includeImpl(libs.translations4j)
    includeImpl(libs.json)
    includeImpl(libs.xz)
    includeImpl(libs.commons.compress)

    testImplementation(libs.fabric.loader.junit)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.mockito.core)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()

            from(components["java"])

            pom {
                name.set("game-api")
                description.set("A library mod that provides functionality to implement games")
            }
        }
    }
}
