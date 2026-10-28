import { Component, OnInit, inject } from '@angular/core';
import { ProduccionService, ProduccionData, Lote, Metrica, AreaProduccion } from '../../core/services/produccion.service';

@Component({
  selector: 'app-produccion',
  templateUrl: './produccion.component.html',
  styleUrls: ['./produccion.component.scss']
})
export class ProduccionComponent implements OnInit {

  private produccionService = inject(ProduccionService);

  vista: 'tablero' | 'lista' = 'tablero';

  busqueda = '';
  filtroPrioridad = '';
  filtroResponsable = '';
  filtroEntrega = '';

  modalAvanceVisible = false;
  loteSeleccionado: Lote | null = null;

  metricas: Metrica[] = [];
  areas: AreaProduccion[] = [];
  cargando = false;

  get totalLotes(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.length, 0);
  }

  get totalUnidades(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.reduce((s, l) => s + l.unidades, 0), 0);
  }

  ngOnInit(): void {
    this.cargarProduccion();
  }

  cargarProduccion(): void {
    this.cargando = true;
    this.produccionService.getProduccion().subscribe({
      next: (data) => {
        this.metricas = data.metricas;
        this.areas = data.areas;
        this.cargando = false;
      },
      error: () => {
        const demo = this.produccionService.getDemoData();
        this.metricas = demo.metricas;
        this.areas = demo.areas;
        this.cargando = false;
      }
    });
  }

  lotesFiltrados(area: AreaProduccion): Lote[] {
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
    if (p === 'Alta') return 'pill-warning';
    if (p === 'Normal') return 'pill-slate';
    return 'pill-slate';
  }

  abrirModalAvance(lote: Lote): void {
    this.loteSeleccionado = lote;
    this.modalAvanceVisible = true;
  }

  cerrarModalAvance(): void {
    this.modalAvanceVisible = false;
    this.loteSeleccionado = null;
  }

  onAvanceRegistrado(): void {
    this.cerrarModalAvance();
    this.cargarProduccion(); // Recargar datos
  }

  onFiltroCambio(): void {
    // Los filtros son reactivos con [(ngModel)]
  }
}
