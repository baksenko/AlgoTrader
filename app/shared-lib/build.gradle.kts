plugins {
    id("java")
}

group = "algotrader"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

tasks.test {
    useJUnitPlatform()
}