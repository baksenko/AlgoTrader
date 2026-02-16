plugins {
    java
    groovy
}

allprojects {
    group = "com.algotrader"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "groovy")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
    }

    dependencies {
        // ── Groovy (required by Spock) ──────────────────────────────────
        implementation("org.apache.groovy:groovy:4.0.15")

        // ── Spock Framework (BDD Testing) ───────────────────────────────
        testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")

        // ── JUnit 5 Platform (Spock 2.x runs on it) ────────────────────
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")

        // ── Lombok (optional – prefer Records for DTOs) ────────────────
        compileOnly("org.projectlombok:lombok:1.18.30")
        annotationProcessor("org.projectlombok:lombok:1.18.30")

        // ── SLF4J + Logback (standard logging) ─────────────────────────
        implementation("org.slf4j:slf4j-api:2.0.11")
        runtimeOnly("ch.qos.logback:logback-classic:1.4.14")
    }

    tasks.test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
            showStandardStreams = true
        }
    }
}
