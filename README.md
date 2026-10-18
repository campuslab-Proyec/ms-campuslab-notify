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
## 📨 Mensajería (RabbitMQ)

- **Nombres centralizados:** exchanges, colas, DLQ y routing keys están en `application.yaml` (`campuslab.rabbitmq.*`) y se leen con `MessagingProperties`.
- **Topología:** `config/RabbitMQConfig` declara, por cada caso de uso (email, prep, voucher, booking), su `Queue`, su DLQ y sus `Binding` hacia `cmd.direct` / `cmd.dead.dlx`.
- **Consumidores por dominio** (`consumer/`): `EmailConsumer`, `PreparationConsumer`, `VoucherConsumer`, `BookingEventConsumer`.
- **ACK manual:** cada consumidor confirma con `basicAck`. Si los datos son inválidos hace `basicNack` sin reencolar (va a la DLQ); ante un error inesperado reencola una vez y, si vuelve a fallar, lo manda a la DLQ.

## 🛠️ API de administración de RabbitMQ

La lógica está en `service/RabbitAdminService`; `controller/RabbitAdminController` solo expone HTTP.

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/admin/queues` | Todas las colas del broker con mensajes y consumidores (vía API de management) |
| `GET` | `/api/admin/queues/{name}` | Mensajes y consumidores de una cola |
| `POST` | `/api/admin/queues` | Crea una cola (`name`, `durable`, `autoDelete`, `deadLetterExchange`, `deadLetterRoutingKey`, `messageTtl`, `maxLength`) |
| `DELETE` | `/api/admin/queues/{name}?ifUnused=&ifEmpty=` | Elimina una cola |
| `DELETE` | `/api/admin/queues/{name}/messages` | Vacía una cola |
| `GET` | `/api/admin/exchanges` | Lista los exchanges (sin el por defecto ni los `amq.*`) |
| `POST` | `/api/admin/exchanges` | Crea un exchange (`name`, `type`: direct, topic, fanout o headers) |
| `DELETE` | `/api/admin/exchanges/{name}` | Elimina un exchange |
| `GET` | `/api/admin/bindings` | Lista los bindings exchange → cola |
| `POST` | `/api/admin/bindings` | Enlaza una cola a un exchange (`queue`, `exchange`, `routingKey`) |
| `DELETE` | `/api/admin/bindings` | Elimina un binding (mismo body) |
| `POST` | `/api/admin/messages` | Publica un mensaje JSON de prueba (`exchange`, `routingKey`, `payload`) → 202 |

Los listados usan la API HTTP de management de RabbitMQ (`campuslab.rabbitmq.management.url`, en Docker `http://rabbitmq-1:15672`), porque AmqpAdmin no permite listar. Si no responde, `GET /queues` devuelve al menos las colas propias y los demás listados responden 503.

El frontend expone esta API en la página **Panel RabbitMQ** (solo Admin), a través del BFF en `/api/bff/admin/**`.

**Validaciones:** nombres obligatorios, de máximo 255 caracteres, solo `A-Z a-z 0-9 . _ : -` y sin el prefijo reservado `amq.`; tipo de exchange válido; `messageTtl` y `maxLength` positivos; `deadLetterRoutingKey` exige `deadLetterExchange` y ese exchange debe existir.

**Respuestas:** `201` creado, `204` eliminado, `400` parámetros inválidos, `404` cola o exchange inexistente, `409` ya existe, está en uso o la declara la aplicación (las colas y exchanges propios están protegidos), `503` sin conexión con RabbitMQ.

Ejemplo:

```bash
curl -X POST http://localhost:8083/api/admin/queues \
  -H 'Content-Type: application/json' \
  -d '{"name":"demo.admin.queue","messageTtl":60000,"deadLetterExchange":"cmd.dead.dlx"}'
```
