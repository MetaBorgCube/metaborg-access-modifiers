plugins {
    java
    id("org.metaborg.spoofax.compiler.gradle.language") version "999.9.9-aml-release-SNAPSHOT" apply false
    id("org.metaborg.spoofax.compiler.gradle.adapter") version "999.9.9-aml-release-SNAPSHOT" apply false
    id("org.metaborg.spoofax.lwb.compiler.gradle.language") version "999.9.9-aml-release-SNAPSHOT" apply false
}

val spoofaxVersion = "999.9.9-aml-release-SNAPSHOT"

allprojects {
  apply(plugin = "java")

  val javaVersion = JavaLanguageVersion.of(11)
  ext["spoofaxVersion"] = spoofaxVersion

  repositories {
      mavenLocal()
      maven("https://artifacts.metaborg.org/content/groups/public/")
      mavenCentral()
  }

  java {
      toolchain {
          languageVersion.set(javaVersion)
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

  tasks.named<Test>("test") {
      useJUnitPlatform()
  }
}
