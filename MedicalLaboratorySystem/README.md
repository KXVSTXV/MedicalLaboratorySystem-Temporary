# 🔬 MedLab — Medical Laboratory System
### CLI Edition · v1.0

A fully functional **command-line Java application** backed by **SQLite via JDBC**.  
Designed to evolve into a Spring Boot microservices application in later versions.

---

## ✅ Features

| Service | Description |
|---|---|
| **Patient Management** | Register and look up patients |
| **Test Order Service** | Place orders, track status (PENDING → COMPLETED) |
| **Sample Service** | Record sample collection, update processing status |
| **Report Service** | Create draft reports, verify, release |
| **Notification Service** | Console notifications (logged to DB for audit) |

---

## 📁 Project Structure

```
medlab/
├── lib/
│   └── sqlite-jdbc-3.45.1.0.jar        ← You must download this
├── src/main/java/com/medlab/
│   ├── Main.java                        ← CLI entry point + DI wiring
│   ├── db/
│   │   ├── DatabaseConnection.java      ← JDBC connection (SQLite)
│   │   └── SchemaInitializer.java       ← Creates tables on startup
│   ├── model/                           ← Plain POJOs
│   │   ├── Patient.java
│   │   ├── TestOrder.java
│   │   ├── Sample.java
│   │   ├── Report.java
│   │   └── Notification.java
│   ├── repository/                      ← JDBC DAOs (SQL written explicitly)
│   │   ├── PatientRepository.java
│   │   ├── TestOrderRepository.java
│   │   ├── SampleRepository.java
│   │   ├── ReportRepository.java
│   │   └── NotificationRepository.java
│   ├── service/                         ← Business logic layer
│   │   ├── PatientService.java
│   │   ├── TestOrderService.java
│   │   ├── SampleService.java
│   │   ├── ReportService.java
│   │   └── NotificationService.java
│   └── util/
│       └── ConsoleUtil.java             ← Input/output helpers
├── run.sh                               ← Linux/Mac build & run
├── run.bat                              ← Windows build & run
└── README.md
```

---

## ⚙️ Prerequisites

- **JDK 17+** — [Download](https://adoptium.net/)
- **SQLite JDBC Driver** — [Download sqlite-jdbc-3.45.1.0.jar](https://github.com/xerial/sqlite-jdbc/releases/download/3.45.1.0/sqlite-jdbc-3.45.1.0.jar)

Place the downloaded `.jar` inside the `lib/` folder.

---

## 🚀 How to Run

### Linux / Mac
```bash
chmod +x run.sh
./run.sh
```

### Windows
```
run.bat
```

### Manual (any OS)
```bash
# 1. Compile
find src -name "*.java" > sources.txt
javac -cp lib/sqlite-jdbc-3.45.1.0.jar -d out @sources.txt

# 2. Run
java -cp "out:lib/sqlite-jdbc-3.45.1.0.jar" com.medlab.Main
# On Windows use semicolon: "out;lib/sqlite-jdbc-3.45.1.0.jar"
```

The database file `medlab.db` is created automatically in the working directory.

---

## 🔄 Typical Workflow

```
1. Register a Patient         → Patient Management → 1
2. Place a Test Order         → Test Order Service → 1
3. Collect a Sample           → Sample Service → 1
4. Update Sample to ANALYSED  → Sample Service → 4
5. Create a Draft Report      → Report Service → 1
6. Verify the Report          → Report Service → 2
7. Release the Report         → Report Service → 3  ← Patient notified!
8. View Notification Log      → Notifications → 1
```

---

## 📊 Database Schema

```sql
patients      (id, name, age, gender, contact)
test_orders   (id, patient_id, test_name, ordered_by, status, ordered_date)
samples       (id, order_id, sample_type, collected_by, collected_date, status)
reports       (id, order_id, result, remarks, prepared_by, report_date, status)
notifications (id, patient_id, message, type, sent_at)
```

---

## 🗺️ Upgrade Path to Spring Boot (v2.0)

| Current (CLI v1.0) | Future (Spring Boot v2.0) |
|---|---|
| `Main.java` wires services manually | Spring IoC container (`@Autowired`) |
| `DatabaseConnection.java` | Spring DataSource + `application.yml` |
| `SchemaInitializer.java` | Flyway / Liquibase migrations |
| `XxxRepository.java` (raw JDBC) | `JpaRepository<X, Integer>` interface |
| `XxxService.java` | `@Service` + `@Transactional` |
| Console menu | REST Controller + `@RestController` |
| `Notification` console print | Kafka/RabbitMQ event + Email/SMS |
| Single process | Separate microservices per domain |
| SQLite file | MySQL (managed by Kubernetes) |
| Manual DI in `Main` | Spring Boot `@SpringBootApplication` |

The package structure (`com.medlab.model`, `.service`, `.repository`) is already  
aligned with the future microservices split — no refactoring needed for the packages.

---

## 📋 Status Lifecycles

```
TestOrder:  PENDING → SAMPLE_COLLECTED → IN_PROGRESS → COMPLETED
Sample:     COLLECTED → PROCESSING → ANALYSED
Report:     DRAFT → VERIFIED → RELEASED
```
