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
    // Spring Cloud Gateway (reactive WebFlux)
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")

    // Spring WebFlux (reactive web)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Eureka Client для Service Discovery
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")

    // Config Client для централизованной конфигурации
    implementation("org.springframework.cloud:spring-cloud-starter-config")

    // Actuator для health checks и метрик
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Resilience4j для Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")

    // JWT для аутентификации
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Micrometer для трассировки
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.test {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = true
}

tasks.jar {
    enabled = false
}

