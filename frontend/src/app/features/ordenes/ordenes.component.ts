import { Component, OnInit, inject } from '@angular/core';
import { OrdenesService, Orden, OrdenFiltros } from '../../core/services/ordenes.service';

@Component({
  selector: 'app-ordenes',
  templateUrl: './ordenes.component.html',
  styleUrls: ['./ordenes.component.scss']
})
export class OrdenesComponent implements OnInit {

  private ordenesService = inject(OrdenesService);

  filtroEstado = '';
  filtroPrioridad = '';

  ordenes: Orden[] = [];
  cargando = false;

  estados: Record<string, { label: string; clase: string }> = {
    borrador: { label: 'Borrador', clase: 'slate' },
    pendiente_corte: { label: 'Pendiente de corte', clase: 'warning' },
    en_proceso: { label: 'En proceso', clase: 'warning' },
    calidad: { label: 'En calidad', clase: 'slate' },
    listo_despacho: { label: 'Listo para despacho', clase: 'success' },
    completado: { label: 'Completado', clase: 'success' },
    cancelado: { label: 'Cancelado', clase: 'danger' }
  };

  ngOnInit(): void {
    this.cargarOrdenes();
  }

  cargarOrdenes(): void {
    this.cargando = true;
    const filtros: OrdenFiltros = {};
    if (this.filtroEstado) filtros.estado = this.filtroEstado;
    if (this.filtroPrioridad) filtros.prioridad = this.filtroPrioridad;

    this.ordenesService.getOrdenes(filtros).subscribe({
      next: (data) => {
        this.ordenes = data;
        this.cargando = false;
      },
      error: () => {
        // Fallback a datos demo si el backend no responde
        this.ordenes = this.ordenesService.getDemoOrdenes();
        this.cargando = false;
      }
    });
  }

  get ordenesFiltradas() {
    return this.ordenes.filter(o =>
      (!this.filtroEstado || o.estado === this.filtroEstado) &&
      (!this.filtroPrioridad || o.prioridad === this.filtroPrioridad)
    );
  }

  estadoDe(orden: Orden) {
    return this.estados[orden.estado] ?? { label: orden.estado, clase: 'slate' };
  }

  clasePrioridad(p: string): string {
    if (p === 'Alta') return 'pill-danger';
    if (p === 'Normal') return 'pill-slate';
    return 'pill-slate';
  }

  onFiltroCambio(): void {
    this.cargarOrdenes();
  }

  montoTexto(valor: number): string {
    return '$' + valor.toLocaleString('es-PE', { minimumFractionDigits: 0 });
  }
}
