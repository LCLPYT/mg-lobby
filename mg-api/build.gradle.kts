import org.apache.tools.ant.filters.ReplaceTokens
import org.kohsuke.github.GitHub
import work.lclpnet.build.task.GithubDeploymentTask
import work.lclpnet.build.util.GithubUtil

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
        token = env["GITHUB_TOKEN"]
        repository = env["GITHUB_REPOSITORY"]
    }

    val targetTag = "mg-api-${project.version}"

    release {
        title = "[${libs.versions.minecraft.get()}] ${project.name} ${project.version}"
        tag = targetTag
    }

    assets.add(artifactTask.archiveFile.get())

    onlyIf("tag does not exist on GitHub") {
        val token = env["GITHUB_TOKEN"] ?: return@onlyIf false
        val repo = env["GITHUB_REPOSITORY"] ?: return@onlyIf false

        !GithubUtil(GitHub.connectUsingOAuth(token)).tagExists(repo, targetTag)
    }
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
