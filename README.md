# Latin Dream — ERP de Producción y Trazabilidad Industrial

Sistema integral de gestión de producción, trazabilidad de lotes por etapas, control de despachos y cálculo de comisiones para la fábrica de colchones **Latin Dream** (Lima, Perú).

---

## 📑 Tabla de Contenidos

1. [Arquitectura de N Capas](#1-arquitectura-de-n-capas)
2. [Cumplimiento de Principios SOLID](#2-cumplimiento-de-principios-solid)
3. [Librerías Clave y su Uso](#3-librerías-clave-y-su-uso)
4. [Mapa de APIs RESTful](#4-mapa-de-apis-restful)
5. [Catálogo de Servicios](#5-catálogo-de-servicios)
6. [Pila Tecnológica](#6-pila-tecnológica)
7. [Instalación y Puesta en Marcha](#7-instalación-y-puesta-en-marcha)
8. [Usuarios y Credenciales de Acceso](#8-usuarios-y-credenciales-de-acceso)

---

## 1. Arquitectura de N Capas

El proyecto implementa una arquitectura desacoplada en **N capas**, garantizando alta cohesión, bajo acoplamiento y un flujo unidireccional de dependencias:

```
┌─────────────────────────────────────────────────────────────┐
│                 CAPA DE PRESENTACIÓN                        │
│   • Controllers REST (Spring Web)                           │
│   • UI / Single Page Application (Angular 17 + Bootstrap 5) │
└──────────────────────────────┬──────────────────────────────┘
                               │ (Consume DTOs)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 CAPA DE APLICACIÓN                          │
│   • Servicios de Orquestación y Casos de Uso                │
│   • Generación de Reportes Excel (Apache POI)               │
│   • DTOs (Request / Response) & Mappers                     │
└──────────────────────────────┬──────────────────────────────┘
                               │ (Accede al Dominio)
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 CAPA DE DOMINIO                             │
│   • Entidades de Negocio (JPA / Hibernate)                  │
│   • Interfaces de Repositorios (Spring Data JPA)            │
│   • Reglas y Restricciones de Negocio                       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 CAPA DE INFRAESTRUCTURA                     │
│   • Persistencia PostgreSQL (Supabase / Pooler)             │
│   • Seguridad JWT (Filtros, Tokens, Hasheo BCrypt)          │
│   • Logging y Configuración de Sistema (Logback)            │
└─────────────────────────────────────────────────────────────┘
  ▲                                                       ▲
  └─────────────── CAPA TRANSVERSAL (CROSSCUTTING) ───────┘
                  • Manejador Global de Excepciones
                  • Anotaciones (@UsuarioActual)
```

### Estructura de Paquetes en el Backend (`backend/src/main/java/com/latin/dream/erp/`):

```
backend/
├── presentation/
│   └── controller/
│       ├── AuthController.java          # Autenticación y emisión JWT
│       ├── CatalogoController.java      # Modelos, medidas y materiales
│       ├── ComisionesController.java    # Cálculo y exportación de comisiones
│       ├── DashboardController.java     # KPIs de planta y gráfico donut/ritmo
│       ├── DespachoController.java      # Flota, rutas y confirmación de entrega
│       ├── OrdenController.java         # Gestión del ciclo de órdenes
│       └── ProduccionController.java    # Kanban de trazabilidad y avances
├── application/
│   ├── dto/
│   │   ├── AuthRequest.java / AuthResponse.java
│   │   ├── OrdenRequest.java / OrdenResponse.java
│   └── service/
│       ├── AuthService.java             # Validación de credenciales y roles
│       ├── ComisionesService.java       # Auditoría de comisiones por modelo
│       ├── DashboardService.java        # Consolidación de métricas ejecutivas
│       ├── DespachoService.java         # Operaciones de rutas y despachos
│       ├── NumeroOrdenService.java      # Generador de correlativo OP-AAAA-NNNN
│       ├── OrdenService.java            # Casos de uso de órdenes de fabricación
│       ├── ProduccionService.java       # Lógica de lotes y movimiento de etapas
│       └── ReporteExcelService.java     # Motor de exportación XLSX (Apache POI)
├── domain/
│   ├── entity/
│   │   ├── Despacho.java                # Unidad de transporte y entrega
│   │   ├── Lote.java                    # Unidad de trazabilidad en planta
│   │   ├── LoteAvance.java              # Historial de cambios de etapa
│   │   ├── Modelo.java                  # Ficha técnica de colchón
│   │   ├── Orden.java                   # Pedido de fabricación
│   │   ├── PrecioComision.java          # Tarifario de comisiones vigente
│   │   └── Usuario.java                 # Operador o directivo de planta
│   └── repository/
│       ├── DespachoRepository.java
│       ├── LoteAvanceRepository.java
│       ├── LoteRepository.java
│       ├── ModeloRepository.java
│       ├── OrdenRepository.java
│       ├── PrecioComisionRepository.java
│       └── UsuarioRepository.java
├── infrastructure/
│   └── security/
│       ├── JwtAuthFilter.java           # Interceptor de tokens Bearer
│       ├── JwtConfig.java               # Configuración de llaves y expiración
│       └── JwtService.java              # Firma HMAC y parseo de Claims
├── crosscutting/
│   ├── annotation/
│   │   └── UsuarioActual.java           # Inyección de usuario autenticado
│   └── exception/
│       ├── BusinessException.java       # Excepciones de negocio tipadas
│       └── GlobalExceptionHandler.java  # Respuestas HTTP uniformes {codigo, mensaje}
└── config/
    ├── DataSeeder.java                  # Poblador de datos en PostgreSQL
    ├── DatabaseUrlEnvironmentPostProcessor.java
    ├── SecurityConfig.java              # Reglas CORS y filtros de seguridad
    └── SeedProperties.java              # Propiedades de inicio
```

---

## 2. Cumplimiento de Principios SOLID

| Principio | Aplicación en el Proyecto |
|---|---|
| **S — Single Responsibility (Responsabilidad Única)** | Cada clase tiene una sola razón para cambiar. Por ejemplo, `ReporteExcelService` se encarga exclusivamente de maquetar hojas de cálculo con Apache POI; `NumeroOrdenService` solo gestiona correlativos; y `ComisionesController` solo recibe y responde peticiones HTTP sin ejecutar lógica de persistencia. |
| **O — Open/Closed (Abierto/Cerrado)** | Los componentes están abiertos a extensión y cerrados a modificación. La entidad `LoteAvance` permite añadir nuevos tipos de trazabilidad y áreas productivas sin alterar la estructura básica de `Lote`. Los filtros de ordenación aceptan especificaciones dinámicas vía Spring Data JPA. |
| **L — Liskov Substitution (Sustitución de Liskov)** | Los repositorios heredan de `JpaRepository` sin alterar el contrato base. Las respuestas de los controladores manejan tipos de retorno coherentes (`ResponseEntity<T>`), permitiendo sustituciones transparentes en pruebas unitarias y mocks. |
| **I — Interface Segregation (Segregación de Interfaces)** | En lugar de un repositorio o servicio monolítico masivo, existen repositorios específicos por entidad (`LoteRepository`, `DespachoRepository`, `PrecioComisionRepository`). Ninguna clase se ve forzada a depender de métodos de persistencia que no utiliza. |
| **D — Dependency Inversion (Inversión de Dependencias)** | Los servicios y controladores dependen exclusivamente de interfaces y abstracciones (`OrdenRepository`, `PasswordEncoder`, `AuthenticationManager`), inyectadas por Spring IoC mediante inyección por constructor. El dominio no depende de la base de datos ni de frameworks externos. |

---

## 3. Librerías Clave y su Uso

### A) Apache POI (`org.apache.poi:poi` & `poi-ooxml` v5.2.5)
- **Dónde se usa**: [`ReporteExcelService.java`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/java/com/latin/dream/erp/application/service/ReporteExcelService.java).
- **Propósito**: Generación nativa en memoria de reportes ejecutivos en formato Microsoft Excel (`.xlsx`).
- **Características aplicadas**:
  - Creación de hojas de cálculo con `XSSFWorkbook` y `XSSFSheet`.
  - Estilización corporativa: encabezados oscuros, fuentes tipográficas destacadas y paleta industrial Latin Dream.
  - Formato numérico contable (`$#,##0.00`) para precios base y comisiones.
  - Fila de sumatorias y totales dinámicos.
  - Ajuste automático del ancho de columnas (`sheet.autoSizeColumn()`).
  - Transmisión en streaming mediante `ByteArrayOutputStream` hacia el navegador.

### B) Logback (`ch.qos.logback` vía SLF4J)
- **Dónde se usa**: Configurado en [`logback-spring.xml`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/resources/logback-spring.xml) y utilizado en todas las capas mediante `LoggerFactory.getLogger(...)`.
- **Propósito**: Trazabilidad detallada, auditoría operativa y diagnóstico de incidentes en producción.
- **Características aplicadas**:
  - Salida coloreada en consola con indicación de hilo, clase y nivel.
  - `RollingFileAppender` persistiendo en `logs/erp-produccion.log`.
  - Rotación automática diaria y compresión en archivos `.gz` con política de retención de 30 días y límite de 1 GB.
  - Niveles de log segregados por capas: `DEBUG` para controladores web, `INFO` para servicios de aplicación y dominio.

### C) Apache Commons Lang (`org.apache.commons:commons-lang3` v3.14.0)
- **Dónde se usa**: [`NumeroOrdenService.java`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/java/com/latin/dream/erp/application/service/NumeroOrdenService.java), [`DespachoService.java`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/java/com/latin/dream/erp/application/service/DespachoService.java), [`ProduccionService.java`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/java/com/latin/dream/erp/application/service/ProduccionService.java) y [`ReporteExcelService.java`](file:///c:/Users/Sebas301/Documents/GitHub/Sistema-Trazabilidad-Fabrica/backend/src/main/java/com/latin/dream/erp/application/service/ReporteExcelService.java).
- **Propósito**: Robustez en el manejo de cadenas, protección contra valores nulos y formateo de correlativos.
- **Funciones clave utilizadas**:
  - `StringUtils.leftPad(...)`: Generación de secuencias con relleno de ceros (`OP-2026-0001`).
  - `StringUtils.defaultIfBlank(...)`: Sustitución segura de valores nulos o vacíos por valores por defecto.
  - `StringUtils.capitalize(...)` y `StringUtils.lowerCase(...)`: Estandarización de estados de lote y despacho.
  - `StringUtils.split(...)`: Extracción segura de nombres para generar iniciales de avatares.

---

## 4. Mapa de APIs RESTful

Prefijo base: `http://localhost:8080/api/v1`

| Método | Endpoint | Descripción | Body / Parámetros | Respuesta / Códigos |
|---|---|---|---|---|
| `POST` | `/auth/login` | Inicia sesión y genera token Bearer JWT. | `{ "correo": "...", "password": "..." }` | `200 OK`: `{ token, nombre, correo, rol }`<br>`401 Unauthorized` |
| `GET` | `/resumen` | Datos del dashboard: KPIs, ritmo, gráfico donut y seguimiento. | Ninguno | `200 OK`: `DashboardData` JSON |
| `GET` | `/ordenes` | Lista de órdenes de fabricación con filtros. | `?estado=&prioridad=` | `200 OK`: `List<OrdenResponse>` |
| `GET` | `/ordenes/{numero}` | Detalle de orden por código correlativo. | `numero` (path) | `200 OK`: `OrdenResponse`<br>`404 Not Found` |
| `POST` | `/ordenes/borrador` | Guarda orden preliminar en estado borrador. | `OrdenRequest` JSON | `200 OK`: `OrdenResponse` |
| `POST` | `/ordenes/{id}/crear` | Confirma orden y crea su lote inicial en Corte. | `id` (path) | `200 OK`: `OrdenResponse` |
| `GET` | `/produccion` | Vista Kanban: lotes clasificados por 5 etapas de planta. | Ninguno | `200 OK`: `{ metricas, areas, totalLotes, totalUnidades }` |
| `GET` | `/produccion/lotes` | Lista plana de todos los lotes de planta. | Ninguno | `200 OK`: `List<LoteDto>` |
| `POST` | `/produccion/avances` | Registra el movimiento de un lote a una nueva etapa. | `{ "lote_id": "...", "area_destino": "...", "notas": "..." }` | `200 OK`: Lote actualizado |
| `GET` | `/comisiones` | Cálculo de comisiones por modelo y distribución. | Ninguno | `200 OK`: `ComisionesData` JSON |
| `GET` | `/comisiones/reporte/excel` | Descarga de reporte Excel (.xlsx) con Apache POI. | Ninguno | `200 OK`: Archivo binario `Reporte_Comisiones_LatinDream.xlsx` |
| `GET` | `/despachos` | Lista de rutas de despacho del día e historial. | Ninguno | `200 OK`: `DespachosData` JSON |
| `PATCH` | `/despachos/{codigo}/estado` | Actualiza estado de despacho (`transito`, `entregado`). | `{ "estado": "entregado" }` | `200 OK`: `{ codigo, estado, mensaje }` |
| `GET` | `/catalogos` | Catálogo de modelos, medidas, rellenos y usuarios. | Ninguno | `200 OK`: `{ modelos, dimensiones, rellenos, usuarios }` |

---

## 5. Catálogo de Servicios

### Backend (Spring Boot Services):
1. **`AuthService`**: Valida credenciales, genera tokens con HMAC-SHA256 y verifica roles del usuario.
2. **`OrdenService`**: Gestiona el ciclo de vida de órdenes, validaciones de fecha de entrega y creación del lote inicial.
3. **`NumeroOrdenService`**: Asigna correlativos atómicos únicos por año (`OP-AAAA-NNNN`) usando Apache Commons.
4. **`ProduccionService`**: Agrupa y procesa lotes en áreas de trabajo (`Corte`, `Ensamblaje`, `Tapizado`, `Calidad`, `Listo`) y registra transiciones.
5. **`DashboardService`**: Agrega estadísticas en tiempo real de producción, lotes retrasados y estados del flujo.
6. **`ComisionesService`**: Calcula tarifas vigentes, montos devengados, comisiones pendientes y distribución porcentual.
7. **`DespachoService`**: Controla la asignación de choferes, unidades en ruta y actualización del estado de entrega.
8. **`ReporteExcelService`**: Ensambla libros Excel XLSX con estilos, formatos numéricos y metadatos usando Apache POI.

### Frontend (Angular Services):
1. **`AuthService`**: Administra sesión en `localStorage`, observables de autenticación e interceptor HTTP Bearer.
2. **`OrdenesService`**: Consume `/api/v1/ordenes` y `/api/v1/catalogos` para listas y creación de pedidos.
3. **`ProduccionService`**: Conecta el tablero Kanban interactivo y emite avances de área.
4. **`DashboardService`**: Proporciona datos para Chart.js y tarjetas métricas.
5. **`ComisionesService`**: Consume comisiones dinámicas y activa la descarga del Excel generado por Apache POI.
6. **`DespachosService`**: Consulta despachos activos y actualiza estados de ruta.

---

## 6. Pila Tecnológica

- **Frontend**: Angular 17, TypeScript, SCSS modular, Bootstrap 5, Chart.js.
- **Backend**: Java 17/24, Spring Boot 3.5.x, Spring Data JPA, Spring Security, Hibernate 6.
- **Librerías Auxiliares**: Apache POI 5.2.5, Apache Commons Lang3 3.14.0, Logback, JJWT 0.12.3, Lombok.
- **Base de Datos**: PostgreSQL 17 alojado en **Supabase**.
- **Gestión de Dependencias**: Maven 3.9+ (Backend) y npm / Node.js (Frontend).

---

## 7. Instalación y Puesta en Marcha

### Requisitos Previos:
- Java JDK 17 o superior.
- Node.js 18+ y npm.
- Acceso a internet para conectar con Supabase PostgreSQL.

### 2. Ejecutar el Backend:
```bash
cd backend
mvn spring-boot:run
```
*El backend iniciará en `http://localhost:8080/api/v1` y aplicará `backend/src/main/resources/schema.sql` y validará las tablas. La semilla solo se activa con `APP_SEED_ENABLED=true`.*

### 3. Ejecutar el Frontend:
```bash
cd frontend
npm install
npm run start -- --port 4200
```
*Abrir navegador en `http://localhost:4200/`.*

---

## 8. Usuarios y Credenciales de Acceso

Con `APP_SEED_ENABLED=true` se crean los siguientes usuarios si la tabla está vacía. Sus contraseñas corresponden a `APP_SEED_PASSWORD`:

| Nombre | Correo Electrónico | Contraseña | Rol / Permiso |
|---|---|---|---|
| **María Rojas** | `maria.rojas@latindream.com` | `APP_SEED_PASSWORD` | **Supervisora** (Acceso global de planta) |
| **José Martínez** | `jose.martinez@latindream.com` | `APP_SEED_PASSWORD` | **Gerente** (Dirección y comisiones) |
| **Valeria Soto** | `valeria.soto@latindream.com` | `APP_SEED_PASSWORD` | **Vendedora** (Ingreso de órdenes) |
| **Roberto Díaz** | `roberto.diaz@latindream.com` | `APP_SEED_PASSWORD` | **Encargado de Área** (Avances de lote) |
| **Lucía Fernández** | `lucia.fernandez@latindream.com` | `APP_SEED_PASSWORD` | **Contabilidad** (Auditoría de comisiones) |
| **Diego Ruiz** | `diego.ruiz@latindream.com` | `APP_SEED_PASSWORD` | **Conductor** (Despacho y entrega) |

## Correcciones y operación

- Configura una clave JWT aleatoria de al menos 32 bytes en `JWT_SECRET`. Los secretos publicados anteriormente deben rotarse en el proveedor; quitarlos del repositorio no los revoca.
- `APP_SEED_ENABLED` está desactivado por defecto y controla toda la carga de datos de prueba.
- Cada modelo define su categoría, dimensiones y relleno; la orden usa esas especificaciones.
- Producción avanza Corte → Ensamblaje → Tapizado → Calidad → Listo. La salida de Calidad exige `aprobado`; `rechazado` permite volver a Tapizado.
- Las comisiones se consultan por mes de creación de la orden y usan la tarifa vigente en su fecha de registro. La liquidación se habilita solo después de la entrega total y congela cantidades, precio, porcentaje e importe. El pago requiere una referencia y conserva usuario y fecha. Pantalla y Excel comparten el cálculo y el período.
- En producción, sirve el frontend junto a un proxy para `/api/v1` hacia el backend.
- Comprobaciones: `cd backend; mvn test` y `cd frontend; npm test; npm run build`.

## Flujos operativos completos

- **Borradores:** admiten campos parciales. Se pueden retomar desde Órdenes, editar y eliminar; la confirmación exige todos los datos y una fecha de entrega vigente. El creador original se conserva en la auditoría.
- **Creación directa:** guarda y confirma dentro de una transacción. `solicitudId` permite repetir una petición sin duplicarla; si cambian los datos, usa otro identificador.
- **Producción:** ofrece Kanban y lista en escritorio, instrucciones de fabricación e historial de cada lote. Al completar todas las entregas de una orden, sale del tablero activo. El historial sigue disponible desde Órdenes.
- **Despachos:** permite varios lotes y, para cada uno, lote completo o cantidad parcial. Al programar reserva las unidades; editar un despacho programado recalcula la reserva. Cancelar antes de salir libera el saldo. Las transiciones son Programado → En tránsito → Entregado, o Programado → Cancelado. Entregado y Cancelado son terminales.
- **Despachos anteriores sin asociaciones:** se pueden asociar sus lotes desde Editar, incluso si ya estaban en tránsito. No se permite confirmar entregas sin lotes.
- **Comisiones:** Estimado / Sin tarifa → Pendiente (liquidado, tras entrega total) → Pagado (con referencia). No se ejecutan transferencias bancarias: se registra un pago realizado.
- **Modelos y tarifas:** gerencia y supervisión gestionan el catálogo desde `/administracion`. Las especificaciones de modelos usados quedan protegidas; pueden desactivarse. Las vigencias de tarifas no se solapan y los importes liquidados permanecen inmutables.
- **Acceso:** sin Recordarme, la sesión usa `sessionStorage`; al marcarlo usa `localStorage`. La recuperación de contraseña queda pendiente de definición; la interfaz informa de ello.
- **Encabezado:** búsqueda por orden o modelo y consulta de alertas de retraso actuales.

### Comprobaciones locales

`mvn test` ejecuta pruebas de permisos y regresión, además de recorridos con repositorios y transacciones reales sobre H2 aislada. El bloqueo asesor específico de PostgreSQL se sustituye en esas pruebas; nunca se escriben datos de prueba en Supabase. `npm test` comprueba edición, reintentos, sesiones y cantidades de despacho. `npm run build` valida las plantillas Angular y genera producción.
