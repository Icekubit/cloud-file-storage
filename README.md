# Clouf file storage

## Overview
This web application serves as a platform for accessing and managing files and folders stored in the cloud. Users can register an account and upload files and folders to their cloud storage space. Additionally, they can perform operations such as downloading files and folders, creating new empty folders, deleting, renaming, and searching for files.

## Usage
1. Register or log in to your account
2. Uploading files and folders
3. Downloading files and folders
4. Creating a new empty folder
5. Deleting
6. Renaming
7. Searching

## Technologies Used
- Frontend: HTML/CSS/Javascript, Bootstrap 5
- Backend: Spring Boot, Spring Security, Spring Sessions, Thymeleaf
- Database: Postgresql, Spring Data JPA, Minio S3, Redis
- Tests: JUnit 5, TestContainers, Mockito

## Local Setup
- To run this project locally, follow these steps:
1. Clone the repository

```shell
git clone https://github.com/Icekubit/cloud-file-storage.git
```

2. Run docker-compose-dev.yml

```shell
docker-compose -f docker-compose-dev.yml up
```

3. Build and run the project:

```shell
./gradlew bootRun
```

- Or you can use docker-compose-review.yml to start the project without using Gradle:

```shell
docker-compose -f docker-compose-review.yml up
```

## Application Access
The application is accessible at http://185.246.118.24:8080/
