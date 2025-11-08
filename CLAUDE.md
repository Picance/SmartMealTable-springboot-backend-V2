# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmartMealTable is a budget management and restaurant recommendation service built with Spring Boot. The system helps users manage their meal expenses, track budgets, and receive personalized restaurant recommendations.

## Development Commands

### Build and Test
```bash
# Build all modules
./gradlew build

# Run tests for all modules
./gradlew test

# Run tests for specific module
./gradlew :smartmealtable-api:test
./gradlew :smartmealtable-domain:test
./gradlew :smartmealtable-storage:db:test

# Run REST Docs tests only
./gradlew :smartmealtable-api:test --tests '*RestDocsTest'

# Generate API documentation
./gradlew :smartmealtable-api:asciidoctor
```

### Local Development
```bash
# Start local development environment
docker-compose -f docker-compose.local.yml up -d

# Run API server locally
./gradlew :smartmealtable-api:bootRun

# Run admin server locally
./gradlew :smartmealtable-admin:bootRun
```

### Docker Operations
```bash
# Build and run all services
./deploy-all.sh

# Build and run API server only
./deploy-api.sh

# Build and run admin server only
./deploy-admin.sh

# Build and run batch server only
./deploy-batch.sh
```

## Architecture Overview

### Multi-Module Structure
The project follows a layered architecture with clear separation of concerns:

```
smartmealtable/
├── smartmealtable-core/          # Common responses, error handling, exceptions
├── smartmealtable-domain/         # Domain models, business logic, domain services
├── smartmealtable-storage/        # Persistence layer
│   ├── db/                        # JPA entities, repositories, QueryDSL
│   └── cache/                     # Redis caching
├── smartmealtable-api/            # REST API controllers, application services
├── smartmealtable-admin/          # Admin interface and APIs
├── smartmealtable-client/         # External API clients
│   ├── auth/                      # OAuth clients (Kakao, Google)
│   └── external/                  # External service clients (Naver Map, etc.)
├── smartmealtable-batch/          # Batch processing jobs
│   └── crawler/                    # Web crawling functionality
├── smartmealtable-scheduler/      # Scheduled tasks
├── smartmealtable-recommendation/ # AI-powered recommendation system
└── smartmealtable-support/        # Cross-cutting concerns
    ├── logging/                   # Logging utilities
    └── monitoring/                # Monitoring and metrics
```

### Key Architectural Decisions

1. **No Spring Security**: Authentication is handled using custom JWT implementation without Spring Security
2. **Domain-Driven Design**: Business logic is concentrated in domain entities and services
3. **TDD Approach**: Test-driven development with RED-GREEN-REFACTOR cycle
4. **TestContainers**: All tests use TestContainers for consistent testing environment
5. **No Physical Foreign Keys**: Database uses logical foreign keys only for flexibility
6. **Stateless JWT Authentication**: Custom JWT implementation with ArgumentResolver for user context

### Module Dependencies

```
api → core, domain, storage, client, support, recommendation
admin → core, domain, storage, client, support
domain → core
storage:db → domain
storage:cache → core
client:auth → core
client:external → core
batch:crawler → domain, core
scheduler → domain, core
recommendation → domain, core
support → (no domain dependencies)
```

## Development Guidelines

### Code Conventions

1. **DTO Usage**: All inter-layer communication must use DTOs
   - Controller → Application Service: `XxxServiceRequest`
   - Application Service → Controller: `XxxServiceResponse`
   - DTOs should be placed in the dependent module

2. **Entity Mapping**: Avoid JPA associations except within the same aggregate
   - Use foreign key fields instead of `@ManyToOne`, `@OneToMany`
   - Keep entities simple and focused on persistence

3. **No Setters/Data**: Prohibit `@Setter` and `@Data` annotations except in DTOs
   - Use constructor injection and immutable objects where possible

4. **Query Strategy**: Use QueryDSL for complex queries instead of long query method names

### Testing Strategy

1. **TestContainers**: All integration tests must use TestContainers
   - Tests run sequentially (not parallel) to avoid resource conflicts
   - Never switch to H2 or local Docker MySQL for tests

2. **Test Coverage**:
   - Test both happy paths and error scenarios
   - Include all HTTP status codes (400, 404, 422, 500)
   - Test edge cases and boundary values
   - Use Mockist style with Mockito

3. **Test Independence**: Each test must be independent and isolated

### API Design

1. **URL Structure**: All APIs follow `/api/v1/` pattern with versioning
2. **Response Format**: All responses use `ApiResponse<T>` wrapper
3. **Documentation**: Use Spring REST Docs for API documentation
4. **Error Handling**: Use ControllerAdvice for consistent error responses

### Authentication

1. **JWT Implementation**: Custom JWT without Spring Security
2. **User Context**: Use `ArgumentResolver` to inject `AuthenticatedUser` into controllers
3. **Token Management**: Stateless JWT with refresh token rotation

## Database Schema

### Key Design Principles

1. **Audit Fields**: Use database default timestamps for `created_at`, `updated_at`
   - Do not expose audit fields in entities or domain models
   - Only business-meaningful timestamps (like `registered_at`) should be modeled

2. **Logical Foreign Keys**: No physical foreign key constraints
   - Maintain referential integrity at application level
   - Use foreign key fields as simple values

3. **Aggregate Boundaries**: Design entities around DDD aggregate boundaries
   - Only use associations within the same aggregate
   - Cross-aggregate references use ID fields

## Important Notes

1. **Legacy Code**: Some functionality exists in `legacy/src/main/java/com/stcom/smartmealtable/`
   - Credit message parsing code can be reused from `component/creditmessage/`
   - Do not modify legacy code without careful consideration

2. **Implementation Progress**: The `IMPLEMENTATION_PROGRESS.md` document may not be 100% accurate
   - Always verify actual implementation status before starting work
   - Update the document when completing features

3. **Module Responsibilities**:
   - **api**: REST controllers, application services, web configuration
   - **domain**: Domain entities, domain services, repository interfaces
   - **storage**: JPA entities, repository implementations, database configuration
   - **core**: Common exceptions, API responses, error codes
   - **client**: External API integrations, OAuth clients
   - **batch**: Background processing jobs, data crawling
   - **support**: Logging, monitoring, utilities
   - **recommendation**: AI-powered recommendation algorithms

4. **Environment Variables**: Required environment variables are documented in `.env.example`
   - OAuth credentials (Kakao, Google)
   - Vertex AI (Gemini) configuration
   - Database and Redis connection settings
   - Naver Map API keys

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.4.10, Spring MVC
- **Data**: Spring Data JPA, QueryDSL, MySQL 8.0
- **Cache**: Redis
- **AI**: Spring AI (Vertex AI Gemini)
- **Test**: JUnit 5, Mockito, TestContainers
- **Docs**: Spring REST Docs, AsciiDoc
- **Build**: Gradle 8.x
- **Infra**: Docker, Docker Compose, Terraform
- **CI/CD**: GitHub Actions