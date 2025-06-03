# LHIND ShortenUrl-System backend project
The main objective of this application is to provide APIs for creating, shortening, reading URLs in the system.

## Technologies used for this application are:

- Java 17
- Spring Boot 3.2.5
- Spring Data Jpa
- JWT Authentication
- PostgreSQL DB
- Docker
- Swagger


### Things to do before running the App

- execute the docker compose file inside docker/docker-compose.yml (use command docker compose up) in order to set up the DB.


### Explaining third party libraries used in project
1- Docker – I've used docker to containerize the PostgreSQL database, ensuring a consistent and portable environment across different environments.

2- Lombok – Lombok was used extensively to reduce boilerplate code such as getters, setters, constructors, and builders.

3- Hibernate Validators - To ensure data integrity and prevent invalid input from propagating through the system, I applied Java Bean Validation (JSR 380) using annotations like @NotNull, @Email, and @Size. This made the request handling layer more reliable and secure.

4- Automatic Cron Job - To do periodic checks in the database and clean up from expired URLs.

5- Swagger (Springdoc OpenAPI) – Interactive API Documentation
I integrated Swagger to auto-generate comprehensive and interactive documentation for all RESTful APIs, 
Swagger url: (http://localhost:${server_port}/swagger-ui/index.html)


### Configuration settings

| Config file parameter         | Env variable  | To be configured | Default Value | Description                                                                                                           |
|-------------------------------|---------------|:----------------:|---------------|-----------------------------------------------------------------------------------------------------------------------|
| server.port                   | server_port   |                  | 8080          | Application port (HTTP protocol )                                                                                     |
| spring.datasource.username    | db_username   |        Y         | user          | Postgres user                                                                                                         |
| spring.datasource.password    | db_password   |        Y         | p@5Sw0Rd      | Postgres password                                                                                                     |
| spring.datasource.url         | db_url        |        Y         | 172.28.0.1    | Postgres host, (is the host that will be configured on docker file, docker container that will be created for the db) |
| spring.datasource.url.port    | db_port       |        Y         | 5432          | Postgres port                                                                                                         |
| spring.datasource.url.db_name | db_name       |        Y         | shorturidb    | Postgres DB name                                                                                                      |
