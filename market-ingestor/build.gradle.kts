// market-ingestor: Binance API -> Redis (Pub/Sub)

dependencies {
    implementation(project(":shared-lib"))

    // ── SparkJava (Health endpoint) ─────────────────────────────────
    implementation("com.sparkjava:spark-core:2.9.4")

    // ── Lettuce (Redis client) ──────────────────────────────────────
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")

    // ── Jackson (JSON serialization) ────────────────────────────────
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
}
