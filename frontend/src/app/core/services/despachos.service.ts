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
  codigo: string;
  lote?: string;
  cliente: string;
  destino: string;
  unidades: number;
  vehiculo: string;
  conductor: string;
  salida: string;
  entregaEstimada: string;
  estado: 'programado' | 'transito' | 'entregado';
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

@Injectable({ providedIn: 'root' })
export class DespachosService {

  constructor(private http: HttpClient) {}

  getDespachos(): Observable<DespachosData> {
    return this.http.get<DespachosData>(`${ENDPOINTS.despachos}`);
  }

  actualizarEstadoDespacho(codigo: string, estado: string): Observable<Despacho> {
    return this.http.patch<Despacho>(`${ENDPOINTS.despachos}/${codigo}/estado`, { estado });
  }

  // Demo data fallback
  getDemoData(): DespachosData {
    const d = new Date();
    const meses = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','setiembre','octubre','noviembre','diciembre'];
    const texto = `${d.getDate()} ${meses[d.getMonth()]} ${d.getFullYear()}`;

    const despachos: Despacho[] = [
      { codigo: 'DS-1048', lote: 'LT-2610-0424 · Dream Premium Queen', cliente: 'Comercial Andina', destino: 'Lima Centro', unidades: 80, vehiculo: 'LD-201', conductor: 'Diego Ruiz', salida: '11:00', entregaEstimada: '13:30', estado: 'programado' },
      { codigo: 'DS-1047', lote: 'LT-2610-0422 · Comfort Plus King', cliente: 'Casa Norte', destino: 'Lima Norte', unidades: 60, vehiculo: 'LD-202', conductor: 'Carla Vega', salida: '08:00', entregaEstimada: '11:30', estado: 'transito' },
      { codigo: 'DS-1046', lote: 'LT-2610-0420 · Ortopedic Pro Queen', cliente: 'Muebles del Sur', destino: 'Lima Sur', unidades: 40, vehiculo: 'LD-203', conductor: 'Marco Silva', salida: '07:00', entregaEstimada: '09:45', estado: 'entregado' }
    ];

    const programados = despachos.length;
    const unidades = despachos.reduce((a, x) => a + x.unidades, 0);
    const transito = despachos.filter(x => x.estado === 'transito').length;
    const entregados = despachos.filter(x => x.estado === 'entregado').length;

    return {
      tituloFecha: `Despachos del ${texto}`,
      resumen: `${programados} despachos programados · ${unidades} colchones · ${transito} en tránsito y ${entregados} entregado`,
      banner: 'Diego Ruiz confirmó la preparación de carga del despacho DS-1048 a las 10:43.',
      kpis: [
        { etiqueta: 'Despachos del día', valor: String(programados), nota: `${transito} rutas activas en operación` },
        { etiqueta: 'Unidades en ruta', valor: String(unidades), nota: '3 vehículos asignados' },
        { etiqueta: 'En tránsito', valor: String(transito), nota: 'DS-1047 · Lima Norte', tipo: 'transito' },
        { etiqueta: 'Entregados', valor: String(entregados), nota: 'DS-1046 · Lima Sur', tipo: 'entregado' }
      ],
      despachos,
      historial: [
        { despacho: 'DS-1048', cliente: 'Comercial Andina', unidades: 80, estado: 'Programado', salida: '13:30', entrega: '13:30', vehiculo: 'LD-201' },
        { despacho: 'DS-1047', cliente: 'Casa Norte', unidades: 60, estado: 'En tránsito', salida: '08:00', entrega: '11:30', vehiculo: 'LD-202' },
        { despacho: 'DS-1046', cliente: 'Muebles del Sur', unidades: 40, estado: 'Entregado', salida: '07:00', entrega: '09:45', vehiculo: 'LD-203' }
      ]
    };
  }
}
