plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}

kotlin {
  jvmToolchain(17)
  compilerOptions {
    freeCompilerArgs.add("-Xcontext-parameters")
  }
}

application {
  mainClass.set("com.example.remotestatelab.server.ServerKt")
}

dependencies {
  implementation(libs.androidx.compose.remote.creation)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.slf4j.simple)

  testImplementation(kotlin("test"))
}
