# Transactions Service

Un servicio de microservicios construido con Spring Boot que gestiona transacciones bancarias. Forma parte de un sistema de transacciones bancarias distribuido que utiliza Kafka para la comunicación entre servicios.

## Descripción

El Transactions Service es responsable de:
- Crear y gestionar transacciones bancarias (transferencias, depósitos, retiros)
- Validar transacciones contra saldos disponibles
- Comunicar cambios de cuenta con el servicio de cuentas a través de Kafka
- Mantener un historial de transacciones
- Proporcionar reportes y consultas sobre transacciones

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
cd transactions-service
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

- **Puerto**: 8082
- **Base de datos**: PostgreSQL en `jdbc:postgresql://localhost:5432/transaction_db`
- **Kafka Bootstrap Servers**: `localhost:9092`
- **Credenciales PostgreSQL**: 
  - Usuario: `postgres`
  - Contraseña: `admin`

### Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:

```bash
SERVER_PORT=8082
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/transaction_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin
SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Estructura del Proyecto

```
src/main/
├── java/com/bank/transactionservice/
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
java -jar target/transactions-service-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### Obtener todas las transacciones
```
GET /api/transactions
```

### Obtener una transacción por ID
```
GET /api/transactions/{id}
```

### Crear una transacción
```
POST /api/transactions
Content-Type: application/json

{
  "sourceAccountId": 1,
  "destinationAccountId": 2,
  "amount": 100.00,
  "transactionType": "TRANSFER"
}
```

### Obtener transacciones por cuenta
```
GET /api/transactions/account/{accountId}
```

### Obtener transacciones en un rango de fechas
```
GET /api/transactions/date-range?startDate=2025-01-01&endDate=2025-12-31
```

## Tipos de Transacciones

- **TRANSFER**: Transferencia entre cuentas
- **DEPOSIT**: Depósito en una cuenta
- **WITHDRAWAL**: Retiro de una cuenta

## Estados de Transacción

- **PENDING**: Transacción pendiente de procesamiento
- **COMPLETED**: Transacción completada exitosamente
- **FAILED**: Transacción rechazada
- **CANCELLED**: Transacción cancelada

## Kafka Topics

### Tópicos Publicados

- **transaction-events**: Eventos generados por el servicio de transacciones

### Tópicos Consumidos

- **account-events**: Eventos de actualización de cuentas del servicio de cuentas

## Base de Datos

El servicio utiliza Flyway para manejar las migraciones automáticamente. Los scripts de migración se encuentran en `src/main/resources/db/migration/`:

- `V1__create_all_tables.sql` - Crea todas las tablas necesarias (transacciones, auditoría, etc.)

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

## Validaciones

El servicio realiza las siguientes validaciones:

- **Saldo Insuficiente**: Verifica que la cuenta origen tenga saldo suficiente antes de procesar
- **Cuentas Válidas**: Valida que las cuentas origen y destino existan
- **Montos Válidos**: Rechaza transacciones con montos negativos o cero
- **Limites**: Puede implementar limites por transacción o por período

## Troubleshooting

### Error de conexión a PostgreSQL
- Verifica que Docker Compose esté ejecutándose: `docker-compose ps`
- Verifica las credenciales en `application.yml`

### Errores de Kafka
- Verifica que Kafka esté corriendo: `docker-compose -f infra/docker-compose.yml up`
- Verifica los logs: `docker logs kafka`
- Verifica que el servicio de cuentas también esté ejecutándose

### Port already in use
- Cambia el puerto en `application.yml` si el 8082 está en uso

### Transacciones rechazadas
- Verifica el saldo disponible en la cuenta origen
- Verifica los logs de validación

## Logs

Los logs se configuran en Spring Boot y se envían a consola. Para cambiar el nivel de logging, usa:

```yaml
logging:
  level:
    com.bank.transactionservice: DEBUG
```

## Monitoring

Puedes monitorear Kafka usando Kafdrop en `http://localhost:9000`:

- Ver tópicos
- Ver mensajes en tiempo real
- Verificar consumidores

## Licencia

[Especificar licencia]

## Contacto

[Datos de contacto del equipo/desarrollador]
