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
  estado: 'Pagado' | 'Pendiente' | 'En revisión';
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

  getComisiones(): Observable<ComisionesData> {
    return this.http.get<ComisionesData>(`${ENDPOINTS.comisiones}`);
  }

  exportarReporteExcel(): Observable<Blob> {
    return this.http.get(`${ENDPOINTS.comisiones}/reporte/excel`, { responseType: 'blob' });
  }

  // Demo data fallback
  getDemoData(): ComisionesData {
    return {
      kpis: [
        { etiqueta: 'Total Comisiones del Mes ($)', valor: '$11.660', nota: 'Octubre 2026 · 6 filas' },
        { etiqueta: 'Comisiones Pendientes de Pago ($)', valor: '$5.900', nota: 'Incluye $1.500 en revisión', notaTipo: 'warning' },
        { etiqueta: 'Modelo de Mayor Rendimiento', valor: 'Comfort Plus', nota: '$4.560 · 2 categorías' },
        { etiqueta: 'Promedio de Comisión por Unidad Vendida/Producida ($)', valor: '$16,66', nota: '700 unidades · Octubre 2026' }
      ],
      filas: [
        { tipo: 'Modelo', modelo: 'Ortopedic Pro', categoria: 'Queen', unidades: 120, precioBase: 400, porcentaje: 5, monto: 2400, estado: 'Pagado' },
        { tipo: 'Modelo', modelo: 'Dream Classic', categoria: '2 Plazas', unidades: 200, precioBase: 250, porcentaje: 4, monto: 2000, estado: 'Pendiente' },
        { tipo: 'Modelo', modelo: 'Comfort Plus', categoria: 'King', unidades: 80, precioBase: 500, porcentaje: 6, monto: 2400, estado: 'Pendiente' },
        { tipo: 'Modelo', modelo: 'Ortopedic Pro', categoria: 'King', unidades: 60, precioBase: 500, porcentaje: 5, monto: 1500, estado: 'En revisión' },
        { tipo: 'Modelo', modelo: 'Dream Classic', categoria: '1.5 Plazas', unidades: 150, precioBase: 200, porcentaje: 4, monto: 1200, estado: 'Pagado' },
        { tipo: 'Modelo', modelo: 'Comfort Plus', categoria: 'Queen', unidades: 90, precioBase: 400, porcentaje: 6, monto: 2160, estado: 'Pagado' }
      ],
      distribucionModelo: [
        { label: 'Ortopedic Pro', valor: 3900 },
        { label: 'Dream Classic', valor: 3200 },
        { label: 'Comfort Plus', valor: 4560 }
      ],
      distribucionRelleno: [
        { label: 'Resortes ensacados', valor: 7100 },
        { label: 'Memory Foam', valor: 4560 }
      ],
      maxModelo: 5000,
      maxRelleno: 8000
    };
  }

  montoTexto(valor: number): string {
    return '$' + valor.toLocaleString('es-PE', { minimumFractionDigits: 0 });
  }
}
