import org.apache.tools.ant.filters.ReplaceTokens
import work.lclpnet.build.task.GithubDeploymentTask

plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.maven.publish)
}

val env: Map<String, String> = System.getenv()

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

tasks.processResources {
    val tokens = mapOf(
        "version" to project.version.toString(),
        "loader_version" to libs.versions.fabric.loader.get(),
        "minecraft_compat" to project.property("minecraft_compat"),
        "java_version" to libs.versions.java.get()
    )

    inputs.properties(tokens)

    filesMatching("fabric.mod.json") {
        expand(tokens)
    }

    filesMatching("*.mixins.json") {
        filter(ReplaceTokens::class, mapOf(
            "beginToken" to $$"${",
            "endToken" to "}",
            "tokens" to tokens
        ))
    }
}

tasks.register<GithubDeploymentTask>("github") {
    description = "Create a new GitHub release and uploads the built jar to github"

    val artifactTask = tasks.getByName<Jar>("jar")

    dependsOn(artifactTask)

    config {
        token = requireNotNull(env["GITHUB_TOKEN"]) { "Undefined env variable 'GITHUB_TOKEN'" }
        repository = requireNotNull(env["GITHUB_REPOSITORY"]) { "Undefined env variable 'GITHUB_REPOSITORY'" }
    }

    release {
        title = "[${libs.versions.minecraft.get()}] ${project.name} ${project.version}"
        tag = "mg-api-${project.version.toString()}"
    }

    assets.add(artifactTask.archiveFile.get())
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()

            from(components["java"])

            pom {
                name.set("mg-api")
                description.set("A library mod that provides functionality to implement games")
            }
        }
    }
}
