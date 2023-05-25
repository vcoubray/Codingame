plugins {
    kotlin("jvm") version "1.7.10"
}

repositories {
    mavenCentral()
}

subprojects {
    tasks.register<Codingame>("codingame")
}

