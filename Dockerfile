# syntax=docker/dockerfile:1
FROM gradle:7.6.1-jdk17-alpine AS builder
WORKDIR /app
COPY build.gradle.kts .
COPY gradle.properties .
COPY ./library ./library
COPY settings.gradle.kts.build settings.gradle.kts
RUN echo 'include("apps:meeting-service")' >> settings.gradle.kts
COPY ./apps/meeting-service ./apps/meeting-service
RUN --mount=type=cache,id=gradle,target=/root/.gradle \
    --mount=type=cache,id=gradle,target=/home/gradle/.gradle \
    gradle :apps:meeting-service:bootJar --no-daemon

FROM openjdk:17-alpine
EXPOSE 8109
WORKDIR /app
COPY --from=builder /app/apps/meeting-service/build/libs/*.jar meeting-service.jar
ENTRYPOINT ["java", "-jar" ,"meeting-service.jar"]
