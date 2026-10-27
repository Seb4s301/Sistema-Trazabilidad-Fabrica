import { Component } from '@angular/core';

@Component({
  selector: 'app-ordenes',
  templateUrl: './ordenes.component.html',
  styleUrls: ['./ordenes.component.scss']
})
export class OrdenesComponent {

  filtroEstado = '';
  filtroPrioridad = '';

  ordenes = [
    { numero: 'OP-2026-0420', modelo: 'Comfort Plus', categoria: 'Queen', cantidad: 120, prioridad: 'Alta', estado: 'pendiente_corte', entrega: '24 oct. 2026', lote: 'LT-2610-0420' },
    { numero: 'OP-2026-0419', modelo: 'Dream Rest', categoria: 'King', cantidad: 80, prioridad: 'Normal', estado: 'en_proceso', entrega: '22 oct. 2026', lote: 'LT-2610-0419' },
    { numero: 'OP-2026-0418', modelo: 'Comfort Plus', categoria: 'Queen', cantidad: 150, prioridad: 'Alta', estado: 'en_proceso', entrega: '20 oct. 2026', lote: 'LT-2610-0418' },
    { numero: 'OP-2026-0417', modelo: 'Eco Sleep', categoria: '2 Plazas', cantidad: 60, prioridad: 'Baja', estado: 'calidad', entrega: '18 oct. 2026', lote: 'LT-2610-0417' },
    { numero: 'OP-2026-0416', modelo: 'Premium Rest', categoria: '1.5 Plazas', cantidad: 45, prioridad: 'Normal', estado: 'listo_despacho', entrega: '17 oct. 2026', lote: 'LT-2610-0416' },
    { numero: 'OP-2026-0415', modelo: 'Dream Rest', categoria: 'King', cantidad: 200, prioridad: 'Normal', estado: 'borrador', entrega: '30 oct. 2026', lote: '—' }
  ];

  estados: Record<string, { label: string; clase: string }> = {
    borrador: { label: 'Borrador', clase: 'slate' },
    pendiente_corte: { label: 'Pendiente de corte', clase: 'warning' },
    en_proceso: { label: 'En proceso', clase: 'warning' },
    calidad: { label: 'En calidad', clase: 'slate' },
    listo_despacho: { label: 'Listo para despacho', clase: 'success' },
    completado: { label: 'Completado', clase: 'success' },
    cancelado: { label: 'Cancelado', clase: 'danger' }
  };

  get ordenesFiltradas() {
    return this.ordenes.filter(o =>
      (!this.filtroEstado || o.estado === this.filtroEstado) &&
      (!this.filtroPrioridad || o.prioridad === this.filtroPrioridad)
    );
  }

  estadoDe(orden: any) {
    return this.estados[orden.estado] ?? { label: orden.estado, clase: 'slate' };
  }

  clasePrioridad(p: string): string {
    if (p === 'Alta') return 'pill-danger';
    if (p === 'Normal') return 'pill-slate';
    return 'pill-slate';
  }
}
