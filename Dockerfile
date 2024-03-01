FROM gradle:8.6.0-jdk17 AS build

COPY . /app

WORKDIR /app

RUN gradle bootJar --no-daemon

FROM openjdk:17

COPY --from=build /app/build/libs/*.jar /cloud-file-storage.jar

ENTRYPOINT ["java", "-jar", "/cloud-file-storage.jar"]

