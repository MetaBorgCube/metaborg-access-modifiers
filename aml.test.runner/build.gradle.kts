plugins {
  java
  application
  `maven-publish`
  id("com.github.johnrengelman.shadow") version("7.1.2")
}

val spoofaxVersion: String by ext

dependencies {
  implementation("org.metaborg:pie.runtime:0.21.0")
  implementation("org.metaborg:spt:$spoofaxVersion")
  implementation("org.metaborg:spoofax.cli:$spoofaxVersion")
  implementation(project(":aml"))
}

application {
  mainClass.set("mb.aml.test.runner.AMLSptTestRunner")
}

tasks {
  jar {
    manifest {
      attributes["Main-Class"] = application.mainClass
    }
  }
}
