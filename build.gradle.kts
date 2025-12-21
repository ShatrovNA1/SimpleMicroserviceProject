plugins {
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "com.ecommerce"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}


