plugins {
  id("java")
  id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "io.github.viimeinen1.alib"
version = "0.1.0"

repositories {
  mavenCentral()
  maven {
    name = "papermc"
    url = uri("https://repo.papermc.io/repository/maven-public/")
  }
  maven {
    url = uri("https://maven.enginehub.org/repo/")
  }
}

dependencies {
  compileOnly("io.papermc.paper:paper-api:26.2.build.+")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.17") {
    exclude("com.google.guava")
    exclude("com.google.code.gson")
  }
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

mavenPublishing {
  publishToMavenCentral()
  signAllPublications()
}

mavenPublishing {
  coordinates(group.toString(), name, version.toString())

  pom {
    name.set("aLib")
    description.set("General library plugin")
    inceptionYear.set("2026")
    url.set("https://github.com/viimeinen1/aLib/")
    licenses {
      license {
        name.set("MIT")
        url.set("https://opensource.org/licenses/MIT")
      }
    }
    developers {
      developer {
        id.set("viimeinen1")
        name.set("viimeinen1")
        url.set("https://github.com/viimeinen1/")
      }
    }
    scm {
      url.set("https://github.com/viimeinen1/aLib/")
    }
  }
}