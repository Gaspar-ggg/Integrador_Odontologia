# Arquitectura Propuesta

## Patrones aplicados

| Patrón | Dónde se aplica | Justificación |
|---|---|---|
| **Arquitectura en Capas (Layered Architecture)** | Backend Spring Boot (Controller, Service, Repository, Model/DTO) | Separación estricta de responsabilidades, facilidad de testing unitario y aislamiento de la lógica de negocio. |
| **Data Transfer Object (DTO) + Mappers** | Comunicación entre Controllers y Services | Evita exponer las entidades JPA directamente a la red y previene problemas de sobre-exposición o deserialización maliciosa. |
| **Strategy / Adapter Pattern** | Módulo de Notificaciones (`NotificationService`, `EmailNotificationAdapter`, `WhatsAppNotificationAdapter`, `MockNotificationAdapter`) | Permite alternar entre proveedores reales o simulados para desarrollo sin alterar la lógica de negocio. |
| **Repository Pattern** | Capa de persistencia con `JpaRepository` | Abstracción estándar del acceso a datos, desacoplando la base de datos subyacente. |
| **Módulos ES + Componentes TypeScript Vanilla** | Frontend (`src/api`, `src/components`, `src/pages`) | Estructura modular y limpia sin dependencias complejas de frameworks, compilada limpiamente con `tsc`. |

## Estructura de directorios

```
Integrador_Odontologia/
├── backend/
│   ├── build.gradle
│   ├── settings.gradle
│   ├── docker-compose.yml              # PostgreSQL local
│   └── src/
│       ├── main/
│       │   ├── java/com/odontologia/
│       │   │   ├── config/              # SecurityConfig, CorsConfig, MailConfig
│       │   │   ├── controller/          # Controladores REST API
│       │   │   ├── dto/                 # Request y Response DTOs
│       │   │   ├── exception/           # GlobalExceptionHandler y excepciones custom
│       │   │   ├── model/               # Entidades JPA (Usuario, Paciente, Turno, etc.)
│       │   │   ├── repository/          # Interfaces Spring Data JPA
│       │   │   ├── security/            # JwtFilter, JwtProvider, UserDetailsService
│       │   │   └── service/             # Lógica de negocio e interfaces de servicios
│       │   └── resources/
│       │       ├── application.yml      # Configuración de Spring y Datasource
│       │       └── application-test.yml # Configuración H2 para pruebas
│       └── test/                        # Tests unitarios e integración (JUnit 5 + Mockito)
├── frontend/
│   ├── tsconfig.json                    # Configuración estricta de TypeScript
│   ├── package.json                     # Scripts de compilación ("build": "tsc")
│   ├── public/                          # Archivos estáticos HTML y CSS
│   │   ├── index.html                   # Página principal / Landing
│   │   ├── login.html                   # Login y registro
│   │   ├── dashboard.html               # Panel según rol
│   │   └── css/                         # Estilos responsivos modernos (CSS variables)
│   └── src/                             # Código fuente TypeScript
│       ├── api/                         # Cliente HTTP fetch y llamadas a endpoints
│       ├── models/                      # Tipos e interfaces TypeScript compartidas
│       ├── utils/                       # Manejo de JWT, formateo de fechas y validadores
│       └── pages/                       # Lógica específica de cada vista
└── knowledge-base/                      # Base de conocimiento canónica
```

## Seguridad

- **Autenticación**: Stateless con tokens JSON Web Token (JWT) firmados con algoritmo HMAC-SHA256 y clave secreta robusta.
- **Autorización**: RBAC enforcing mediante `@PreAuthorize("hasRole('ADMIN')")` o configuraciones en `SecurityFilterChain` por patrón de URL.
- **Validación de Input**: Anotaciones estándar Jakarta Validation (`@NotNull`, `@NotBlank`, `@Email`, `@Size`) en todos los DTOs de entrada.
- **Contraseñas**: Hasheadas mediante `BCryptPasswordEncoder` antes de persistirse en base de datos.
- **Protección CORS**: Habilitado de forma explícita para el origen del frontend local (`http://localhost:...`).

## Variables de entorno

| Variable | Descripción | Ejemplo | Sensible (Y/N) |
|---|---|---|:---:|
| `DB_HOST` | Host de la base de datos PostgreSQL | `localhost` | N |
| `DB_PORT` | Puerto de conexión a PostgreSQL | `5432` | N |
| `DB_NAME` | Nombre de la base de datos | `odontologia_db` | N |
| `DB_USER` | Usuario de PostgreSQL | `postgres` | N |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `postgres123` | Y |
| `JWT_SECRET` | Clave secreta para firmar tokens JWT | `MiClaveUltraSecretaOdontologia2026Base64` | Y |
| `JWT_EXPIRATION_MS` | Tiempo de vida del token en milisegundos | `86400000` (24 horas) | N |
| `MAIL_HOST` | Host del servidor SMTP de correo | `smtp.gmail.com` | N |
| `MAIL_PORT` | Puerto SMTP | `587` | N |
| `MAIL_USERNAME` | Usuario o email para envío de correos | `notificaciones.clinica@gmail.com` | Y |
| `MAIL_PASSWORD` | Contraseña o Token de aplicación SMTP | `app-password-token` | Y |
| `WHATSAPP_MOCK_ENABLED` | Si activa el mock logger en consola | `true` | N |
