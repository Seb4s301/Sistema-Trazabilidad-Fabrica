import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Orden {
  id?: string;
  numero: string;
  modelo: string;
  modeloId?: string | null;
  categoria: string;
  cantidad: number | null;
  prioridad: string;
  estado: string;
  lote?: string;
  loteId?: string;
  entrega: string;
  observaciones?: string;
  fechaEntrega?: string;
  turno?: string;
  registradoPor?: string;
}

export interface OrdenFiltros {
  estado?: string;
  prioridad?: string;
}

export interface OrdenCreateRequest {
  solicitudId?: string;
  modelo_id: string | null;
  cantidad: number | null;
  prioridad: string | null;
  fecha_entrega: string | null;
  observaciones?: string;
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
    let params = new HttpParams();
    if (filtros?.estado) params = params.set('estado', filtros.estado);
    if (filtros?.prioridad) params = params.set('prioridad', filtros.prioridad);
    return this.http.get<Orden[]>(ENDPOINTS.ordenes, { params });
  }

  crearOrden(data: OrdenCreateRequest): Observable<Orden> {
    return this.http.post<Orden>(ENDPOINTS.ordenes, this.toBackendPayload(data));
  }
  getOrden(numero: string): Observable<Orden> { return this.http.get<Orden>(`${ENDPOINTS.ordenes}/${encodeURIComponent(numero)}`); }
  editarBorrador(id: string, data: OrdenCreateRequest): Observable<Orden> {
    return this.http.put<Orden>(`${ENDPOINTS.ordenes}/${id}/borrador`, this.toBackendPayload(data));
  }
  eliminarBorrador(id: string): Observable<void> { return this.http.delete<void>(`${ENDPOINTS.ordenes}/${id}/borrador`); }
  historial(numero: string): Observable<import('../../shared/historial.component').RegistroHistorial[]> {
    return this.http.get<import('../../shared/historial.component').RegistroHistorial[]>(`${ENDPOINTS.ordenes}/${encodeURIComponent(numero)}/historial`);
  }

  confirmarBorrador(id: string): Observable<Orden> {
    return this.http.post<Orden>(`${ENDPOINTS.ordenes}/${id}/crear`, {});
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
      solicitudId: data.solicitudId,
      modeloId: data.modelo_id,
      cantidad: data.cantidad,
      prioridad: data.prioridad,
      fechaEntrega: data.fecha_entrega,
      observaciones: data.observaciones ?? ''
    };
  }

  getCatalogos(): Observable<Catalogos> {
    return this.http.get<Catalogos>(`${ENDPOINTS.catalogos}`);
  }

}
