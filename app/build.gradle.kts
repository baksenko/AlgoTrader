plugins {
    id("java")
    id("groovy")
}

group = "algotrader"
version = "1.0-SNAPSHOT"

subprojects {
    apply(plugin = "java")
    apply(plugin = "groovy")

    repositories {
        mavenCentral()
    }

    dependencies {

        testImplementation("org.apache.groovy:groovy:4.0.15")

        testImplementation("org.spockframework:spock-core:2.4-M1-groovy-4.0")

        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}