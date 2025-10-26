# GitHub Issue Finder

A Spring Boot REST API service that helps developers find open-source contribution opportunities by discovering GitHub issues marked as "good first issue", "help wanted", and other contribution-friendly labels.

## Features

- 🏷️ **Repository Labels**: Retrieve all labels for a specific GitHub repository
- 🔍 **Organization Labels**: Get all unique labels across all repositories in an organization
- 🎯 **Contribution Issues**: Find all open issues marked for contribution across an organization
- 📚 **OpenAPI Documentation**: Interactive API documentation with Swagger UI
- ⚡ **Pagination Support**: Handles GitHub API pagination automatically

## Tech Stack

- **Kotlin** 2.2.0
- **Spring Boot** 3.5.6
- **Spring Web MVC**
- **Spring Boot Actuator**
- **Spring Boot Validation**
- **SpringDoc OpenAPI** 2.8.13 (Swagger UI)
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
GET /api/v1/labels/{org}/{repo}
```
Retrieves all labels for a specific repository.

**Example:**
```
bash curl http://localhost:8080/api/v1/labels/spring-projects/spring-boot
```

### Get All Labels for an Organization
```
GET /api/v1/labels/{org}
```
Retrieves all unique labels across all repositories in an organization.

**Example:**
```
bash curl http://localhost:8080/api/v1/labels/spring-projects
``` 

### Get Contribution Issues for an Organization
```
GET /api/v1/contribution-issues/{org}
```
Retrieves all open issues marked as open for contribution across all repositories in an organization.

**Example:**
```
bash curl http://localhost:8080/api/v1/contribution-issues/spring-projects
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
http://localhost:8080/
```

OpenAPI specification:
```
http://localhost:8080/v3/api-docs
```

## Error handling
This API uses RFC 9457 Problem Details for error responses. On errors you'll receive a JSON object with fields like `type`, `title`, `status`, `detail`, and optionally an `errors` map for validation issues.

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
### Environment variables
- GITHUB_API_URL: Base URL for the GitHub API (default: https://api.github.com)
- GITHUB_API_TOKEN: Personal Access Token used for authenticated requests to increase rate limits (optional)

### GitHub API Rate Limiting
The GitHub API has rate limits:
- **Unauthenticated requests**: 60 requests per hour
- **Authenticated requests**: 5,000 requests per hour

### Actuator endpoints
The application exposes a minimal set of actuator endpoints without authentication:
- /actuator/health
- /actuator/info
- /actuator/metrics

## Development
### Building
``` bash
./gradlew build
```

### Running Tests
``` bash
./gradlew test
```

## Native Image (GraalVM)
This project is configured to build and run as a native image using GraalVM and Spring Boot 3 AOT.
You can choose one of the following options:

### Option A — Build native binary locally
Prerequisites: GraalVM JDK 21 with `native-image` installed (`gu install native-image`).

```bash
# Build native executable
./gradlew nativeCompile

# Run the native app
./build/native/nativeCompile/github-issue-finder
```

### Option B — Build a tiny native container image with Cloud Native Buildpacks
Prerequisites: Docker or Podman.

```bash
# Build a native container image using buildpacks (Paketobuildpacks)
./gradlew bootBuildImage -Pnative

# Run
docker run --rm -p 8080:8080 \
  -e GITHUB_API_URL=${GITHUB_API_URL:-https://api.github.com} \
  -e GITHUB_API_TOKEN=${GITHUB_API_TOKEN} \
  github-issue-finder:latest
```

### Option C — Build with the provided Dockerfile (multi-stage native build)
No local GraalVM required. Docker builds the native executable inside the builder stage.

```bash
# Build the native image using Dockerfile
docker build -t github-issue-finder:latest .

# Run
docker run --rm -p 8080:8080 \
  -e GITHUB_API_URL=${GITHUB_API_URL:-https://api.github.com} \
  -e GITHUB_API_TOKEN=${GITHUB_API_TOKEN} \
  github-issue-finder:latest
```

### Notes
- Health endpoint: `http://localhost:8080/actuator/health`
- OpenAPI UI: `http://localhost:8080/`

