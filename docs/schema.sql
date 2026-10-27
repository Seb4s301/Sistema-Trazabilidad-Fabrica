-- Latin Dream · Modelo de datos de producción
-- PostgreSQL 15+ (Supabase). Derivado de las pantallas del diseño:
-- Login, Resumen, Órdenes (nueva orden), Producción, Comisiones, Despacho.
-- Convención: nombres en español, snake_case, claves bigint identity.
 
BEGIN;
 
-- ============ Enums ============
CREATE TYPE rol_usuario AS ENUM (
  'gerente', 'supervisora', 'vendedora', 'encargado_area',
  'contabilidad', 'caja_chica', 'conductor'
);
CREATE TYPE prioridad AS ENUM ('alta', 'normal', 'baja');
CREATE TYPE estado_orden AS ENUM (
  'borrador', 'pendiente_corte', 'en_proceso', 'listo_despacho', 'completada', 'cancelada'
);
CREATE TYPE resultado_avance AS ENUM ('en_curso', 'aprobado', 'rechazado');
CREATE TYPE estado_comision AS ENUM ('pendiente', 'en_revision', 'pagado');
CREATE TYPE estado_vehiculo AS ENUM ('disponible', 'listo_para_carga', 'en_ruta', 'en_mantenimiento');
CREATE TYPE estado_despacho AS ENUM ('programado', 'en_transito', 'entregado', 'cancelado');
CREATE TYPE tipo_documento AS ENUM ('orden_despacho', 'guia_remision');
 
-- ============ Usuarios y turnos ============
CREATE TABLE usuarios (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  auth_id    uuid UNIQUE,                       -- id en Supabase Auth, si se usa
  nombre     text NOT NULL,                     -- "María Rojas"
  correo     text NOT NULL UNIQUE,
  rol        rol_usuario NOT NULL,
  activo     boolean NOT NULL DEFAULT true,
  creado_en  timestamptz NOT NULL DEFAULT now()
);
 
CREATE TABLE turnos (
  id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo       text NOT NULL UNIQUE,            -- 'A'
  hora_inicio  time NOT NULL,                   -- 06:00
  hora_fin     time NOT NULL                    -- 14:00
);
 
-- ============ Catálogos ============
CREATE TABLE tipos_producto (
  id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre  text NOT NULL UNIQUE                  -- Colchón, Tarima, Cabecera
);
 
CREATE TABLE dimensiones (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre     text NOT NULL UNIQUE,              -- 1.5 Plazas, 2 Plazas, Queen, King
  ancho_cm   integer,
  largo_cm   integer,
  altura_cm  integer
  -- Queen = 160 x 200 x 30 cm (según pantalla). Completar el resto con medidas reales.
);
 
CREATE TABLE modelos (
  id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  tipo_producto_id  bigint NOT NULL REFERENCES tipos_producto(id),
  nombre            text NOT NULL UNIQUE,       -- Dream Classic, Dream Premium, Comfort Plus, Ortopedic Pro
  linea             text,                       -- "premium", etc.
  codigo_producto   text,                       -- DP-QUE-MF (texto de referencia en la pantalla)
  activo            boolean NOT NULL DEFAULT true
);
 
CREATE TABLE rellenos (
  id           bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre       text NOT NULL UNIQUE,            -- Resortes Ensacados, Memory Foam
  descripcion  text
);
 
CREATE TABLE areas_produccion (
  id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo  text NOT NULL UNIQUE,                 -- corte, ensamblaje, tapizado, calidad, listo_despacho
  nombre  text NOT NULL,                        -- Corte, Ensamblaje, Tapizado, Calidad, Listo para despacho
  orden   smallint NOT NULL UNIQUE              -- 1..5, define el orden del tablero
);
 
CREATE TABLE precios_comision (
  id                   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  modelo_id            bigint NOT NULL REFERENCES modelos(id),
  dimension_id         bigint NOT NULL REFERENCES dimensiones(id),
  precio_base          numeric(10,2) NOT NULL CHECK (precio_base >= 0),
  porcentaje_comision  numeric(5,2) NOT NULL CHECK (porcentaje_comision BETWEEN 0 AND 100),
  vigente_desde        date NOT NULL,
  UNIQUE (modelo_id, dimension_id, vigente_desde)
);
 
CREATE TABLE clientes (
  id         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  nombre     text NOT NULL UNIQUE,              -- Comercial Andina, Casa Norte, Muebles del Sur
  zona       text,                              -- Lima Centro, Lima Norte, Lima Sur
  direccion  text,
  contacto   text
);
 
-- ============ Órdenes y lotes ============
CREATE SEQUENCE orden_numero_seq START 1;
 
CREATE TABLE ordenes (
  id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero          text NOT NULL UNIQUE
                  DEFAULT ('OP-' || to_char(now(), 'YYYY') || '-' || lpad(nextval('orden_numero_seq')::text, 4, '0')),
  modelo_id       bigint NOT NULL REFERENCES modelos(id),
  dimension_id    bigint NOT NULL REFERENCES dimensiones(id),
  relleno_id      bigint NOT NULL REFERENCES rellenos(id),
  cantidad        integer NOT NULL CHECK (cantidad > 0),
  prioridad       prioridad NOT NULL DEFAULT 'normal',
  fecha_entrega   date NOT NULL,
  observaciones   text,
  estado          estado_orden NOT NULL DEFAULT 'borrador',
  turno_id        bigint REFERENCES turnos(id),
  creado_por      bigint NOT NULL REFERENCES usuarios(id),
  creado_en       timestamptz NOT NULL DEFAULT now(),
  actualizado_en  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON ordenes (estado);
CREATE INDEX ON ordenes (fecha_entrega);
 
-- Historial de cambios ("Los cambios quedarán registrados en el historial")
CREATE TABLE historial_ordenes (
  id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  orden_id        bigint NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
  campo           text NOT NULL,
  valor_anterior  text,
  valor_nuevo     text,
  usuario_id      bigint REFERENCES usuarios(id),
  cambiado_en     timestamptz NOT NULL DEFAULT now()
);
 
-- Un lote por orden. Código generado por la app: LT-YYMM-NNNN (LT-2610-0420 para OP-2026-0420).
CREATE TABLE lotes (
  id                     bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  orden_id               bigint NOT NULL UNIQUE REFERENCES ordenes(id),
  codigo                 text NOT NULL UNIQUE,
  area_actual_id         bigint REFERENCES areas_produccion(id),
  responsable_actual_id  bigint REFERENCES usuarios(id),
  completado_en          timestamptz,
  creado_en              timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON lotes (area_actual_id);
 
CREATE TABLE lote_avances (
  id              bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  lote_id         bigint NOT NULL REFERENCES lotes(id) ON DELETE CASCADE,
  area_id         bigint NOT NULL REFERENCES areas_produccion(id),
  responsable_id  bigint NOT NULL REFERENCES usuarios(id),
  inicio          timestamptz NOT NULL DEFAULT now(),
  fin             timestamptz,
  resultado       resultado_avance NOT NULL DEFAULT 'en_curso',
  observaciones   text
);
CREATE INDEX ON lote_avances (lote_id, inicio);
 
-- ============ Comisiones ============
CREATE TABLE comisiones (
  id                   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  periodo              date NOT NULL,            -- primer día del mes: 2026-10-01
  modelo_id            bigint NOT NULL REFERENCES modelos(id),
  dimension_id         bigint NOT NULL REFERENCES dimensiones(id),
  relleno_id           bigint REFERENCES rellenos(id),
  orden_id             bigint REFERENCES ordenes(id),
  unidades             integer NOT NULL CHECK (unidades >= 0),
  precio_base          numeric(10,2) NOT NULL CHECK (precio_base >= 0),
  porcentaje_comision  numeric(5,2) NOT NULL CHECK (porcentaje_comision BETWEEN 0 AND 100),
  monto_total          numeric(12,2) GENERATED ALWAYS AS
                       (round(unidades * precio_base * porcentaje_comision / 100, 2)) STORED,
  estado               estado_comision NOT NULL DEFAULT 'pendiente',
  creado_en            timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON comisiones (periodo, estado);
 
-- ============ Despacho y transporte ============
CREATE TABLE vehiculos (
  id                   bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  codigo               text NOT NULL UNIQUE,      -- LD-201
  capacidad_colchones  integer NOT NULL CHECK (capacidad_colchones > 0),
  estado               estado_vehiculo NOT NULL DEFAULT 'disponible',
  ultima_revision      date
);
 
CREATE TABLE despachos (
  id                 bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  numero             text NOT NULL UNIQUE,         -- DS-1048
  cliente_id         bigint NOT NULL REFERENCES clientes(id),
  destino            text NOT NULL,                -- Lima Centro
  vehiculo_id        bigint NOT NULL REFERENCES vehiculos(id),
  conductor_id       bigint NOT NULL REFERENCES usuarios(id),
  unidades           integer NOT NULL CHECK (unidades > 0),
  guia_remision      text NOT NULL UNIQUE,         -- GR-2026-1048
  estado             estado_despacho NOT NULL DEFAULT 'programado',
  salida_programada  timestamptz NOT NULL,
  entrega_estimada   timestamptz,
  entregado_en       timestamptz,
  creado_por         bigint NOT NULL REFERENCES usuarios(id),
  creado_en          timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON despachos (salida_programada);
CREATE INDEX ON despachos (estado);
 
CREATE TABLE despacho_lotes (
  despacho_id  bigint NOT NULL REFERENCES despachos(id) ON DELETE CASCADE,
  lote_id      bigint NOT NULL REFERENCES lotes(id),
  cantidad     integer NOT NULL CHECK (cantidad > 0),
  PRIMARY KEY (despacho_id, lote_id)
);
 
CREATE TABLE despacho_eventos (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  despacho_id   bigint NOT NULL REFERENCES despachos(id) ON DELETE CASCADE,
  estado        estado_despacho NOT NULL,
  nota          text,                              -- "Diego Ruiz confirmó la carga"
  usuario_id    bigint REFERENCES usuarios(id),
  registrado_en timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON despacho_eventos (despacho_id, registrado_en);
 
CREATE TABLE documentos_despacho (
  id            bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  despacho_id   bigint NOT NULL REFERENCES despachos(id) ON DELETE CASCADE,
  tipo          tipo_documento NOT NULL,
  nombre        text NOT NULL,                     -- DS-1048 / GR-2026-1048
  storage_path  text NOT NULL,
  tamano_bytes  integer,
  UNIQUE (despacho_id, tipo)
);


-- ============ Seguridad (Supabase) ============
-- El frontend no consulta estas tablas directamente: todo pasa por Spring Boot.
-- Con RLS activo y sin políticas, anon y authenticated no pueden leer ni escribir.
-- Spring Boot se conecta con el rol del dueño de las tablas (postgres), que no está
-- sujeto a RLS, o con service_role. No usar la clave anon en el backend.
 
ALTER TABLE usuarios            ENABLE ROW LEVEL SECURITY;
ALTER TABLE turnos              ENABLE ROW LEVEL SECURITY;
ALTER TABLE tipos_producto      ENABLE ROW LEVEL SECURITY;
ALTER TABLE dimensiones         ENABLE ROW LEVEL SECURITY;
ALTER TABLE modelos             ENABLE ROW LEVEL SECURITY;
ALTER TABLE rellenos            ENABLE ROW LEVEL SECURITY;
ALTER TABLE areas_produccion    ENABLE ROW LEVEL SECURITY;
ALTER TABLE precios_comision    ENABLE ROW LEVEL SECURITY;
ALTER TABLE clientes            ENABLE ROW LEVEL SECURITY;
ALTER TABLE ordenes             ENABLE ROW LEVEL SECURITY;
ALTER TABLE historial_ordenes   ENABLE ROW LEVEL SECURITY;
ALTER TABLE lotes               ENABLE ROW LEVEL SECURITY;
ALTER TABLE lote_avances        ENABLE ROW LEVEL SECURITY;
ALTER TABLE comisiones          ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehiculos           ENABLE ROW LEVEL SECURITY;
ALTER TABLE despachos           ENABLE ROW LEVEL SECURITY;
ALTER TABLE despacho_lotes      ENABLE ROW LEVEL SECURITY;
ALTER TABLE despacho_eventos    ENABLE ROW LEVEL SECURITY;
ALTER TABLE documentos_despacho ENABLE ROW LEVEL SECURITY;
 
-- Quitar acceso directo de la API pública de Supabase.
REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM anon, authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON TABLES    FROM anon, authenticated;
ALTER DEFAULT PRIVILEGES IN SCHEMA public REVOKE ALL ON SEQUENCES FROM anon, authenticated;
 
COMMIT;



