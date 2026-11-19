# Base de datos y pruebas

El esquema canónico está en [schema.sql](../backend/src/main/resources/schema.sql).

Al iniciar, el backend lo aplica de forma idempotente antes de validar las entidades (`DB_INIT_MODE=always`, valor por defecto). Añade columnas y tablas sin borrar registros y permite campos nulos en órdenes para guardar borradores parciales. Para una instalación con migraciones externas, ejecuta el SQL antes del despliegue y configura `DB_INIT_MODE=never`.

Las nuevas columnas son `ordenes.solicitud_id`, `despachos.solicitud_id` y `despachos.entregado_en`. La tabla nueva `liquidaciones_comision` conserva importes liquidados y registros de pago. `despacho_lotes` e `historial_ordenes`, ya existentes, ahora tienen entidades y flujos de aplicación.

Las pruebas de integración usan H2 en memoria con repositorios, restricciones, bloqueos de fila y transacciones reales. El bloqueo asesor de PostgreSQL se sustituye en ese entorno: no certifican por sí solas el comportamiento de ese bloqueo ni ejecutan cambios contra Supabase.
