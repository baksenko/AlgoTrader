// execution-service: Signals -> Order Execution (Paper Trading)

dependencies {
    implementation(project(":shared-lib"))

    // ── Lettuce (Redis client) ──────────────────────────────────────
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")

    // ── Jackson (JSON serialization) ────────────────────────────────
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
}
