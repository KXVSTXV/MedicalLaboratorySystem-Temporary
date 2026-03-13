# 🔬 MedLab — Medical Laboratory System
### CLI Edition · v1.0 · Core Java + MySQL + JDBC · Maven Build

A fully functional **command-line Java application** backed by **MySQL via JDBC**.  
Dependencies are managed automatically by **Maven** — no manual JAR downloads needed.

---

## ✅ Features

| Service | Description |
|---|---|
| **Authentication & Authorization** | Login/logout with roles: ADMIN, RECEPTIONIST, LAB_TECHNICIAN |
| **Test Order Service** | Place, view, cancel orders with priority levels |
| **Sample Service** | Record sample collection, update processing status |
| **Report Service** | Create drafts, verify, release with normal-range comparison |
| **Notification Service** | Console alerts + persisted notification log with read tracking |
| **Patient Management** | Register, search, soft-delete patients |
| **User Administration** | ADMIN-only user management |
| **Performance Metrics** | Per-operation timing with < 300ms NFR tracking |

---

## 🏗️ Architecture

```
com.medlab/
├── Main.java                          ← CLI entry point + Manual DI wiring
├── auth/
│   └── AuthContext.java               ← Thread-local session (replaces Spring Security)
├── db/
│   ├── DatabaseConnection.java        ← MySQL JDBC connection singleton
│   └── SchemaInitializer.java         ← Idempotent DDL on startup
├── model/                             ← Plain POJOs (future: JPA @Entity)
│   ├── User.java
│   ├── Patient.java
│   ├── TestOrder.java
│   ├── Sample.java
│   ├── Report.java
│   └── Notification.java
├── repository/                        ← JDBC DAOs (future: JpaRepository)
│   ├── UserRepository.java
│   ├── PatientRepository.java
│   ├── TestOrderRepository.java
│   ├── SampleRepository.java
│   ├── ReportRepository.java
│   └── NotificationRepository.java
├── service/                           ← Business logic + authorization
│   ├── AuthService.java
│   ├── PatientService.java
│   ├── TestOrderService.java
│   ├── SampleService.java
│   ├── ReportService.java
│   └── NotificationService.java
└── util/
    ├── ConsoleUtil.java               ← Input/output helpers
    ├── PasswordUtil.java              ← SHA-256 password hashing
    └── PerformanceMetrics.java        ← NFR timing tracker (< 300ms)
```

### Maven Directory Layout
```
medlab/
├── pom.xml                            ← Maven build descriptor (all dependencies here)
├── run.sh                             ← Linux/macOS: build + run
├── run.bat                            ← Windows: build + run
├── src/
│   ├── main/
│   │   ├── java/com/medlab/           ← Application source
│   │   └── resources/
│   │       └── schema.sql             ← DB schema reference
│   └── test/
│       └── java/com/medlab/           ← JUnit 5 + Mockito tests
└── target/
    └── medlab.jar                     ← Fat JAR produced by mvn package
```

---

## 📊 Database Schema

```sql
users         (id, username[UNIQUE], email[UNIQUE], mobile_number[UNIQUE],
               password_hash, role, is_active, is_deleted,
               created_at, updated_at, created_by)

patients      (id, name, age, gender, email[UNIQUE], mobile_number[UNIQUE],
               address, is_deleted, created_at, updated_at, created_by)

test_orders   (id, patient_id, test_name, ordered_by, status, priority,
               notes, is_deleted, created_at, updated_at, created_by)

samples       (id, order_id, sample_type, collected_by, collected_date,
               status, rejection_reason, is_deleted, created_at, updated_at, created_by)

reports       (id, order_id, result, remarks, normal_range, is_abnormal,
               prepared_by, verified_by, report_date, status,
               is_deleted, created_at, updated_at, created_by)

notifications (id, patient_id, message, type, is_read, is_deleted,
               created_at, updated_at, created_by)
```

### Audit Fields
Every table has: `created_at` (auto-set by DB), `updated_at` (auto-updated by DB), `created_by`

### Soft Delete
Every table has: `is_deleted BOOLEAN DEFAULT FALSE`  
Deleted records remain in the DB for audit history but are invisible to all queries.

### DB Constraints
- `users`: UNIQUE on `username`, `email`, `mobile_number`
- `patients`: UNIQUE on `email`, `mobile_number`

---

## 📋 Status Lifecycles

```
TestOrder:  ORDERED → SAMPLE_COLLECTED → IN_PROGRESS → REPORT_READY → COMPLETED
                                                                     ↳ CANCELLED (soft-delete)

Sample:     COLLECTED → PROCESSING → ANALYSED
                                   ↳ REJECTED  (with rejection_reason)

Report:     DRAFT → VERIFIED → RELEASED
```

---

## 🔐 Role-Based Access Control

| Action | ADMIN | RECEPTIONIST | LAB_TECHNICIAN |
|--------|-------|-------------|----------------|
| Register patients | ✔ | ✔ | ✗ |
| Place test orders | ✔ | ✔ | ✗ |
| Cancel test orders | ✔ | ✔ | ✗ |
| Collect samples | ✔ | ✗ | ✔ |
| Update sample status | ✔ | ✗ | ✔ |
| Create/verify/release reports | ✔ | ✗ | ✔ |
| View all (patients/orders/etc.) | ✔ | ✔ | ✔ |
| User administration | ✔ | ✗ | ✗ |

---

## 📦 Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| **JDK** | 17+ | [Adoptium](https://adoptium.net/) |
| **Maven** | 3.6+ | [maven.apache.org](https://maven.apache.org/download.cgi) — handles all JARs automatically |
| **MySQL** | 8.x | Running locally on port 3306 |

> **No manual JAR downloads required.** Maven pulls `mysql-connector-j` and all test dependencies automatically on first build.

---

## ⚙️ Setup

### 1. Create the database

```sql
CREATE DATABASE medlab_db;
```

The schema tables and seed data are created automatically on first run via `SchemaInitializer`.

### 2. Configure the DB connection

**Option A: Environment variables (recommended)**
```bash
export MEDLAB_DB_URL="jdbc:mysql://localhost:3306/medlab_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export MEDLAB_DB_USER="root"
export MEDLAB_DB_PASSWORD="your_password"
```

**Option B: JVM system properties at runtime**
```bash
java -Dmedlab.db.url="..." -Dmedlab.db.user="root" -Dmedlab.db.password="..." -jar target/medlab.jar
```

---

## 🚀 How to Run

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Windows
```bat
run.bat
```

### Manual Maven commands
```bash
# Compile only
mvn compile

# Run tests
mvn test

# Build fat JAR (skipping tests)
mvn clean package -DskipTests

# Run the fat JAR
java -Dmedlab.db.url="jdbc:mysql://localhost:3306/medlab_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
     -Dmedlab.db.user="root" \
     -Dmedlab.db.password="yourpassword" \
     -jar target/medlab.jar
```

---

## 👥 Sample Users (Seeded on First Run)

| Username | Password | Role |
|----------|----------|------|
| `admin` | `Admin@123` | ADMIN |
| `receptionist1` | `Admin@123` | RECEPTIONIST |
| `labtech1` | `Admin@123` | LAB_TECHNICIAN |

### Sample Patients (Seeded)

| ID | Name | Age | Gender | Mobile |
|----|------|-----|--------|--------|
| 1 | John Doe | 22 | M | 1234567890 |
| 2 | Jane Doe | 22 | F | 2345678901 |
| 3 | Richard Roe | 22 | M | 3456789012 |

---

## 🔄 Typical Workflow

```
1. Login as receptionist1 / Admin@123
2. Patient Management → Register a new patient (or use seeded ones)
3. Test Order Service → Place a new order (Patient ID + test name + doctor)
4. [Switch: login as labtech1 / Admin@123]
5. Sample Service → Record sample collection (Order ID + sample type)
6. Sample Service → Update sample to ANALYSED
7. Report Service → Create draft report (Order ID + result + normal range)
8. Report Service → Verify the report
9. Report Service → Release the report  ← Patient automatically notified!
10. Notifications → View notification log
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=AuthServiceTest

# Run tests + generate Surefire HTML report
mvn surefire-report:report
# Report: target/site/surefire-report.html
```

**Test coverage:**

| Test Class | Service Under Test | Scenarios |
|---|---|---|
| `AuthServiceTest` | `AuthService` | login, wrong password, inactive account, register, logout, role guard |
| `NotificationServiceTest` | `NotificationService` | send, DB failure resilience, getAllNotifications, markAsRead |
| `ReportServiceTest` | `ReportService` | create DRAFT, abnormal detection, verify, release + notification |
| `SampleServiceTest` | `SampleService` | collectSample, wrong state, role guard, ANALYSED advances order |
| `TestOrderServiceTest` | `TestOrderService` | placeOrder, cancel, getAllOrders, role guards |
| `PasswordUtilTest` | `PasswordUtil` | hash consistency, length, verify, case-insensitive comparison |

---

## 🎯 Non-Functional Requirements (NFR) Tracking

Per the HLRD document, response time must be **< 300ms** for most operations.

The built-in `PerformanceMetrics` utility tracks all key operations.  
View the session summary from the main menu → option **7. Performance Summary**.

```
── Performance Summary ──────────────────────────────
TestOrderService.placeOrder                12 ms  ✔
TestOrderService.getAllOrders              18 ms  ✔
─────────────────────────────────────────────────────
```

Operations exceeding 300ms show a `⚠` warning inline.

---

## 🗺️ Upgrade Path

| Current (CLI v1.0) | Spring Boot v2.0 | Microservices v3.0 |
|---|---|---|
| `Main.java` manual DI | Spring IoC `@Autowired` | Separate service JARs |
| `DatabaseConnection.java` | `DataSource` + `application.yml` | Per-service DB |
| `SchemaInitializer.java` | Flyway / Liquibase | Per-service migrations |
| `XxxRepository.java` JDBC | `JpaRepository<X,Integer>` | Spring Data JPA |
| `XxxService.java` | `@Service` + `@Transactional` | REST `@RestController` |
| Console menu | REST API + Swagger/OpenAPI | API Gateway routing |
| `AuthContext` thread-local | Spring Security + JWT | OAuth2 / Keycloak |
| `PasswordUtil` SHA-256 | BCrypt via Spring Security | Identity Service |
| Console notifications | Kafka/RabbitMQ + Email/SMS | Notification Microservice |
| Single fat JAR | Spring Boot JAR | K8S pods per service |
| `pom.xml` (Maven) | `pom.xml` (Spring Boot parent) | Multi-module Maven project |

The package structure (`com.medlab.model`, `.service`, `.repository`) is already  
aligned with the future microservices split — no refactoring needed for packages.
