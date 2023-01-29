plugins {
  id("org.jetbrains.kotlin.jvm") version "1.8.0"
  id("org.jetbrains.dokka") version "1.6.0"
}

kotlin { jvmToolchain(18) }

dependencies {
  implementation(project(":pets"))

  implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.0")

  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
  testImplementation("com.google.truth:truth:1.1.3")

  testImplementation(project(mapOf("path" to ":engine")))
  testImplementation(project(mapOf("path" to ":repl")))
}

sourceSets {
  val main by getting {
    resources {
      srcDir("src/main/kotlin")
      exclude("**/*.kt")
    }
  }
}
