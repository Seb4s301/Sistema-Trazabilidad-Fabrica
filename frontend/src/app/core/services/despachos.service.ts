import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  tipo?: string;
}

export interface Despacho {
  solicitudId?: string;
  lotes: { loteId: string; codigo: string; modelo: string; cantidad: number; unidades: number }[];
  codigo: string;
  lote?: string;
  cliente: string;
  destino: string;
  unidades: number;
  vehiculo: string;
  conductor: string;
  salida: string;
  entregaEstimada: string;
  estado: 'programado' | 'transito' | 'entregado' | 'cancelado';
  fecha_salida?: string;
  fecha_entrega_estimada?: string;
}

export interface HistorialItem {
  despacho: string;
  cliente: string;
  unidades: number;
  estado: string;
  salida: string;
  entrega: string;
  vehiculo: string;
}

export interface DespachosData {
  tituloFecha: string;
  resumen: string;
  banner: string;
  kpis: Kpi[];
  despachos: Despacho[];
  historial: HistorialItem[];
}

export interface LoteDisponible { id: string; codigo: string; orden: string; modelo: string; unidades: number; disponibles: number; }
export interface DespachoRequest { solicitudId: string; cliente: string; destino: string; vehiculo: string; conductor: string; fechaSalida: string; fechaEntregaEstimada: string; lotes: { loteId: string; cantidad: number }[]; }

@Injectable({ providedIn: 'root' })
export class DespachosService {

  constructor(private http: HttpClient) {}

  getDespachos(): Observable<DespachosData> {
    return this.http.get<DespachosData>(`${ENDPOINTS.despachos}`);
  }

  disponibles(): Observable<LoteDisponible[]> { return this.http.get<LoteDisponible[]>(`${ENDPOINTS.despachos}/lotes-disponibles`); }
  crear(datos: DespachoRequest): Observable<Despacho> { return this.http.post<Despacho>(ENDPOINTS.despachos, datos); }
  editar(codigo: string, datos: DespachoRequest): Observable<Despacho> { return this.http.put<Despacho>(`${ENDPOINTS.despachos}/${codigo}`, datos); }

  actualizarEstadoDespacho(codigo: string, estado: string): Observable<Despacho> {
    return this.http.patch<Despacho>(`${ENDPOINTS.despachos}/${codigo}/estado`, { estado });
  }

}
