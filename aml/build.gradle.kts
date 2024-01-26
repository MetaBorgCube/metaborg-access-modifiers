import mb.spoofax.compiler.adapter.*

plugins {
  `java`
  `java-library`
  id("org.metaborg.spoofax.compiler.gradle.language") apply false
  id("org.metaborg.spoofax.compiler.gradle.adapter") apply false
  id("org.metaborg.spoofax.lwb.compiler.gradle.language")
}

val spoofaxVersion: String by ext

dependencies {
  api("com.google.code.findbugs:jsr305:3.0.2")
  api("org.metaborg:pie.task.java:999.9.9-develop-SNAPSHOT")

  // annotationProcessor("com.google.code.findbugs:jsr305:3.0.2")

  testImplementation("org.metaborg:spoofax.test:$spoofaxVersion")
  testRuntimeOnly(project(":aml.test.runner"))
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

task<JavaExec>("runSpt") {
    main = "mb.aml.test.runner.AMLSptTestRunner"
    classpath = java.sourceSets["test"].runtimeClasspath
    args = project.runSptArgs()
}

fun Project.runSptArgs(): List<String> {
  val hasProp = project.hasProperty("sptPath");
  val command = if (project.hasProperty("sptPath") && project.properties["sptPath"].toString().endsWith(".spt")) "runTestSuite" else "runTestSuites"
  val path = if (project.hasProperty("sptPath")) project.properties["sptPath"].toString() else "test/self/options"
  return listOf(command, "${project.rootDir}/${project.relativePath(path)}")
}
