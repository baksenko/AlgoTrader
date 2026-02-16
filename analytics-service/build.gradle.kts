// analytics-service: JOOQ Persistence -> REST API (SparkJava)

dependencies {
    implementation(project(":shared-lib"))

    // ── SparkJava (REST API) ────────────────────────────────────────
    implementation("com.sparkjava:spark-core:2.9.4")

    // ── JOOQ (Type-safe SQL) ────────────────────────────────────────
    implementation("org.jooq:jooq:3.19.3")

    // ── PostgreSQL JDBC Driver ──────────────────────────────────────
    implementation("org.postgresql:postgresql:42.7.1")

    // ── HikariCP (Connection Pool) ──────────────────────────────────
    implementation("com.zaxxer:HikariCP:5.1.0")

    // ── Jackson (JSON serialization for REST) ───────────────────────
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")

    // ── Lettuce (Redis client – for reading cached data) ────────────
    implementation("io.lettuce:lettuce-core:6.3.1.RELEASE")
}
