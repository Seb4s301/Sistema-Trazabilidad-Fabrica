import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  notaTipo?: 'success' | 'warning' | 'danger' | 'muted';
}

export interface RitmoProduccion {
  dia: string;
  valor: number;
  meta: number;
  destacado?: boolean;
}

export interface DonutItem {
  label: string;
  valor: number;
  color: string;
}

export interface OrdenSeguimiento {
  orden: string;
  modelo: string;
  cantidad: number;
  etapa: string;
  estado: string;
  entrega: string;
  estadoClase: string;
}

export interface DashboardData {
  kpis: Kpi[];
  ritmo: RitmoProduccion[];
  maxRitmo: number;
  donut: DonutItem[];
  totalOrdenesActivas: number;
  seguimiento: OrdenSeguimiento[];
  semana: number;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {

  constructor(private http: HttpClient) {}

  getDashboard(): Observable<DashboardData> {
    return this.http.get<DashboardData>(`${ENDPOINTS.dashboard}`);
  }

  // Métodos helper para cuando el backend no esté disponible (fallback a datos demo)
  getDemoData(): DashboardData {
    return {
      semana: 42,
      kpis: [
        { etiqueta: 'Colchones producidos', valor: '1.248', nota: '+8,6 % vs. semana anterior', notaTipo: 'success' },
        { etiqueta: 'Eficiencia de ensamblaje', valor: '92,4 %', nota: 'Meta de línea: 90 %', notaTipo: 'success' },
        { etiqueta: 'Lotes con retraso', valor: '2', nota: 'Requieren seguimiento', notaTipo: 'danger' },
        { etiqueta: 'Aprobación de calidad', valor: '98,2 %', nota: '+1,4 pp vs. semana anterior', notaTipo: 'success' }
      ],
      ritmo: [
        { dia: 'Lun', valor: 196, meta: 240 },
        { dia: 'Mar', valor: 224, meta: 240 },
        { dia: 'Mié', valor: 208, meta: 240 },
        { dia: 'Jue', valor: 332, meta: 240 },
        { dia: 'Vie', valor: 288, meta: 240, destacado: true }
      ],
      maxRitmo: 400,
      donut: [
        { label: 'En proceso', valor: 6, color: '#D4A935' },
        { label: 'Pendientes de corte', valor: 2, color: '#1B1D21' },
        { label: 'Listas para despacho', valor: 1, color: '#1E7B4F' }
      ],
      totalOrdenesActivas: 9,
      seguimiento: [
        { orden: 'OP-2026-0420', modelo: 'Comfort Plus · Queen', cantidad: 120, etapa: 'Ensamblaje', estado: 'Retrasado', entrega: '15 oct.', estadoClase: 'danger' },
        { orden: 'OP-2026-0418', modelo: 'Dream Classic · 2 Plazas', cantidad: 100, etapa: 'Tapizado', estado: 'Retrasado', entrega: '15 oct.', estadoClase: 'danger' },
        { orden: 'OP-2026-0422', modelo: 'Ortopedic Pro · King', cantidad: 60, etapa: 'Calidad', estado: 'En plazo', entrega: '17 oct.', estadoClase: 'warning' },
        { orden: 'OP-2026-0424', modelo: 'Dream Premium · Queen', cantidad: 80, etapa: 'Listo para despacho', estado: 'Completado', entrega: '18 oct.', estadoClase: 'success' }
      ]
    };
  }
}
