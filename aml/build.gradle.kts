import mb.spoofax.compiler.adapter.*

plugins {
  `java-library`
  id("org.metaborg.spoofax.compiler.gradle.language") version "999.9.9-refret-SNAPSHOT" apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") version "999.9.9-refret-SNAPSHOT" apply false
  id("org.metaborg.spoofax.lwb.compiler.gradle.language") version "999.9.9-refret-SNAPSHOT"
}

repositories {
    mavenLocal()
    maven("https://artifacts.metaborg.org/content/groups/public/")
    mavenCentral()
}

dependencies {
  api("javax.annotation:javax.annotation-api:1.3.1")
  api("org.metaborg:pie.task.java:999.9.9-develop-SNAPSHOT")

  annotationProcessor("javax.annotation:javax.annotation-api:1.3.1")
  
  testAnnotationProcessor("javax.annotation:javax.annotation-api:1.3.1")
  // testImplementation("org.metaborg:spoofax.test:")
  // testCompileOnly("org.checkerframework:checker-qual-android")
}
/*
val javaVersion = JavaLanguageVersion.of(11)

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
        withSourcesJar()
        withJavadocJar()
    }
}

configure<JavaPluginExtension> {
    toolchain.languageVersion.set(javaVersion)
    withSourcesJar()
    withJavadocJar()
}

val service = project.extensions.getByType<JavaToolchainService>()
val customLauncher = service.launcherFor {
    languageVersion.set(javaVersion)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
*/
