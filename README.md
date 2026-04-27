# yanki-service

Microservicio encargado del monedero movil Yanki. Permite registrar wallets,
consultarlas por numero de celular y ejecutar transferencias entre wallets usando
el numero telefonico.

## Tecnologias

- Java 17
- Spring Boot 3.2.4
- Spring WebFlux
- Spring Data MongoDB Reactive
- Spring Kafka
- Reactor Kafka
- RxJava 3
- Reactor Adapter
- Spring Cloud Config
- Eureka Client
- Springdoc OpenAPI
- Maven
- JUnit 5
- JaCoCo
- Spotless
- Checkstyle

## Puerto

```text
http://localhost:8087
```

## OpenAPI

```text
http://localhost:8087/swagger-ui.html
http://localhost:8087/v3/api-docs
```

## Levantar sin Docker

Requisitos locales:

- Java 17
- Maven
- MongoDB en `localhost:27017`
- Kafka en `localhost:9092`
- Config Server opcional en `localhost:8888`
- Eureka en `localhost:8761`
- account-service disponible si se asocia tarjeta de debito

```powershell
cd .\yanki-service
mvn spring-boot:run
```

## Levantar con Docker

Primero levantar la infraestructura desde la raiz del repositorio:

```powershell
cd .\infra
docker compose up -d --build
```

El `docker-compose.yml` de este microservicio usa la red externa
`infra_ntt_network`, creada por el compose de infraestructura, y se conecta a
MongoDB, Kafka, Config Server, Eureka y `account-service` usando nombres internos
de Docker.

Generar el jar y levantar el contenedor:

```powershell
cd ..\yanki-service
mvn clean package
docker compose up -d --build
```

Ver logs:

```powershell
docker compose logs -f yanki-service
```

Detener el microservicio:

```powershell
docker compose down
```

## Tests

```powershell
cd .\yanki-service
mvn test
```

Si luego se agregan tests de integracion:

```powershell
mvn verify
```

## Formato y Checkstyle

```powershell
mvn spotless:apply
mvn checkstyle:check
```

## JaCoCo

```powershell
mvn test jacoco:report
```

Reporte:

```text
target/site/jacoco/index.html
```

## MongoDB

Base de datos:

```text
ntt_yanki
```

Colecciones principales:

```text
wallets
yanki_transactions
```

Consulta:

```powershell
mongosh
use ntt_yanki
show collections
db.wallets.find().pretty()
db.yanki_transactions.find().pretty()
```

Se usa database per service logico: cada microservicio mantiene su propia base de
datos MongoDB.
