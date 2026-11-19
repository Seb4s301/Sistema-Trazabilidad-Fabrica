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
}
