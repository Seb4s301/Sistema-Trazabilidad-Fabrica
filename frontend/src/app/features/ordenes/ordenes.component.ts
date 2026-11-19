import { AuthService } from '../../core/auth.service';
import { ProduccionService } from '../../core/services/produccion.service';
import { ActivatedRoute } from '@angular/router';
import { RegistroHistorial } from '../../shared/historial.component';
import { Component, OnInit, inject } from '@angular/core';
import { OrdenesService, Orden, OrdenFiltros } from '../../core/services/ordenes.service';

@Component({
  selector: 'app-ordenes',
  templateUrl: './ordenes.component.html',
  styleUrls: ['./ordenes.component.scss']
})
export class OrdenesComponent implements OnInit {

  public authService = inject(AuthService);

  private ordenesService = inject(OrdenesService);

  private route = inject(ActivatedRoute);
  private produccionService = inject(ProduccionService);
  historialDeLote = false;
  detalle: Orden | null = null;
  movimientos: RegistroHistorial[] = []; historialCargando = false; historialError = '';
  busqueda = '';
  filtroEstado = '';
  filtroPrioridad = '';

  ordenes: Orden[] = [];
  cargando = false;
  errorCarga = '';

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
    this.route.queryParamMap.subscribe(params => { this.busqueda = params.get('q') || ''; });
    this.cargarOrdenes();
  }

  cargarOrdenes(): void {
    this.errorCarga = '';
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
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.ordenes = [];
        this.cargando = false;
      }
    });
  }

  get ordenesFiltradas() {
    return this.ordenes.filter(o =>
      (!this.busqueda || (o.numero + ' ' + o.modelo).toLowerCase().includes(this.busqueda.toLowerCase())) &&
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

  verHistorial(o: Orden): void {
    this.historialDeLote = false; this.detalle = o; this.movimientos = []; this.historialError = ''; this.historialCargando = true;
    this.ordenesService.historial(o.numero).subscribe({ next: datos => { this.movimientos = datos; this.historialCargando = false; },
      error: () => { this.historialError = 'No se pudo consultar el historial.'; this.historialCargando = false; } });
  }
  cambiarHistorial(): void {
    if (!this.detalle?.loteId) return;
    this.historialDeLote = !this.historialDeLote; this.historialCargando = true; this.historialError = ''; this.movimientos = [];
    const consulta = this.historialDeLote ? this.produccionService.historial(this.detalle.loteId) : this.ordenesService.historial(this.detalle.numero);
    consulta.subscribe({ next: d => { this.movimientos = d; this.historialCargando = false; }, error: () => { this.historialError = 'No se pudo consultar el historial.'; this.historialCargando = false; } });
  }
  eliminar(o: Orden): void {
    if (!o.id || this.cargando || !confirm('¿Eliminar este borrador? Esta acción no se puede deshacer.')) return;
    this.cargando = true;
    this.ordenesService.eliminarBorrador(o.id).subscribe({ next: () => this.cargarOrdenes(),
      error: e => { this.cargando = false; this.errorCarga = e?.error?.mensaje || 'No se pudo eliminar el borrador.'; } });
  }
  confirmar(orden: Orden): void {
    if (!orden.id || this.cargando) return;
    this.cargando = true;
    this.ordenesService.confirmarBorrador(orden.id).subscribe({
      next: () => this.cargarOrdenes(),
      error: (err) => { this.cargando = false; this.errorCarga = err?.error?.mensaje || 'No se pudo confirmar el borrador.'; }
    });
  }
}
