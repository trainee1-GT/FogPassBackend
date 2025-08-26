# 🚀 EOEL-FogPASS Master Data Management API

## 📋 Overview

This document provides comprehensive information about the Master Data Management module implementation for the EOEL-FogPASS project. The implementation follows production-grade architectural patterns with complete CRUD operations for Zone and Division entities.

## 🏗️ Architecture Overview

### Core Architectural Components

1. **Auditing**: All entities extend `BaseEntity` with automatic audit fields (createdAt, updatedAt, createdBy, updatedBy)
2. **Error Handling**: Global exception handler with consistent `ApiResponse` format
3. **DTO Pattern**: Strict separation between Request/Response DTOs and internal Entities
4. **Mapping**: MapStruct for all DTO-to-Entity conversions
5. **API Documentation**: Complete Swagger/OpenAPI documentation
6. **REST Standards**: Proper HTTP status codes, Location headers, PATCH semantics

### Project Structure

```
src/main/java/train/local/fogpass/
├── config/
│   ├── AuditConfig.java              # JPA Auditing configuration
│   └── OpenApiConfig.java            # Swagger/OpenAPI configuration
├── controller/
│   ├── ZoneController.java           # Zone REST endpoints
│   └── DivisionController.java       # Division REST endpoints
├── dto/
│   ├── masterdata/
│   │   ├── ZoneResponse.java         # Zone response DTO
│   │   ├── ZoneCreateRequest.java    # Zone creation DTO
│   │   ├── ZoneUpdateRequest.java    # Zone update DTO (PATCH)
│   │   ├── DivisionResponse.java     # Division response DTO
│   │   ├── DivisionCreateRequest.java # Division creation DTO
│   │   └── DivisionUpdateRequest.java # Division update DTO (PATCH)
│   └── response/
│       ├── ApiResponse.java          # Standard API response wrapper
│       └── PageResponse.java         # Pagination response wrapper
├── entity/
│   ├── BaseEntity.java               # Base entity with audit fields
│   ├── Zone.java                     # Zone entity (extends BaseEntity)
│   └── Division.java                 # Division entity (extends BaseEntity)
├── exception/
│   ├── GlobalExceptionHandler.java   # Global exception handling
│   ├── ResourceNotFoundException.java # 404 exception
│   └── BadRequestException.java      # 400 exception
├── mapper/
│   ├── ZoneMapper.java               # MapStruct mapper for Zone
│   └── DivisionMapper.java           # MapStruct mapper for Division
├── repository/
│   ├── ZoneRepository.java           # Zone JPA repository
│   └── DivisionRepository.java       # Division JPA repository
└── service/
    ├── ZoneService.java              # Zone service interface
    ├── DivisionService.java          # Division service interface
    └── impl/
        ├── ZoneServiceImpl.java      # Zone service implementation
        └── DivisionServiceImpl.java  # Division service implementation
```

## 🛠️ Setup Instructions

### Prerequisites

- Java 21
- Maven 3.6+
- MySQL 8.0+
- Postman (for API testing)

### Database Setup

```sql
CREATE DATABASE fogpass_db;
CREATE USER 'fogpass_user'@'localhost' IDENTIFIED BY 'fogpass_password';
GRANT ALL PRIVILEGES ON fogpass_db.* TO 'fogpass_user'@'localhost';
FLUSH PRIVILEGES;
```

### Application Configuration

Ensure your `application.properties` contains:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/fogpass_db
spring.datasource.username=fogpass_user
spring.datasource.password=fogpass_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration
app.jwt.secret=mySecretKey
app.jwt.expiration=86400000
```

### Build and Run

```bash
# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation at:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Authentication

All master data endpoints require authentication. Use the login endpoint to obtain a JWT token:

```bash
POST /api/auth/login
Content-Type: application/json

{
    "username": "superadmin",
    "password": "admin123"
}
```

## 🧪 API Testing with Postman

### Import Collection

1. Import the provided `Master_Data_API.postman_collection.json` file into Postman
2. Set up environment variables:
   - `baseUrl`: http://localhost:8080
   - `authToken`: (will be auto-populated after login)

### Testing Sequence

1. **Authentication**
   - Run "Login" request
   - JWT token will be automatically saved to `authToken` variable

2. **Zone Management**
   - Create zones using "Create Zone"
   - Test pagination with "Get All Zones (Paginated)"
   - Test search functionality with "Search Zones"
   - Update zones using "Update Zone (PATCH)"

3. **Division Management**
   - Create divisions using "Create Division"
   - Test zone-based filtering with "Get Divisions by Zone"
   - Test search functionality with "Search Divisions"
   - Update divisions using "Update Division (PATCH)"

## 🔧 API Endpoints

### Zone Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/master-data/zones` | Create new zone | ✅ |
| GET | `/api/master-data/zones` | Get all zones (paginated) | ✅ |
| GET | `/api/master-data/zones?all=true` | Get all zones (no pagination) | ✅ |
| GET | `/api/master-data/zones?name=X&code=Y` | Search zones | ✅ |
| GET | `/api/master-data/zones/{id}` | Get zone by ID | ✅ |
| PATCH | `/api/master-data/zones/{id}` | Update zone (partial) | ✅ |
| DELETE | `/api/master-data/zones/{id}` | Delete zone | ✅ |

### Division Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/master-data/divisions` | Create new division | ✅ |
| GET | `/api/master-data/divisions` | Get all divisions (paginated) | ✅ |
| GET | `/api/master-data/divisions?zoneId=1` | Get divisions by zone | ✅ |
| GET | `/api/master-data/divisions?name=X&code=Y` | Search divisions | ✅ |
| GET | `/api/master-data/divisions/{id}` | Get division by ID | ✅ |
| PATCH | `/api/master-data/divisions/{id}` | Update division (partial) | ✅ |
| DELETE | `/api/master-data/divisions/{id}` | Delete division | ✅ |

## 📊 Response Formats

### Standard API Response

```json
{
    "success": true,
    "message": "Operation completed successfully",
    "data": { ... }
}
```

### Paginated Response

```json
{
    "success": true,
    "message": "Data retrieved successfully",
    "data": {
        "content": [...],
        "pageNumber": 0,
        "pageSize": 20,
        "totalElements": 100,
        "totalPages": 5,
        "first": true,
        "last": false
    }
}
```

### Error Response

```json
{
    "success": false,
    "message": "Error description",
    "data": null
}
```

## 🔒 Security Features

1. **JWT Authentication**: All endpoints require valid JWT token
2. **Role-Based Access**: Only ADMIN and SUPER_ADMIN roles can access master data
3. **Input Validation**: Comprehensive validation using Bean Validation
4. **SQL Injection Protection**: JPA/Hibernate provides protection
5. **Audit Trail**: All changes are tracked with user and timestamp

## 🚨 Error Handling

The application provides comprehensive error handling:

- **400 Bad Request**: Invalid input data, validation failures
- **401 Unauthorized**: Missing or invalid JWT token
- **403 Forbidden**: Insufficient permissions
- **404 Not Found**: Resource not found
- **409 Conflict**: Duplicate data (name/code already exists)
- **500 Internal Server Error**: Unexpected server errors

## 📈 Performance Features

1. **Lazy Loading**: Related entities are loaded on-demand
2. **Database Indexing**: Proper indexes on foreign keys and search fields
3. **Pagination**: All list endpoints support pagination
4. **Caching**: JPA second-level cache can be enabled
5. **Connection Pooling**: HikariCP for efficient database connections

## 🧪 Testing Examples

### Create Zone

```bash
POST /api/master-data/zones
Authorization: Bearer {token}
Content-Type: application/json

{
    "name": "Western Railway Zone",
    "code": "WR",
    "description": "Western Railway Zone covering Mumbai and surrounding areas"
}
```

### Create Division

```bash
POST /api/master-data/divisions
Authorization: Bearer {token}
Content-Type: application/json

{
    "name": "Mumbai Division",
    "code": "MUM",
    "description": "Mumbai Division covering local and suburban routes",
    "zoneId": 1
}
```

### Update Zone (PATCH)

```bash
PATCH /api/master-data/zones/1
Authorization: Bearer {token}
Content-Type: application/json

{
    "description": "Updated description for Western Railway Zone"
}
```

## 🔄 PATCH vs PUT Semantics

This implementation uses **PATCH semantics** for updates:

- Only non-null fields in the request are updated
- Null fields are ignored (existing values preserved)
- This allows partial updates without affecting other fields

## 📝 Validation Rules

### Zone Validation

- **name**: Required, 2-100 characters, must be unique
- **code**: Required, 2-10 characters, must be unique
- **description**: Optional, max 500 characters

### Division Validation

- **name**: Required, 2-100 characters, must be unique within zone
- **code**: Required, 2-10 characters, must be unique within zone
- **description**: Optional, max 500 characters
- **zoneId**: Required, must reference existing zone

## 🚀 Future Enhancements

1. **Caching**: Implement Redis caching for frequently accessed data
2. **Bulk Operations**: Add endpoints for bulk create/update/delete
3. **Export/Import**: Add CSV/Excel export/import functionality
4. **Audit Log**: Detailed audit log with change history
5. **Soft Delete**: Implement soft delete instead of hard delete
6. **Search Enhancement**: Full-text search capabilities
7. **Rate Limiting**: API rate limiting for production use

## 🐛 Troubleshooting

### Common Issues

1. **Compilation Errors**: Ensure MapStruct processor is configured correctly
2. **Database Connection**: Verify MySQL is running and credentials are correct
3. **JWT Token**: Ensure token is included in Authorization header
4. **Validation Errors**: Check request body format and required fields
5. **Unique Constraints**: Zone/Division names and codes must be unique

### Debug Tips

1. Enable SQL logging: `spring.jpa.show-sql=true`
2. Check application logs for detailed error messages
3. Use Swagger UI for interactive testing
4. Verify database schema is created correctly
5. Test with Postman collection for comprehensive validation

## 📞 Support

For technical support or questions about the Master Data API:

1. Check the Swagger documentation at `/swagger-ui/index.html`
2. Review the Postman collection for usage examples
3. Check application logs for detailed error information
4. Verify database connectivity and schema

---

**Happy Coding! 🚀**