-- ============================================================================
-- Latin Dream · Esquema PostgreSQL (Supabase)
-- Sistema de trazabilidad de producción — fábrica de colchones (Lima, Perú)
--
-- IMPORTANTE: ejecutar este script en Supabase (SQL Editor) ANTES de arrancar
-- el backend, porque application.yml usa spring.jpa.hibernate.ddl-auto=validate.
--
-- Los IDs son VARCHAR(36) porque las entidades Java usan String (UUID en texto,
-- ej. "550e8400-e29b-41d4-a716-446655440000"). Así el validador de Hibernate
-- coincide el tipo de columna con el de la entidad.
-- ============================================================================

-- Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(50) NOT NULL, -- gerente, supervisora, vendedora, encargado_area, contabilidad, caja_chica, conductor
    turno VARCHAR(20),        -- Turno A / Turno B
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: modelos de colchón
CREATE TABLE IF NOT EXISTS modelos (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    categoria VARCHAR(50) NOT NULL, -- 1.5 Plazas, 2 Plazas, Queen, King
    dimensiones VARCHAR(100) NOT NULL, -- ej. "160 × 200 cm · Altura: 30 cm"
    relleno VARCHAR(100) NOT NULL,    -- Resortes Ensacados, Memory Foam, etc.
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Tabla: órdenes de fabricación
CREATE TABLE IF NOT EXISTS ordenes (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    numero VARCHAR(20) NOT NULL UNIQUE, -- OP-AAAA-NNNN
    modelo_id VARCHAR(36) NOT NULL REFERENCES modelos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad >= 1),
    prioridad VARCHAR(20) NOT NULL,     -- Alta, Normal, Baja
    fecha_entrega DATE NOT NULL,
    observaciones VARCHAR(1000),
    estado VARCHAR(30) NOT NULL DEFAULT 'borrador', -- borrador, pendiente_corte, en_proceso, calidad, listo_despacho, completado, cancelado
    registrado_por VARCHAR(36) REFERENCES usuarios(id),
    turno VARCHAR(20),
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: lotes (un lote por orden)
CREATE TABLE IF NOT EXISTS lotes (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    codigo VARCHAR(20) NOT NULL UNIQUE, -- LT-AAMM-NNNN
    orden_id VARCHAR(36) NOT NULL REFERENCES ordenes(id),
    unidades INTEGER NOT NULL CHECK (unidades >= 1),
    area_actual VARCHAR(30) NOT NULL DEFAULT 'Corte', -- Corte, Ensamblaje, Tapizado, Calidad, Listo
    fecha_entrega DATE NOT NULL,
    retrasado BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: avances de lote (trazabilidad por movimiento de área)
CREATE TABLE IF NOT EXISTS lote_avances (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    lote_id VARCHAR(36) NOT NULL REFERENCES lotes(id),
    area_origen VARCHAR(30),
    area_destino VARCHAR(30) NOT NULL,
    responsable_id VARCHAR(36) NOT NULL REFERENCES usuarios(id),
    inicio TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    fin TIMESTAMP,
    resultado VARCHAR(20), -- aprobado, rechazado (solo en Calidad)
    notas VARCHAR(1000),
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: historial de órdenes
CREATE TABLE IF NOT EXISTS historial_ordenes (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    orden_id VARCHAR(36) NOT NULL REFERENCES ordenes(id),
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(36) REFERENCES usuarios(id),
    fecha TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    notas VARCHAR(1000)
);

-- Tabla: despachos
CREATE TABLE IF NOT EXISTS despachos (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    codigo VARCHAR(20) NOT NULL UNIQUE, -- DS-XXXX
    cliente VARCHAR(150) NOT NULL,
    destino VARCHAR(200) NOT NULL,
    unidades INTEGER NOT NULL CHECK (unidades >= 1),
    estado VARCHAR(30) NOT NULL DEFAULT 'programado', -- programado, en_transito, entregado, cancelado
    fecha_salida DATE,
    fecha_entrega_estimada DATE,
    vehiculo VARCHAR(50),
    conductor VARCHAR(100),
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP,
    actualizado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: lotes asignados a despacho
CREATE TABLE IF NOT EXISTS despacho_lotes (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    despacho_id VARCHAR(36) NOT NULL REFERENCES despachos(id),
    lote_id VARCHAR(36) NOT NULL REFERENCES lotes(id),
    cantidad INTEGER NOT NULL CHECK (cantidad >= 1),
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- Tabla: precios de comisión vigentes
CREATE TABLE IF NOT EXISTS precios_comision (
    id VARCHAR(36) PRIMARY KEY DEFAULT (gen_random_uuid()::text),
    modelo_id VARCHAR(36) NOT NULL REFERENCES modelos(id),
    categoria VARCHAR(50) NOT NULL,
    precio_base NUMERIC(12,2) NOT NULL,
    porcentaje NUMERIC(5,2) NOT NULL, -- % de comisión
    vigente_desde DATE NOT NULL,
    vigente_hasta DATE,
    creado_en TIMESTAMP NOT NULL DEFAULT LOCALTIMESTAMP
);

-- ============================================================================
-- Índices útiles
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_ordenes_numero ON ordenes(numero);
CREATE INDEX IF NOT EXISTS idx_ordenes_estado ON ordenes(estado);
CREATE INDEX IF NOT EXISTS idx_lotes_codigo ON lotes(codigo);
CREATE INDEX IF NOT EXISTS idx_lotes_area ON lotes(area_actual);
CREATE INDEX IF NOT EXISTS idx_despachos_estado ON despachos(estado);
CREATE INDEX IF NOT EXISTS idx_usuarios_correo ON usuarios(correo);

-- ============================================================================
-- Formato de error de la API: { "codigo": "...", "mensaje": "..." } en español
-- ============================================================================
