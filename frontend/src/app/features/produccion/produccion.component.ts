import { Component } from '@angular/core';

interface Metrica {
  etiqueta: string;
  valor: string;
  peligro?: boolean;
}

interface Lote {
  orden: string;
  modelo: string;
  plaza: string;
  unidades: number;
  codigo: string;
  prioridad: 'Alta' | 'Normal' | 'Baja';
  responsable: string;
  iniciales: string;
  entrega: string;
  diasRetraso?: number;
  aprobado?: boolean;
}

interface Area {
  id: string;
  nombre: string;
  subtitulo: string;
  lotes: Lote[];
}

@Component({
  selector: 'app-produccion',
  templateUrl: './produccion.component.html',
  styleUrls: ['./produccion.component.scss']
})
export class ProduccionComponent {

  vista: 'tablero' | 'lista' = 'tablero';

  busqueda = '';
  filtroPrioridad = '';
  filtroResponsable = '';
  filtroEntrega = '';

  metricas: Metrica[] = [
    { etiqueta: 'Lotes en planta', valor: '8' },
    { etiqueta: 'Unidades en planta', valor: '780' },
    { etiqueta: 'Lotes con retraso', valor: '2', peligro: true },
    { etiqueta: 'Listas para despacho', valor: '80' }
  ];

  areas: Area[] = [
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
  ];

  get totalLotes(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.length, 0);
  }

  get totalUnidades(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.reduce((s, l) => s + l.unidades, 0), 0);
  }

  lotesFiltrados(area: Area): Lote[] {
    const b = this.busqueda.toLowerCase();
    return area.lotes.filter(l =>
      (!b || l.orden.toLowerCase().includes(b) || l.codigo.toLowerCase().includes(b) || l.modelo.toLowerCase().includes(b)) &&
      (!this.filtroPrioridad || l.prioridad === this.filtroPrioridad) &&
      (!this.filtroResponsable || l.responsable === this.filtroResponsable)
    );
  }

  get responsables(): string[] {
    const set = new Set<string>();
    this.areas.forEach(a => a.lotes.forEach(l => set.add(l.responsable)));
    return Array.from(set);
  }

  clasePrioridad(p: string): string {
    if (p === 'Alta') return 'pill-danger';
    if (p === 'Normal') return 'pill-warning';
    return 'pill-slate';
  }
}
