FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY . .

# The repository may be checked out with Windows line endings. Normalize the
# wrapper before executing it in the Linux build image.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
# The integration tests require Docker, so they are not run inside this build
# image. Run them on the host/CI before building the deployable image.
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Run the application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

# Spring Boot must listen on the container interface so published ports are
# reachable from outside the container.
ENV SERVER_ADDRESS=0.0.0.0

RUN useradd --system --create-home appuser \
    && chown appuser:appuser /app/app.jar
USER appuser

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
