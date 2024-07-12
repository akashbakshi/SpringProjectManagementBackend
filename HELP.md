# Pira (Project Management App)

### Overview

This project is used to demonstrate my web dev abilities. This is meant to be a lightweight JIRA/Trello clone. It allows you to create multiple projects with multiple boards. 

### Technologies Used
* Spring Boot 3
* Docker
* Docker Compose
* JWT Authentication
* Spring Data JPA
* Postgres
* RabbitMQ Messaging

### Setup

If you would like to test this out yourself, first run:

	`docker-compose up -d`

This will setup the postgres database, the Pira App and RabbitMQ instance.

Then the application will be available on port localhost:8080.

You can check out some of the client applications made that utilize this API and provide a UI.

* Android project:
* ReactJS project: