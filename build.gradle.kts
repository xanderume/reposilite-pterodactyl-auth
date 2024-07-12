plugins {
    application
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "cc.fyre.reposilite"

repositories {
    reposilite()
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(libs.guava)
    compileOnly(libs.reposilite)

    implementation(libs.bcrypt)
    implementation(libs.mariadb)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.core)

    testImplementation(libs.bcrypt)
    testImplementation(libs.mariadb)
    testImplementation(libs.exposed.jdbc)
    testImplementation(libs.exposed.core)
    testImplementation(libs.kotlin.test)
}

application {
    mainClass.set("cc.fyre.reposilite.PterodactylAuthPlugin")
}

tasks {

    test {
        useJUnitPlatform()
    }

    shadowJar {
        archiveBaseName.set(this.project.name)
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("")
    }

}

kotlin {
    jvmToolchain(11)
    compilerOptions {
        freeCompilerArgs.set(listOf(
            "-Xopt-in=kotlin.RequiresOptIn",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=io.ktor.util.KtorExperimentalAPI",
            "-language-version","2.0",
        ))
    }
}

fun RepositoryHandler.reposilite() {
    maven("https://maven.reposilite.com/releases")
    maven("https://maven.reposilite.com/snapshots")
    maven("https://maven.reposilite.com/maven-central")
}