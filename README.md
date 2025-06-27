# Bank Statement Service

A REST API service to manage bank account statements via CSV import/export.

For simplicity in memory database is used. Initial bank statements data is provided in repository as bank-statements.csv file and can be imported through import endpoint.  

## 🚀 How to Run

### Prerequisites
- Java 17+
- Maven 3.8+ installed (or use the wrapper)

### Build & Run
- Build application: mvn clean install
- Run application: mvn spring-boot:run

### Endpoints
For testing purposes Swagger UI is configured and reachable when application is running: 
- http://localhost:8080/swagger-ui/index.html