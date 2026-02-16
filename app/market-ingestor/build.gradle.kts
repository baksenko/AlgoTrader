plugins {
    id("java")
}

group = "algotrader"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared-lib"))

    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation("io.lettuce:lettuce-core:6.2.6.RELEASE")
}

tasks.test {
    useJUnitPlatform()
}