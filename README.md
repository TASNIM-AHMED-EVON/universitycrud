# University Management System (CSE 3230)

Spring Boot 3 / Java 17 REST API with three roles: **Admin**, **Teacher**, **Student**.
Spring Security (session login/logout, BCrypt, RBAC), PostgreSQL, Docker Compose.

## Run with Docker (recommended)

```bash
docker compose up --build
```

API: http://localhost:8080  
Default admin (created on first start): `admin` / `admin123`

Stop: `docker compose down` (add `-v` to also delete the database volume).

## Run without Docker (H2 in-memory DB)

Needs JDK 17+ and Maven.

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Who can access what

| Endpoint            | ADMIN | TEACHER | STUDENT |
|---------------------|:-----:|:-------:|:-------:|
| `/api/admins/**`    |  yes  |   no    |   no    |
| `/api/teachers/**`  |  yes  |   yes   |   no    |
| `/api/students/**`  |  yes  |   no    |   yes   |
| `/api/auth/me`      |  yes  |   yes   |   yes   |

Not logged in -> `401`. Logged in but wrong role -> `403`.

## Endpoints

| Method | URL                     | Success | Description                      |
|--------|-------------------------|---------|----------------------------------|
| POST   | `/api/auth/login`       | 200     | Log in (sets `JSESSIONID` cookie)|
| POST   | `/api/auth/logout`      | 200     | Log out                          |
| GET    | `/api/auth/me`          | 200     | Current user                     |
| GET    | `/api/{resource}`       | 200     | List all                         |
| GET    | `/api/{resource}/{id}`  | 200     | Get one (`404` if missing)       |
| POST   | `/api/{resource}`       | 201     | Create (`Location` header set)   |
| PUT    | `/api/{resource}/{id}`  | 200     | Update                           |
| DELETE | `/api/{resource}/{id}`  | 204     | Delete                           |

`{resource}` = `admins`, `teachers` or `students`.  
Other codes: `400` validation error, `409` duplicate username/email/roll.

## Quick test with curl

```bash
# 1. Login as admin and keep the session cookie
curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Create a teacher
curl -i -b cookies.txt -X POST http://localhost:8080/api/teachers \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","password":"secret123","fullName":"Dr. Rahman","email":"rahman@kuet.ac.bd","department":"CSE","designation":"Professor"}'

# 3. Create a student
curl -i -b cookies.txt -X POST http://localhost:8080/api/students \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"secret123","fullName":"Rafi Ahmed","email":"rafi@kuet.ac.bd","studentRoll":"2207001","department":"CSE","semester":6}'

# 4. List / update / delete
curl -b cookies.txt http://localhost:8080/api/teachers
curl -b cookies.txt -X PUT http://localhost:8080/api/teachers/2 \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","fullName":"Dr. A. Rahman","email":"rahman@kuet.ac.bd","department":"CSE","designation":"Professor"}'
curl -i -b cookies.txt -X DELETE http://localhost:8080/api/teachers/2

# 5. Logout
curl -b cookies.txt -X POST http://localhost:8080/api/auth/logout
```

Test RBAC: log in as `student1` (new cookie file) and call `GET /api/teachers` -> `403`,
`GET /api/students` -> `200`.

On update (`PUT`) the `password` field is optional; leave it out to keep the old password.

## Project structure

```
src/main/java/com/kuet/ums/
  config/       SecurityConfig, DataInitializer (seeds first admin)
  controller/   AuthController, Admin/Teacher/StudentController
  dto/          request/response records (passwords are never returned)
  exception/    custom exceptions + GlobalExceptionHandler (JSON errors)
  model/        AppUser (base) + Admin, Teacher, Student  (one "users" table)
  repository/   Spring Data JPA repositories
  service/      business logic + CustomUserDetailsService
```

## Notes

- CSRF is disabled to make the API easy to test with Postman/curl.
- Change `ADMIN_PASSWORD` (and the DB password) in `compose.yaml` for anything beyond a lab demo.

## Tests and CI

| Kind | Files | Count | Command |
|------|-------|:-----:|---------|
| Unit (JUnit 5 + Mockito) | `src/test/java/.../service/*Test.java` | 6 | `mvn test` |
| Integration (Spring context + H2 + MockMvc) | `src/test/java/.../integration/*IT.java` | 3 | `mvn verify` |

GitHub Actions (`.github/workflows/ci.yml`) runs the unit tests, then the integration tests, on every push and pull request.
