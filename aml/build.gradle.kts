import mb.spoofax.compiler.adapter.*

plugins {
  `java`
  `java-library`
  // id("org.metaborg.gradle.config.java-library")
  // id("org.metaborg.gradle.config.junit-testing")
  id("org.metaborg.spoofax.compiler.gradle.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

dependencies {
  api("com.google.code.findbugs:jsr305:3.0.2")
  api("org.metaborg:pie.task.java:999.9.9-develop-SNAPSHOT")

  // annotationProcessor("com.google.code.findbugs:jsr305:3.0.2")

  testImplementation("org.metaborg:spoofax.test:999.9.9-refret-SNAPSHOT")
  // testCompileOnly("org.checkerframework:checker-qual-android")
}

tasks.named("sourcesJar") {
  mustRunAfter("compileLanguageProject")
  mustRunAfter("compileAdapterProject")
  mustRunAfter("compileLanguage")
}

tasks.named("processResources") {
  mustRunAfter("compileLanguage")
}
