rootProject.name = "aml-project"

pluginManagement {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenLocal()        
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        maven("https://artifacts.metaborg.org/content/groups/public/")
        mavenLocal()
    }
    dependencies {
        classpath("org.metaborg:gradle.config:0.4.7")
    }
}

plugins {
    id("com.gradle.enterprise") version("3.10.1")
}

include(":aml")
include(":aml.test.runner")

gradleEnterprise {
    buildScan {
        // Publish the build scan and agree to the TOS
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
