# Account Service

Un servicio de microservicios construido con Spring Boot que gestiona cuentas bancarias. Forma parte de un sistema de transacciones bancarias distribuido que utiliza Kafka para la comunicación entre servicios.

## Descripción

El Account Service es responsable de:
- Gestionar cuentas bancarias (creación, actualización, consulta)
- Procesar eventos de transacciones desde el servicio de transacciones
- Mantener la integridad de los saldos de las cuentas
- Publicar eventos de actualización de cuentas para otros servicios

## Tecnologías

- **Java 21** - Lenguaje de programación
- **Spring Boot 3.5.6** - Framework principal
- **Spring Data JPA** - Acceso a datos
- **Spring Kafka** - Mensajería asincrónica
- **PostgreSQL** - Base de datos
- **Flyway** - Migraciones de base de datos
- **Maven** - Gestor de dependencias

## Requisitos Previos

- Java 21 instalado
- Docker y Docker Compose (para bases de datos e infraestructura)
- Maven 3.6+

## Instalación

1. **Clonar el repositorio**
```bash
cd account-service
```

2. **Compilar el proyecto**
```bash
mvn clean package
```

3. **Iniciar la infraestructura (desde la raíz del proyecto)**
```bash
docker-compose -f infra/docker-compose.yml up -d
```

## Configuración

La configuración principal se encuentra en `src/main/resources/application.yml`:

- **Puerto**: 8083
- **Base de datos**: PostgreSQL en `jdbc:postgresql://localhost:5432/account_db`
- **Kafka Bootstrap Servers**: `localhost:9092`
- **Credenciales PostgreSQL**: 
  - Usuario: `postgres`
  - Contraseña: `admin`

### Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:

```bash
SERVER_PORT=8083
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/account_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Estructura del Proyecto

```
src/main/
├── java/com/bank/accountservice/
│   ├── config/           # Configuración de Spring
│   ├── controller/       # Controladores REST
│   ├── dto/             # Data Transfer Objects
│   ├── event/           # Modelos de eventos
│   ├── exception/       # Excepciones personalizadas
│   ├── kafka/           # Configuración de Kafka
│   ├── mapper/          # Mappers entre entidades y DTOs
│   ├── model/           # Modelos de datos (entidades JPA)
│   ├── repository/      # Repositorios JPA
│   └── service/         # Lógica de negocio
└── resources/
    ├── application.yml  # Configuración
    └── db/migration/    # Scripts SQL de Flyway
```

## Ejecución

### Desarrollo

```bash
mvn spring-boot:run
```

### Compilación y JAR

```bash
mvn clean package
java -jar target/account-service-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Obtener todas las cuentas
```
GET /api/accounts
```

### Obtener una cuenta por ID
```
GET /api/accounts/{id}
```

### Crear una cuenta
```
POST /api/accounts
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "balance": 1000.00
}
```

### Actualizar una cuenta
```
PUT /api/accounts/{id}
Content-Type: application/json

{
  "balance": 1500.00
}
```

## Kafka Topics

### Tópicos Consumidos

- **transaction-events**: Eventos de transacciones del servicio de transacciones

### Tópicos Publicados

- **account-events**: Eventos de actualización de cuentas

## Base de Datos

El servicio utiliza Flyway para manejar las migraciones automáticamente. Los scripts de migración se encuentran en `src/main/resources/db/migration/`:

- `V1__create_accounts_table.sql` - Crea la tabla de cuentas
- `V2__create_outbox_event_table.sql` - Crea la tabla de eventos outbox

## Testing

Ejecutar los tests:

```bash
mvn test
```

## Desarrollo

### Agregar nuevas dependencias

Edita `pom.xml` y ejecuta:

```bash
mvn clean install
```

### Crear nuevas migraciones

Crea un nuevo archivo en `src/main/resources/db/migration/` siguiendo el patrón `V{numero}__{descripcion}.sql`

## Troubleshooting

### Error de conexión a PostgreSQL
- Verifica que Docker Compose esté ejecutándose: `docker-compose ps`
- Verifica la credenciales en `application.yml`

### Errores de Kafka
- Verifica que Kafka esté corriendo: `docker-compose -f infra/docker-compose.yml up`
- Verifica los logs: `docker logs kafka`

### Port already in use
- Cambia el puerto en `application.yml` si el 8083 está en uso

## Logs

Los logs se configuran en Spring Boot y se envían a consola. Para cambiar el nivel de logging, usa:

```yaml
logging:
  level:
    com.bank.accountservice: DEBUG
```

## Licencia

[Especificar licencia]

## Contacto

[Datos de contacto del equipo/desarrollador]
