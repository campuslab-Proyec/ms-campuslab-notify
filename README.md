# MS CampusLab Catalog

Servicio backend encargado de la gestión del catálogo de laboratorios y recursos en el sistema CampusLab, proporcionando la persistencia de datos y la publicación de eventos.

## 🛠️ Tecnologías Utilizadas

* **Java** 21+ / Java 25
* **Spring Boot** 4.x / 3.x
* **Spring Data JPA** (Persistencia)
* **MySQL 8.x** (Base de datos relacional)
* **Hibernate** (ORM)
* **Spring AMQP / RabbitMQ** (Procesamiento de eventos en segundo plano)
* **Jackson** (Serialización / Deserialización JSON)
* **Apache Maven** (Gestor de dependencias y construcción)

## 📋 Requisitos Previos

Asegúrate de contar con lo siguiente instalado y en ejecución en tu entorno local:

1. **JDK 21** o superior instalado.
2. **MySQL Server 8.x** activo en `localhost:3306`.
3. **RabbitMQ Server** activo en sus puertos predeterminados (`5672` / `15672`).
4. **Maven** instalado (o usar el wrapper `./mvnw` / `mvnw.cmd` incluido en el repositorio).

## 🚀 Instalación y Ejecución

1. **Clonar el repositorio:**
   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd ms-campuslab-catalog
Configurar la base de datos:
Asegúrate de que la base de datos campuslab_catalog existe en tu servidor MySQL o mantén el parámetro createDatabaseIfNotExist=true en la cadena de conexión del archivo src/main/resources/application.yaml.

Ajusta credenciales si es necesario:

YAML
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campuslab_catalog?...
    username: root
    password: tu_password
Compilar el proyecto:

Bash
mvn clean compile
Ejecutar la aplicación:

Bash
mvn spring-boot:run
El microservicio estará disponible por defecto en: http://localhost:8082