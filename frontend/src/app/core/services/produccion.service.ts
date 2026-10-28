import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENDPOINTS } from '../api.config';

export interface Lote {
  id?: string;
  codigo: string;
  orden: string;
  modelo: string;
  plaza: string;
  unidades: number;
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

  getLotes(): Observable<Lote[]> {
    return this.http.get<Lote[]>(`${ENDPOINTS.lotes}`);
  }

  actualizarLote(id: string, data: Partial<Lote>): Observable<Lote> {
    return this.http.patch<Lote>(`${ENDPOINTS.lotes}/${id}`, data);
  }

  registrarAvance(data: AvanceLote): Observable<AvanceLote> {
    return this.http.post<AvanceLote>(`${ENDPOINTS.produccion}/avances`, data);
  }

  // Demo data fallback
  getDemoData(): ProduccionData {
    return {
      metricas: [
        { etiqueta: 'Lotes en planta', valor: '8' },
        { etiqueta: 'Unidades en planta', valor: '780' },
        { etiqueta: 'Lotes con retraso', valor: '2', peligro: true },
        { etiqueta: 'Listas para despacho', valor: '80' }
      ],
      areas: [
        {
          id: 'corte',
          nombre: 'Corte',
          subtitulo: 'Corte de telas y espumas · 240 unidades',
          lotes: [
            { orden: 'OP-2026-0419', modelo: 'Eco Sleep', plaza: '2 Plazas', unidades: 60, codigo: 'LT-2610-0419', prioridad: 'Normal', responsable: 'Ana Torres', iniciales: 'AT', entrega: '24 oct. 2026' },
            { orden: 'OP-2026-0418', modelo: 'Comfort Plus', plaza: 'Queen', unidades: 180, codigo: 'LT-2610-0418', prioridad: 'Alta', responsable: 'Luis García', iniciales: 'LG', entrega: '22 oct. 2026', diasRetraso: 1 }
          ]
        },
        {
          id: 'ensamblaje',
          nombre: 'Ensamblaje',
          subtitulo: 'Núcleo y estructura · 190 unidades',
          lotes: [
            { orden: 'OP-2026-0417', modelo: 'Dream Rest', plaza: 'King', unidades: 80, codigo: 'LT-2610-0417', prioridad: 'Normal', responsable: 'Carla Ruiz', iniciales: 'CR', entrega: '21 oct. 2026' },
            { orden: 'OP-2026-0416', modelo: 'Premium Rest', plaza: '1.5 Plazas', unidades: 110, codigo: 'LT-2610-0416', prioridad: 'Baja', responsable: 'Jorge Lima', iniciales: 'JL', entrega: '25 oct. 2026' }
          ]
        },
        {
          id: 'tapizado',
          nombre: 'Tapizado',
          subtitulo: 'Costura y tapizado final · 160 unidades',
          lotes: [
            { orden: 'OP-2026-0415', modelo: 'Comfort Plus', plaza: 'Queen', unidades: 160, codigo: 'LT-2610-0415', prioridad: 'Alta', responsable: 'María Rojas', iniciales: 'MR', entrega: '20 oct. 2026', diasRetraso: 2 }
          ]
        },
        {
          id: 'calidad',
          nombre: 'Calidad',
          subtitulo: 'Inspección y control · 110 unidades',
          lotes: [
            { orden: 'OP-2026-0414', modelo: 'Eco Sleep', plaza: '2 Plazas', unidades: 110, codigo: 'LT-2610-0414', prioridad: 'Normal', responsable: 'Diego Ruiz', iniciales: 'DR', entrega: '19 oct. 2026' }
          ]
        },
        {
          id: 'listo',
          nombre: 'Listo',
          subtitulo: 'Alistados para despacho · 80 unidades',
          lotes: [
            { orden: 'OP-2026-0413', modelo: 'Premium Rest', plaza: '1.5 Plazas', unidades: 80, codigo: 'LT-2610-0413', prioridad: 'Normal', responsable: 'Sofía Melgar', iniciales: 'SM', entrega: '18 oct. 2026', aprobado: true }
          ]
        }
      ],
      totalLotes: 8,
      totalUnidades: 780
    };
  }
}
