import { Component } from '@angular/core';

interface ComisionFila {
  tipo: string;
  modelo: string;
  categoria: string;
  unidades: number;
  precioBase: number;
  porcentaje: number;
  monto: number;
  estado: 'Pagado' | 'Pendiente' | 'En revisión';
}

interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  notaTipo?: string;
}

@Component({
  selector: 'app-comisiones',
  templateUrl: './comisiones.component.html',
  styleUrls: ['./comisiones.component.scss']
})
export class ComisionesComponent {

  kpis: Kpi[] = [
    { etiqueta: 'Total comisiones del mes', valor: '$11.660', nota: 'Octubre 2026' },
    { etiqueta: 'Comisiones pendientes de pago', valor: '$5.900', nota: 'incluye $1.500 en revisión', notaTipo: 'warning' },
    { etiqueta: 'Modelo de mayor rendimiento', valor: '$4.560', nota: 'Comfort Plus · 2 categorías' },
    { etiqueta: 'Promedio por unidad', valor: '$16,66', nota: '700 unidades' }
  ];

  filas: ComisionFila[] = [
    { tipo: 'Modelo', modelo: 'Comfort Plus', categoria: 'Queen', unidades: 200, precioBase: 456, porcentaje: 5, monto: 4560, estado: 'Pendiente' },
    { tipo: 'Modelo', modelo: 'Comfort Plus', categoria: '1.5 Plazas', unidades: 120, precioBase: 380, porcentaje: 5, monto: 2280, estado: 'Pendiente' },
    { tipo: 'Modelo', modelo: 'Dream Rest', categoria: 'King', unidades: 80, precioBase: 560, porcentaje: 5, monto: 2240, estado: 'Pagado' },
    { tipo: 'Modelo', modelo: 'Eco Sleep', categoria: '2 Plazas', unidades: 150, precioBase: 300, porcentaje: 4, monto: 1800, estado: 'En revisión' },
    { tipo: 'Modelo', modelo: 'Premium Rest', categoria: '1.5 Plazas', unidades: 60, precioBase: 460, porcentaje: 5, monto: 1380, estado: 'Pagado' },
    { tipo: 'Modelo', modelo: 'Eco Sleep', categoria: 'Queen', unidades: 50, precioBase: 400, porcentaje: 4, monto: 800, estado: 'Pendiente' }
  ];

  // Distribución por modelo
  distribucionModelo = [
    { label: 'Comfort Plus', valor: 6840 },
    { label: 'Dream Rest', valor: 2240 },
    { label: 'Eco Sleep', valor: 2600 },
    { label: 'Premium Rest', valor: 1380 }
  ];

  // Distribución por relleno
  distribucionRelleno = [
    { label: 'Resortes Ensacados', valor: 7180 },
    { label: 'Memory Foam', valor: 5880 }
  ];

  maxModelo = 7000;
  maxRelleno = 8000;

  get claseEstado(): Record<string, string> {
    return {
      'Pagado': 'pill-success',
      'Pendiente': 'pill-warning',
      'En revisión': 'pill-slate'
    };
  }

  montoTexto(valor: number): string {
    return '$' + valor.toLocaleString('es-PE', { minimumFractionDigits: 0 });
  }
}
