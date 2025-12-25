FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY config-server/build.gradle.kts config-server/
COPY config-server/src config-server/src
COPY config-repo config-repo
RUN chmod +x gradlew
RUN ./gradlew :config-server:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/build/libs/*.jar app.jar
COPY --from=build /app/config-repo /app/config-repo
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]

