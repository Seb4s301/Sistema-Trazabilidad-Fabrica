import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface ComisionFila {
  tipo: string;
  modelo: string;
  categoria: string;
  unidades: number;
  precioBase: number;
  porcentaje: number;
  monto: number;
  estado: 'Estimado' | 'Sin tarifa' | 'Pendiente' | 'Pagado';
  ordenId: string; liquidacionId?: string; puedeLiquidar: boolean; referenciaPago?: string; liquidadoEn?: string; pagadoEn?: string;
}

export interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  notaTipo?: string;
}

export interface DistribucionItem {
  label: string;
  valor: number;
}

export interface ComisionesData {
  kpis: Kpi[];
  filas: ComisionFila[];
  distribucionModelo: DistribucionItem[];
  distribucionRelleno: DistribucionItem[];
  maxModelo: number;
  maxRelleno: number;
}

@Injectable({ providedIn: 'root' })
export class ComisionesService {

  constructor(private http: HttpClient) {}

  getComisiones(periodo?: string): Observable<ComisionesData> {
    return this.http.get<ComisionesData>(`${ENDPOINTS.comisiones}`, { params: periodo ? { periodo } : {} });
  }

  exportarReporteExcel(periodo?: string): Observable<Blob> {
    return this.http.get(`${ENDPOINTS.comisiones}/reporte/excel`, { responseType: 'blob', params: periodo ? { periodo } : {} });
  }

  liquidar(id: string): Observable<ComisionFila> { return this.http.post<ComisionFila>(`${ENDPOINTS.comisiones}/ordenes/${id}/liquidar`, {}); }
  pagar(id: string, referencia: string): Observable<unknown> { return this.http.post(`${ENDPOINTS.comisiones}/liquidaciones/${id}/pago`, { referencia }); }
  montoTexto(valor: number): string {
    return '$' + valor.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
