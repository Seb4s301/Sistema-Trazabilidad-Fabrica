import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Orden {
  id?: string;
  numero: string;
  modelo: string;
  categoria: string;
  cantidad: number;
  prioridad: string;
  estado: string;
  lote?: string;
  entrega: string;
  observaciones?: string;
  fechaEntrega?: string;
  turno?: string;
  registradoPor?: string;
}

export interface OrdenFiltros {
  estado?: string;
  prioridad?: string;
  busqueda?: string;
}

export interface OrdenCreateRequest {
  modelo_id: string;
  dimension_id: string;
  relleno_id: string;
  cantidad: number;
  prioridad: string;
  fecha_entrega: string;
  observaciones?: string;
  turno: string;
}

export interface Modelo {
  id: string;
  codigo: string;
  nombre: string;
  categoria: string;
  dimensiones: string;
  relleno: string;
  activo: boolean;
}

export interface Dimension {
  id: string;
  nombre: string;
  ancho_cm: number;
  largo_cm: number;
  altura_cm: number;
  medidas: string;
}

export interface Relleno {
  id: string;
  nombre: string;
  descripcion: string;
}

export interface Catalogos {
  modelos: Modelo[];
  dimensiones: Dimension[];
  rellenos: Relleno[];
  usuarios: any[];
}

@Injectable({ providedIn: 'root' })
export class OrdenesService {

  constructor(private http: HttpClient) {}

  getOrdenes(filtros?: OrdenFiltros): Observable<Orden[]> {
    let url = ENDPOINTS.ordenes;
    const params: string[] = [];
    if (filtros?.estado) params.push(`estado=${filtros.estado}`);
    if (filtros?.prioridad) params.push(`prioridad=${filtros.prioridad}`);
    if (filtros?.busqueda) params.push(`busqueda=${filtros.busqueda}`);
    if (params.length) url += '?' + params.join('&');
    return this.http.get<Orden[]>(url);
  }

  getOrden(id: string): Observable<Orden> {
    return this.http.get<Orden>(`${ENDPOINTS.ordenes}/${id}`);
  }

  /**
   * El backend trabaja en dos pasos: primero guarda el borrador y luego
   * confirma la orden (genera número OP-XXXX-NNNN y su lote).
   */
  crearOrden(data: OrdenCreateRequest): Observable<Orden> {
    return this.http
      .post<{ id: string }>(`${ENDPOINTS.ordenes}/borrador`, this.toBackendPayload(data))
      .pipe(
        switchMap(borrador =>
          this.http.post<Orden>(`${ENDPOINTS.ordenes}/${borrador.id}/crear`, {})
        )
      );
  }

  guardarBorrador(data: OrdenCreateRequest): Observable<Orden> {
    return this.http.post<Orden>(
      `${ENDPOINTS.ordenes}/borrador`,
      this.toBackendPayload(data)
    );
  }

  /** Traduce los campos del formulario al DTO que espera Spring Boot. */
  private toBackendPayload(data: OrdenCreateRequest) {
    return {
      modeloId: data.modelo_id,
      cantidad: data.cantidad,
      prioridad: data.prioridad,
      fechaEntrega: data.fecha_entrega,
      observaciones: data.observaciones ?? ''
    };
  }

  actualizarOrden(id: string, data: Partial<Orden>): Observable<Orden> {
    return this.http.put<Orden>(`${ENDPOINTS.ordenes}/${id}`, data);
  }

  eliminarOrden(id: string): Observable<void> {
    return this.http.delete<void>(`${ENDPOINTS.ordenes}/${id}`);
  }

  getCatalogos(): Observable<Catalogos> {
    return this.http.get<Catalogos>(`${ENDPOINTS.catalogos}`);
  }

  // Demo data fallback
  getDemoOrdenes(): Orden[] {
    return [
      { numero: 'OP-2026-0420', modelo: 'Comfort Plus', categoria: 'Queen', cantidad: 120, prioridad: 'Alta', estado: 'pendiente_corte', entrega: '24 oct. 2026', lote: 'LT-2610-0420' },
      { numero: 'OP-2026-0419', modelo: 'Dream Rest', categoria: 'King', cantidad: 80, prioridad: 'Normal', estado: 'en_proceso', entrega: '22 oct. 2026', lote: 'LT-2610-0419' },
      { numero: 'OP-2026-0418', modelo: 'Comfort Plus', categoria: 'Queen', cantidad: 150, prioridad: 'Alta', estado: 'en_proceso', entrega: '20 oct. 2026', lote: 'LT-2610-0418' },
      { numero: 'OP-2026-0417', modelo: 'Eco Sleep', categoria: '2 Plazas', cantidad: 60, prioridad: 'Baja', estado: 'calidad', entrega: '18 oct. 2026', lote: 'LT-2610-0417' },
      { numero: 'OP-2026-0416', modelo: 'Premium Rest', categoria: '1.5 Plazas', cantidad: 45, prioridad: 'Normal', estado: 'listo_despacho', entrega: '17 oct. 2026', lote: 'LT-2610-0416' },
      { numero: 'OP-2026-0415', modelo: 'Dream Rest', categoria: 'King', cantidad: 200, prioridad: 'Normal', estado: 'borrador', entrega: '30 oct. 2026', lote: '—' }
    ];
  }

  getDemoCatalogos(): Catalogos {
    return {
      modelos: [
        { id: '1', codigo: 'CP-001', nombre: 'Comfort Plus', categoria: 'Queen', dimensiones: '160 × 200 cm · Altura: 30 cm', relleno: 'Resortes Ensacados', activo: true },
        { id: '2', codigo: 'DR-002', nombre: 'Dream Rest', categoria: 'King', dimensiones: '180 × 200 cm · Altura: 30 cm', relleno: 'Memory Foam', activo: true },
        { id: '3', codigo: 'ES-003', nombre: 'Eco Sleep', categoria: '2 Plazas', dimensiones: '140 × 190 cm · Altura: 26 cm', relleno: 'Resortes Ensacados', activo: true },
        { id: '4', codigo: 'PR-004', nombre: 'Premium Rest', categoria: '1.5 Plazas', dimensiones: '120 × 190 cm · Altura: 26 cm', relleno: 'Memory Foam', activo: true }
      ],
      dimensiones: [
        { id: '1.5', nombre: '1.5 Plazas', ancho_cm: 120, largo_cm: 190, altura_cm: 26, medidas: '1.5 Plazas: 120 × 190 cm · Altura: 26 cm' },
        { id: '2', nombre: '2 Plazas', ancho_cm: 140, largo_cm: 190, altura_cm: 26, medidas: '2 Plazas: 140 × 190 cm · Altura: 26 cm' },
        { id: 'queen', nombre: 'Queen', ancho_cm: 160, largo_cm: 200, altura_cm: 30, medidas: 'Queen: 160 × 200 cm · Altura: 30 cm' },
        { id: 'king', nombre: 'King', ancho_cm: 180, largo_cm: 200, altura_cm: 30, medidas: 'King: 180 × 200 cm · Altura: 30 cm' }
      ],
      rellenos: [
        { id: 'resortes', nombre: 'Resortes Ensacados', descripcion: 'Núcleo de resortes individuales envueltos en bolsas de tela para soporte independiente.' },
        { id: 'memory', nombre: 'Memory Foam', descripcion: 'Espuma viscoelástica que se adapta al cuerpo y alivia la presión.' }
      ],
      usuarios: []
    };
  }
}
