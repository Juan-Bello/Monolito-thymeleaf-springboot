# Sistema de Gestión de Biblioteca Universitaria

Monolito Spring Boot + Thymeleaf para gestionar el préstamo de libros: catálogo de libros, registro de estudiantes, solicitud de préstamos con reglas de negocio y registro de devoluciones con cálculo de mora.

## Integrantes

- Juan Bello
- Camila Montealegre
- Mateo Guerra

## Stack

- Java 21, Spring Boot 4.0.2 (Web MVC, Thymeleaf, Data JPA, Validation)
- H2 en memoria por defecto; PostgreSQL 16 vía Docker Compose
- Empaquetado WAR: despliegue en Tomcat externo (`ServletInitializer`) o ejecución embebida

## Requisitos

- JDK 21
- Docker Desktop / Docker Engine en ejecución (solo para los flujos Docker)

## Comandos

Desarrollo local (H2 en memoria, sin base de datos externa):

```bash
./mvnw spring-boot:run
```

Tests (JUnit 5 + Mockito):

```bash
./mvnw test
./mvnw test -Dtest=PrestamoServiceTest
```

Empaquetar el WAR:

```bash
./mvnw package
```

Docker standalone (app con H2 en memoria):

```bash
docker system prune -a   # opcional: limpiar imágenes/contenedores viejos
docker build -t biblioteca-app .
docker run -p 8080:8080 biblioteca-app
```

Docker Compose (app + PostgreSQL + pgAdmin):

```bash
docker compose up
```

- App: http://localhost:8080
- pgAdmin: http://localhost:5050 (admin@example.com / admin123)
- PostgreSQL: bibliotecauser / bibliotecasecret

## Reglas de negocio

- **Solicitud de préstamo**: valida que el libro tenga ejemplares disponibles y que el estudiante no tenga préstamos vencidos; si es válido, registra el préstamo (estado `ACTIVO`), descuenta un ejemplar y calcula la fecha límite (14 días, configurable con `prestamo.dias-plazo`).
- **Préstamos vencidos**: una tarea programada (`prestamo.cron-vencidos`, cada minuto por defecto) marca como `VENCIDO` los préstamos activos vencidos; la validación de mora también se evalúa en tiempo real al solicitar.
- **Devolución**: calcula los días de mora según la fecha límite, marca el préstamo como `DEVUELTO` y devuelve el ejemplar al inventario (sin superar el total registrado).

## Endpoints

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/` | Redirige a `/libros` |
| GET | `/libros` | Catálogo de libros |
| GET/POST | `/libros/nuevo` | Formulario / registro de libro |
| GET | `/estudiantes` | Lista de estudiantes |
| GET/POST | `/estudiantes/nuevo` | Formulario / registro de estudiante |
| GET | `/prestamos` | Listado de préstamos (estado, mora, acciones) |
| GET/POST | `/prestamos/nuevo` | Formulario / solicitud de préstamo |
| POST | `/prestamos/{id}/devolver` | Registrar devolución |

## Estructura

```
src/main/java/co/javeriana/dw/biblioteca
├── model/          Libro, Estudiante, Prestamo, EstadoPrestamo
├── repository/     Spring Data JPA (Libro, Estudiante, Prestamo)
├── service/        Reglas de negocio transaccionales (préstamo, devolución, vencidos)
├── controller/     Controladores MVC + PrestamoForm (backing object)
├── exception/      NegocioException (violaciones de reglas de negocio)
├── BibliotecaApplication      (+ @EnableScheduling)
└── ServletInitializer         (despliegue WAR en Tomcat)
src/main/resources/templates/  Vistas Thymeleaf + fragments/ (header, footer)
```
