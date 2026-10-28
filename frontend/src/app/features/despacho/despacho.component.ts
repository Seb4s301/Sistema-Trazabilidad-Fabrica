import { Component, OnInit, inject } from '@angular/core';
import { DespachosService, DespachosData, Despacho } from '../../core/services/despachos.service';

@Component({
  selector: 'app-despacho',
  templateUrl: './despacho.component.html',
  styleUrls: ['./despacho.component.scss']
})
export class DespachoComponent implements OnInit {

  private despachosService = inject(DespachosService);

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

  ngOnInit(): void {
    this.cargarDespachos();
  }

  cargarDespachos(): void {
    this.cargando = true;
    this.despachosService.getDespachos().subscribe({
      next: (data) => {
        this.tituloFecha = data.tituloFecha;
        this.resumen = data.resumen;
        this.banner = data.banner;
        this.kpis = data.kpis;
        this.despachos = data.despachos;
        this.historial = data.historial;
        this.cargando = false;
      },
      error: () => {
        const demo = this.despachosService.getDemoData();
        this.tituloFecha = demo.tituloFecha;
        this.resumen = demo.resumen;
        this.banner = demo.banner;
        this.kpis = demo.kpis;
        this.despachos = demo.despachos;
        this.historial = demo.historial;
        this.cargando = false;
      }
    });
  }

  get despachosFiltrados(): Despacho[] {
    return this.despachos.filter(d =>
      (!this.filtroEstado || d.estado === this.filtroEstado) &&
      (!this.filtroCliente || d.cliente === this.filtroCliente) &&
      (!this.filtroVehiculo || d.vehiculo === this.filtroVehiculo)
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
    return 'Entregado';
  }

  claseEstado(e: string): string {
    if (e === 'programado') return 'pill-warning';
    if (e === 'transito') return 'pill-slate';
    return 'pill-success';
  }

  claseHistorial(estado: string): string {
    if (estado === 'Entregado') return 'pill-success';
    if (estado === 'Cancelado') return 'pill-danger';
    return 'pill-warning';
  }

  abrirDetalle(d: Despacho): void {
    this.detalleAbierto = this.detalleAbierto?.codigo === d.codigo ? null : d;
  }

  abrirModalEstado(d: Despacho): void {
    this.despachoSeleccionado = d;
    this.nuevoEstadoModal = d.estado;
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

  aplicarFiltros(): void {
    // Los filtros ya están enlazados con [(ngModel)]
  }

  nuevoEstadoModal = 'programado';
}
