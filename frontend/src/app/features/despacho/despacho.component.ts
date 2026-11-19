import { AuthService } from '../../core/auth.service';
import { Component, OnInit, inject } from '@angular/core';
import { DespachosService, DespachosData, Despacho } from '../../core/services/despachos.service';

@Component({
  selector: 'app-despacho',
  templateUrl: './despacho.component.html',
  styleUrls: ['./despacho.component.scss']
})
export class DespachoComponent implements OnInit {

  public authService = inject(AuthService);

  private despachosService = inject(DespachosService);

  programarVisible = false;
  editando: Despacho | null = null;

  tituloFecha = '';
  resumen = '';
  detalleAbierto: Despacho | null = null;
  modalEstadoVisible = false;
  despachoSeleccionado: Despacho | null = null;

  filtroEstado = '';
  filtroCliente = '';
  filtroVehiculo = '';
  filtroFecha = '';

  kpis: DespachosData['kpis'] = [];
  despachos: Despacho[] = [];
  historial: DespachosData['historial'] = [];
  banner = '';
  cargando = false;
  errorCarga = '';

  ngOnInit(): void {
    this.cargarDespachos();
  }

  cargarDespachos(): void {
    this.errorCarga = '';
    this.cargando = true;
    this.despachosService.getDespachos().subscribe({
      next: (data) => {
        this.tituloFecha = data.tituloFecha;
        this.resumen = data.resumen;
        this.banner = data.banner;
        this.kpis = data.kpis;
        this.despachos = data.despachos;
        if (this.detalleAbierto) this.detalleAbierto = data.despachos.find(d => d.codigo === this.detalleAbierto?.codigo) || null;
        this.historial = data.historial;
        this.cargando = false;
      },
      error: () => {
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.kpis = []; this.despachos = []; this.historial = [];
        this.cargando = false;
      }
    });
  }

  get despachosFiltrados(): Despacho[] {
    return this.despachos.filter(d =>
      (!this.filtroEstado || d.estado === this.filtroEstado) &&
      (!this.filtroCliente || d.cliente === this.filtroCliente) &&
      (!this.filtroVehiculo || d.vehiculo === this.filtroVehiculo) &&
      (!this.filtroFecha || d.fecha_salida === this.filtroFecha)
    );
  }

  get clientes(): string[] {
    return Array.from(new Set(this.despachos.map(d => d.cliente)));
  }

  get vehiculos(): string[] {
    return Array.from(new Set(this.despachos.map(d => d.vehiculo)));
  }

  estadoLabel(e: string): string {
    if (e === 'programado') return 'Programado';
    if (e === 'transito') return 'En tránsito';
    if (e === 'cancelado') return 'Cancelado';
    return e === 'entregado' ? 'Entregado' : e;
  }

  claseEstado(e: string): string {
    if (e === 'programado') return 'pill-warning';
    if (e === 'transito') return 'pill-slate';
    return e === 'cancelado' ? 'pill-danger' : 'pill-success';
  }

  claseHistorial(estado: string): string {
    if (estado === 'Entregado') return 'pill-success';
    if (estado === 'Cancelado') return 'pill-danger';
    return 'pill-warning';
  }

  programar(d: Despacho | null = null): void { this.editando = d; this.programarVisible = true; }
  onGuardado(): void { this.programarVisible = false; this.editando = null; this.cargarDespachos(); }
  puedeEditar(d: Despacho): boolean { return this.authService.puedeGestionarDespacho() && (d.estado === 'programado' || (d.estado === 'transito' && !d.lotes.length)); }
  abrirDetalle(d: Despacho): void {
    this.detalleAbierto = this.detalleAbierto?.codigo === d.codigo ? null : d;
  }

  abrirModalEstado(d: Despacho): void {
    this.despachoSeleccionado = d;
    this.modalEstadoVisible = true;
  }

  cerrarModalEstado(): void {
    this.modalEstadoVisible = false;
    this.despachoSeleccionado = null;
  }

  onEstadoCambiado(): void {
    this.cerrarModalEstado();
    this.cargarDespachos(); // Recargar datos
  }


}
