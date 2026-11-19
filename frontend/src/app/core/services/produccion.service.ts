import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Lote {
  id?: string;
  observaciones?: string;
  codigo: string;
  orden: string;
  modelo: string;
  plaza: string;
  unidades: number;
  disponibles?: number; reservadas?: number; entregadas?: number; pendientes?: number;
  area_actual?: string;
  prioridad: string;
  responsable: string;
  iniciales: string;
  entrega: string;
  diasRetraso?: number;
  aprobado?: boolean;
  fecha_entrega?: string;
  retrasado?: boolean;
}

export interface AreaProduccion {
  id: string;
  nombre: string;
  subtitulo: string;
  lotes: Lote[];
}

export interface Metrica {
  etiqueta: string;
  valor: string;
  peligro?: boolean;
}

export interface AvanceLote {
  id?: string;
  lote_id: string;
  area_origen?: string;
  area_destino: string;
  responsable_id?: string;
  inicio?: string;
  fin?: string;
  resultado?: string;
  notas?: string;
}

export interface ProduccionData {
  metricas: Metrica[];
  areas: AreaProduccion[];
  totalLotes: number;
  totalUnidades: number;
}

@Injectable({ providedIn: 'root' })
export class ProduccionService {

  constructor(private http: HttpClient) {}

  getProduccion(): Observable<ProduccionData> {
    return this.http.get<ProduccionData>(`${ENDPOINTS.produccion}`);
  }

  historial(id: string): Observable<import('../../shared/historial.component').RegistroHistorial[]> {
    return this.http.get<import('../../shared/historial.component').RegistroHistorial[]>(`${ENDPOINTS.produccion}/lotes/${id}/historial`);
  }
  registrarAvance(data: AvanceLote): Observable<AvanceLote> {
    return this.http.post<AvanceLote>(`${ENDPOINTS.produccion}/avances`, data);
  }

}
