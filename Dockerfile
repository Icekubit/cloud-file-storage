FROM openjdk:17

COPY build/libs/cloud-file-storage.jar cloud-file-storage.jar

ENTRYPOINT ["java", "-jar", "/cloud-file-storage.jar"]

