# GitHub Issue Finder

A Spring Boot REST API service that helps developers find open-source contribution opportunities by discovering GitHub issues marked as "good first issue", "help wanted", and other contribution-friendly labels.

## Features

- 🏷️ **Repository Labels**: Retrieve all labels for a specific GitHub repository
- 🔍 **Organization Labels**: Get all unique labels across all repositories in an organization
- 🎯 **Contribution Issues**: Find all open issues marked for contribution across an organization
- 📚 **OpenAPI Documentation**: Interactive API documentation with Swagger UI
- ⚡ **Pagination Support**: Handles GitHub API pagination automatically

## Tech Stack

- **Kotlin** 1.9.22
- **Spring Boot** 3.4.2
- **Spring Web MVC**
- **SpringDoc OpenAPI** 2.8.13 (Swagger UI)
- **GitHub API** 1.326
- **Java** 21

## Getting Started

### Prerequisites

- Java 21 or higher
- Git

### Installation

1. Clone the repository:
   bash git clone [https://github.com/yourusername/github-issue-finder.git](https://github.com/yourusername/github-issue-finder.git) cd github-issue-finder
2. Build the project:
   bash ./gradlew build
3. Run the application:
   bash ./gradlew bootRun

The application will start on `http://localhost:8080`

## API Endpoints

### Get Labels for a Repository
```
http GET /labels/{org}/{repo}
```
Retrieves all labels for a specific repository.

**Example:**
```
bash curl http://localhost:8080/labels/spring-projects/spring-boot
```

### Get All Labels for an Organization
```
http GET /labels/{org}
```
Retrieves all unique labels across all repositories in an organization.

**Example:**
```
bash curl http://localhost:8080/labels/spring-projects
``` 

### Get Contribution Issues for an Organization
```
http GET /contribution-issues/{org}
```
Retrieves all open issues marked as open for contribution across all repositories in an organization.

**Example:**
```
bash curl http://localhost:8080/contribution-issues/spring-projects
``` 

**Response:**
```json
[
  {
    "url": "https://api.github.com/repos/spring-projects/spring-boot/issues/12345",
    "htmlUrl": "https://github.com/spring-projects/spring-boot/issues/12345",
    "title": "Add support for custom configuration properties",
    "state": "open",
    "comments": 5,
    "labels": ["good first issue", "help wanted", "enhancement"]
  }
]
```

## Swagger UI Documentation
Interactive API documentation is available at:
``` 
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```

## Supported Contribution Labels
Since there is no uniform convention for marking issues as contribution ready. This service filters issues based on a hardcoded list of labels.
The service recognizes the following labels as contribution-friendly:
- ideal-for-contribution
- ideal-for-user-contribution
- status: ideal-for-contribution
- status: first-timers-only
- status/first-timers-only
- community contribution
- contribution welcome
- help wanted
- first-timers-only
- good first issue
- type/help-needed

## Configuration
### GitHub API Rate Limiting
The GitHub API has rate limits:
- **Unauthenticated requests**: 60 requests per hour
- **Authenticated requests**: 5,000 requests per hour

## Development
### Building
``` bash
./gradlew build
```

### Running Tests
``` bash
./gradlew test
```

