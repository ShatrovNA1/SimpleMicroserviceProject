plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "com.ecommerce"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

extra["springCloudVersion"] = "2025.1.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql")

    // Eureka Client
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Config Client
    implementation("org.springframework.cloud:spring-cloud-starter-config")

    // OpenFeign для межсервисного взаимодействия
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")

    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Micrometer для трассировки
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}

