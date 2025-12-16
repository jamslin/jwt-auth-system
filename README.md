# 🔐 JWT Authentication System

A complete authentication system with a Spring Boot backend and HTML frontend.

## 📁 Project Structure

```
auth-system/
├── frontend/
│   └── index.html          # HTML page for testing API
│
└── backend/
    ├── pom.xml             # Maven dependencies
    └── src/main/
        ├── resources/
        │   └── application.yml    # Configuration
        │
        └── java/com/example/authapi/
            ├── AuthApiApplication.java     # Main class
            │
            ├── config/
            │   ├── SecurityConfig.java     # Security rules
            │   └── DataInitializer.java    # Test data
            │
            ├── controller/
            │   ├── AuthController.java     # Auth endpoints
            │   └── TestController.java     # Test endpoints
            │
            ├── dto/
            │   ├── LoginRequest.java
            │   ├── RegisterRequest.java
            │   ├── AuthResponse.java
            │   └── RefreshTokenRequest.java
            │
            ├── entity/
            │   └── User.java               # User entity
            │
            ├── exception/
            │   ├── AuthException.java
            │   └── GlobalExceptionHandler.java
            │
            ├── repository/
            │   └── UserRepository.java     # Database access
            │
            ├── security/
            │   ├── JwtService.java         # Token operations
            │   ├── JwtAuthenticationFilter.java
            │   └── CustomUserDetailsService.java
            │
            └── service/
                └── AuthService.java        # Business logic
```

---

## 🔑 How JWT Authentication Works

### What is JWT?

JWT (JSON Web Token) is a compact, URL-safe token format used for authentication.

```
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIiwiaWF0IjoxNzA1MzI0ODAwfQ.signature
|_____HEADER_____|._________PAYLOAD________|.____SIGNATURE____|
```

### Authentication Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        REGISTRATION                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Client                              Server                     │
│     │                                   │                        │
│     │ ──POST /api/auth/register──────►  │                        │
│     │   {username, email, password}     │                        │
│     │                                   │                        │
│     │                          ┌────────┴────────┐               │
│     │                          │ 1. Validate     │               │
│     │                          │ 2. Hash password│               │
│     │                          │ 3. Save to DB   │               │
│     │                          │ 4. Generate JWT │               │
│     │                          └────────┬────────┘               │
│     │                                   │                        │
│     │ ◄────── {token, refreshToken} ────│                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                           LOGIN                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Client                              Server                     │
│     │                                   │                        │
│     │ ──POST /api/auth/login─────────►  │                        │
│     │   {username, password}            │                        │
│     │                                   │                        │
│     │                          ┌────────┴────────┐               │
│     │                          │ 1. Find user    │               │
│     │                          │ 2. Verify hash  │               │
│     │                          │ 3. Generate JWT │               │
│     │                          └────────┬────────┘               │
│     │                                   │                        │
│     │ ◄────── {token, refreshToken} ────│                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                    ACCESSING PROTECTED RESOURCE                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   Client                              Server                     │
│     │                                   │                        │
│     │ ──GET /api/protected─────────────►│                        │
│     │   Authorization: Bearer <token>   │                        │
│     │                                   │                        │
│     │                          ┌────────┴────────┐               │
│     │                          │ JwtFilter:      │               │
│     │                          │ 1. Extract JWT  │               │
│     │                          │ 2. Validate sig │               │
│     │                          │ 3. Check expiry │               │
│     │                          │ 4. Load user    │               │
│     │                          │ 5. Set auth     │               │
│     │                          └────────┬────────┘               │
│     │                                   │                        │
│     │ ◄────── {protected data} ─────────│                        │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+

### Run the Backend

```bash
cd backend
mvn spring-boot:run
```

The server starts at `http://localhost:8080`

### Test with the Frontend

1. Open `frontend/index.html` in your browser
2. Use the pre-created test accounts:
   - **User:** `user` / `user123`
   - **Admin:** `admin` / `admin123`

---

## 📚 API Endpoints

### Public Endpoints (No Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Create new account |
| POST | `/api/auth/login` | Login and get tokens |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/public/hello` | Public test endpoint |

### Protected Endpoints (Auth Required)

| Method | Endpoint | Required Role | Description |
|--------|----------|---------------|-------------|
| GET | `/api/test` | Any authenticated | Test protected endpoint |
| GET | `/api/profile` | Any authenticated | Get current user profile |
| GET | `/api/user/dashboard` | USER or ADMIN | User dashboard |
| GET | `/api/admin/dashboard` | ADMIN only | Admin dashboard |

---

## 🧪 Testing with cURL

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","password":"secret123"}'
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'
```

### Access Protected Endpoint

```bash
# Replace <token> with the actual JWT from login response
curl -X GET http://localhost:8080/api/test \
  -H "Authorization: Bearer <token>"
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<your-refresh-token>"}'
```

---

## 🔧 Key Components Explained

### 1. SecurityConfig.java

Defines security rules:
- Which endpoints are public vs protected
- Password encoding (BCrypt)
- Session management (stateless for JWT)
- CORS configuration

### 2. JwtService.java

Handles all JWT operations:
- `generateAccessToken()` - Create short-lived access token
- `generateRefreshToken()` - Create long-lived refresh token
- `isTokenValid()` - Validate token signature and expiration
- `extractUsername()` - Get username from token

### 3. JwtAuthenticationFilter.java

Intercepts every request:
1. Extracts JWT from `Authorization: Bearer <token>` header
2. Validates the token
3. Loads user from database
4. Sets authentication in SecurityContext

### 4. AuthService.java

Business logic for:
- Registration (validation, password hashing, save user)
- Login (authenticate, generate tokens)
- Token refresh (validate refresh token, issue new access token)

---

## 🔒 Security Best Practices

### Implemented

✅ Password hashing with BCrypt  
✅ Short-lived access tokens (15 min)  
✅ Refresh token rotation  
✅ Stateless sessions  
✅ CORS configuration  
✅ Input validation  
✅ Global exception handling  

### Production Recommendations

1. **Store JWT secret in environment variable**
   ```yaml
   jwt:
     secret: ${JWT_SECRET}
   ```

2. **Use HTTPS only**

3. **Restrict CORS origins**
   ```java
   configuration.setAllowedOrigins(List.of("https://yourdomain.com"));
   ```

4. **Add rate limiting** for login attempts

5. **Store refresh tokens in database** with revocation capability

6. **Use a real database** (PostgreSQL, MySQL)

---

## 📦 Dependencies

| Dependency | Purpose |
|------------|---------|
| spring-boot-starter-web | REST API |
| spring-boot-starter-security | Authentication |
| spring-boot-starter-data-jpa | Database access |
| spring-boot-starter-validation | Input validation |
| jjwt-api/impl/jackson | JWT operations |
| h2 | In-memory database |
| lombok | Reduce boilerplate |

---

## 🛠️ Customization

### Add New Role

1. Create user with new role:
   ```java
   user.setRoles(Set.of("ROLE_USER", "ROLE_MODERATOR"));
   ```

2. Protect endpoint:
   ```java
   @PreAuthorize("hasRole('MODERATOR')")
   @GetMapping("/api/moderator/panel")
   ```

### Change Token Expiration

Edit `application.yml`:
```yaml
jwt:
  expiration:
    access: 3600000   # 1 hour
    refresh: 2592000000  # 30 days
```

### Use MySQL Instead of H2

1. Add dependency:
   ```xml
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
   </dependency>
   ```

2. Update `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/authdb
       username: root
       password: password
     jpa:
       hibernate:
         ddl-auto: update
   ```

---

## 📝 License

MIT License - Feel free to use and modify!
