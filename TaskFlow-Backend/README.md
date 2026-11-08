# TaskFlow Backend

TaskFlow Backend is a REST API built with Java and Spring Boot for managing tasks, priorities, statuses, and due dates.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Docker

## Features

- Create tasks
- Retrieve all tasks
- Retrieve a task by ID
- Update tasks
- Delete tasks
- Manage task priorities
- Manage task statuses
- Persist tasks using MySQL

## REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks` | Retrieve all tasks |
| GET | `/api/tasks/{id}` | Retrieve a task by ID |
| POST | `/api/tasks` | Create a new task |
| PUT | `/api/tasks/{id}` | Update an existing task |
| DELETE | `/api/tasks/{id}` | Delete a task |

## Task Model

A task contains:

- ID
- Title
- Description
- Priority
- Status
- Due date
- Creation timestamp

### Priority Values

- `LOW`
- `MEDIUM`
- `HIGH`

### Status Values

- `TODO`
- `IN_PROGRESS`
- `COMPLETED`

## Database Configuration

The application reads database settings from environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

This allows the same application to connect to different database environments without storing credentials in source control.

## Docker

The backend includes a Dockerfile and can be packaged as a containerized Spring Boot application.

Example:

```bash
docker build -t taskflow-backend .
```

## AWS Deployment

For the AWS deployment, TaskFlow Backend runs inside a Docker container on Amazon EC2.

The production database is Amazon RDS for MySQL running privately inside the application's VPC.

The React production build is bundled into the Spring Boot static resources for the two-tier AWS deployment.

### Production Architecture

```text
Internet
   |
   v
Amazon EC2
Docker Container
Spring Boot + React
   |
   v
Amazon RDS MySQL
Private DB Subnets
```

The detailed AWS deployment and cloud transformation documentation is maintained separately in the `AWS-Cloud-Projects` repository.
