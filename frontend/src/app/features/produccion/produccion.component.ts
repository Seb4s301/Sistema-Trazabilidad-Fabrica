import { RegistroHistorial } from '../../shared/historial.component';
import { AuthService } from '../../core/auth.service';
import { Component, OnInit, inject } from '@angular/core';
import { ProduccionService, Lote, Metrica, AreaProduccion } from '../../core/services/produccion.service';

@Component({
  selector: 'app-produccion',
  templateUrl: './produccion.component.html',
  styleUrls: ['./produccion.component.scss']
})
export class ProduccionComponent implements OnInit {

  public authService = inject(AuthService);

  private produccionService = inject(ProduccionService);

  historialLote: Lote | null = null;
  movimientos: RegistroHistorial[] = []; historialCargando = false; historialError = '';

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
  errorCarga = '';

  get totalLotes(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.length, 0);
  }

  get totalUnidades(): number {
    return this.areas.reduce((acc, a) => acc + a.lotes.reduce((s, l) => s + (l.pendientes ?? l.unidades), 0), 0);
  }

  ngOnInit(): void {
    this.cargarProduccion();
  }

  cargarProduccion(): void {
    this.errorCarga = '';
    this.cargando = true;
    this.produccionService.getProduccion().subscribe({
      next: (data) => {
        this.metricas = data.metricas;
        this.areas = data.areas;
        this.cargando = false;
      },
      error: () => {
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.metricas = []; this.areas = [];
        this.cargando = false;
      }
    });
  }

  lotesFiltrados(area: AreaProduccion): Lote[] {
    const b = this.busqueda.toLowerCase();
    return area.lotes.filter(l =>
      (!b || l.orden.toLowerCase().includes(b) || l.codigo.toLowerCase().includes(b) || l.modelo.toLowerCase().includes(b)) &&
      (!this.filtroPrioridad || l.prioridad === this.filtroPrioridad) &&
      (!this.filtroResponsable || l.responsable === this.filtroResponsable) &&
      (!this.filtroEntrega || (this.filtroEntrega === 'retrasados' ? !!l.retrasado : this.entregaEstaSemana(l.fecha_entrega)))
    );
  }

  private entregaEstaSemana(fecha?: string): boolean {
    if (!fecha) return false;
    const lunes = new Date();
    lunes.setHours(0, 0, 0, 0);
    lunes.setDate(lunes.getDate() - (lunes.getDay() + 6) % 7);
    const fin = new Date(lunes);
    fin.setDate(fin.getDate() + 7);
    const entrega = new Date(fecha + 'T00:00:00');
    return entrega >= lunes && entrega < fin;
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

  verHistorial(lote: Lote): void {
    this.historialLote = lote; this.movimientos = []; this.historialError = ''; this.historialCargando = true;
    this.produccionService.historial(lote.id!).subscribe({ next: datos => { this.movimientos = datos; this.historialCargando = false; },
      error: () => { this.historialError = 'No se pudo consultar el historial.'; this.historialCargando = false; } });
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

}
