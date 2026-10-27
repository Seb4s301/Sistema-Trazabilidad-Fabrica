# Sistema Trazabilidad Fábrica — Latin Dream

ERP de producción web para una fábrica de colchones en Lima (interfaz en español, Perú).

## Stack

- **Frontend:** Angular + Bootstrap (responsive: escritorio y móvil).
- **Backend:** Spring Boot (Java), JWT, Spring Security.
- **Base de datos:** PostgreSQL en Supabase (esquema en `schema.sql`).
- **Prefijo API:** `/api/v1`
- **Zona horaria:** `America/Lima`.

## Arquitectura de carpetas (monorepo simple)

```
sistema-trazabilidad-fabrica/
├── backend/                      # Spring Boot (Java)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/latin dream/erp/
│   │       │   ├── config/       # Seguridad (JWT, Spring Security), CORS, beans
│   │       │   ├── controller/   # REST controllers (/api/v1/...)
│   │       │   ├── service/      # Lógica de negocio
│   │       │   ├── repository/   # Acceso a datos (JPA / Supabase)
│   │       │   ├── entity/       # Entidades JPA
│   │       │   ├── dto/          # Objetos de transferencia (request/response)
│   │       │   ├── security/     # Filtros, proveedores, utils de JWT
│   │       │   └── exception/    # Manejo global de errores ({codigo, mensaje})
│   │       └── resources/
│   │           ├── application.yml
│   │           └── schema.sql    # Esquema PostgreSQL (Supabase)
│   └── pom.xml
├── frontend/                     # Angular + Bootstrap
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/             # Auth, interceptors, guards, servicios compartidos
│   │   │   ├── shared/           # Componentes, pipes, directivas, modelos compartidos
│   │   │   ├── layouts/          # Shell: sidebar, header, footer, pantallas login
│   │   │   └── features/         # Módulos por funcionalidad (rutas del SKILL.md)
│   │   │       ├── login/
│   │   │       ├── resumen/
│   │   │       ├── ordenes/      # lista + nueva orden (stepper)
│   │   │       ├── produccion/   # tablero de trazabilidad por área
│   │   │       ├── comisiones/
│   │   │       └── despacho/
│   │   ├── assets/
│   │   └── environments/
│   └── angular.json
├── docs/
│   └── schema.sql                # Copia del esquema de base de datos
├── docker-compose.yml            # (opcional) Supabase local / servicios
├── .gitignore
└── README.md
```

## Convenciones

- Lenguaje visible: español (Perú). Fechas `16 oct. 2026`, montos `$11.660`.
- Errores de API: `{ "codigo": "...", "mensaje": "..." }` en español.
- Estados de UI: carga (esqueleto), vacía, error (banner rojo con reintento), datos.
- No hardcodear datos de ejemplo (clientes, correos, placas, teléfonos): son semillas de prueba.

## Endpoints sugeridos (backend)

`POST /auth/login`, `POST /auth/logout`, `GET /auth/me` · `GET /resumen` · `GET/POST/PUT /ordenes` · `GET /catalogos/*` · `GET /produccion/lotes` · `POST /produccion/lotes/{id}/avances` · `GET /comisiones` · `GET/POST /despachos` · `GET /despachos/{id}/documentos/{tipo}`.
