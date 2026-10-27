import { Component, OnInit } from '@angular/core';

interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  notaTipo?: string;
}

@Component({
  selector: 'app-resumen',
  templateUrl: './resumen.component.html',
  styleUrls: ['./resumen.component.scss']
})
export class ResumenComponent implements OnInit {

  periodoSeleccionado = 'semana42';
  rangoActualizado = 'Datos actualizados a las 10:42';
  vistaGrafico: 'dia' | 'semana' = 'semana';

  semana = 42;

  kpis: Kpi[] = [
    { etiqueta: 'Colchones producidos', valor: '1.248', nota: '+8,6 % vs. semana anterior', notaTipo: 'success' },
    { etiqueta: 'Eficiencia de ensamblaje', valor: '92,4 %', nota: 'meta 90 %', notaTipo: 'success' },
    { etiqueta: 'Lotes con retraso', valor: '2', nota: 'Requieren seguimiento', notaTipo: 'danger' },
    { etiqueta: 'Aprobación de calidad', valor: '98,2 %', nota: '+1,4 pp', notaTipo: 'success' }
  ];

  // Gráfico de barras Lun–Vie (ritmo de producción)
  ritmo = [
    { dia: 'Lun', valor: 218, meta: 240 },
    { dia: 'Mar', valor: 232, meta: 240 },
    { dia: 'Mié', valor: 205, meta: 240 },
    { dia: 'Jue', valor: 241, meta: 240 },
    { dia: 'Vie', valor: 252, meta: 240, destacado: true }
  ];
  maxRitmo = 280;

  // Donut estado de órdenes
  donut = [
    { label: 'En proceso', valor: 6, color: '#D4A935' },
    { label: 'Pendientes de corte', valor: 2, color: '#6B7A8A' },
    { label: 'Listas para despacho', valor: 1, color: '#1E7B4F' }
  ];
  totalOrdenesActivas = 9;

  donutStyle: string = '';

  // Tabla órdenes bajo seguimiento
  seguimiento = [
    { orden: 'OP-2026-0418', modelo: 'Comfort Plus · Queen', cantidad: 120, etapa: 'Tapizado', estado: 'En proceso', entrega: '20 oct. 2026', estadoClase: 'warning' },
    { orden: 'OP-2026-0415', modelo: 'Dream Rest · King', cantidad: 80, etapa: 'Ensamblaje', estado: 'En proceso', entrega: '22 oct. 2026', estadoClase: 'warning' },
    { orden: 'OP-2026-0412', modelo: 'Eco Sleep · 2 Plazas', cantidad: 150, etapa: 'Corte', estado: 'Pendiente', entrega: '24 oct. 2026', estadoClase: 'slate' },
    { orden: 'OP-2026-0409', modelo: 'Comfort Plus · 1.5 Plazas', cantidad: 60, etapa: 'Calidad', estado: 'Listo para despacho', entrega: '18 oct. 2026', estadoClase: 'success' }
  ];

  ngOnInit(): void {
    this.calcularDonut();
  }

  calcularDonut(): void {
    const total = this.donut.reduce((acc, d) => acc + d.valor, 0);
    let acumulado = 0;
    const segmentos = this.donut.map(d => {
      const inicio = (acumulado / total) * 360;
      acumulado += d.valor;
      const fin = (acumulado / total) * 360;
      return `${d.color} ${inicio}deg ${fin}deg`;
    });
    this.donutStyle = `conic-gradient(${segmentos.join(', ')})`;
  }

  porcentaje(valor: number): number {
    return Math.round((valor / this.totalOrdenesActivas) * 100);
  }

  alturaBarra(valor: number): number {
    return (valor / this.maxRitmo) * 100;
  }
}
