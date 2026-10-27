import { Component, OnInit } from '@angular/core';

interface Kpi {
  etiqueta: string;
  valor: string;
  nota?: string;
  tipo?: string;
}

interface Despacho {
  codigo: string;
  cliente: string;
  destino: string;
  unidades: number;
  vehiculo: string;
  conductor: string;
  salida: string;
  entregaEstimada: string;
  estado: 'programado' | 'transito' | 'entregado';
}

@Component({
  selector: 'app-despacho',
  templateUrl: './despacho.component.html',
  styleUrls: ['./despacho.component.scss']
})
export class DespachoComponent implements OnInit {

  tituloFecha = '';
  resumen = '';
  detalleAbierto: Despacho | null = null;

  filtroEstado = '';
  filtroCliente = '';
  filtroVehiculo = '';
  filtroFecha = '';

  kpis: Kpi[] = [
    { etiqueta: 'Despachos del día', valor: '3' },
    { etiqueta: 'Unidades en ruta', valor: '120', nota: 'Vehículos asignados' },
    { etiqueta: 'En tránsito', valor: '1', nota: 'LD-201 · Ruta 2', tipo: 'transito' },
    { etiqueta: 'Entregados', valor: '1', nota: 'Completado', tipo: 'entregado' }
  ];

  despachos: Despacho[] = [
    { codigo: 'DS-1048', cliente: 'Distribuidora Norte', destino: 'Av. Brasil 1450, Cercado', unidades: 60, vehiculo: 'LD-201', conductor: 'Diego Ruiz', salida: '08:30', entregaEstimada: '11:00', estado: 'transito' },
    { codigo: 'DS-1049', cliente: 'Sueños & Descanso', destino: 'Jr. Camaná 410, Cercado', unidades: 75, vehiculo: 'LD-105', conductor: 'Carlos Medina', salida: '09:45', entregaEstimada: '12:30', estado: 'programado' },
    { codigo: 'DS-1047', cliente: 'Colchones Lima Sur', destino: 'Av. Huaylas 220, Surco', unidades: 45, vehiculo: 'LD-310', conductor: 'Rosa Paredes', salida: '06:15', entregaEstimada: '08:00', estado: 'entregado' }
  ];

  historial = [
    { despacho: 'DS-1046', cliente: 'Descanso Total', unidades: 50, estado: 'Entregado', salida: '16 oct. · 07:00', entrega: '16 oct. · 09:20', vehiculo: 'LD-201' },
    { despacho: 'DS-1045', cliente: 'Hoteles Callao', unidades: 90, estado: 'Entregado', salida: '15 oct. · 06:30', entrega: '15 oct. · 10:15', vehiculo: 'LD-105' },
    { despacho: 'DS-1044', cliente: 'Muebles Perú', unidades: 35, estado: 'Cancelado', salida: '15 oct. · 13:00', entrega: '—', vehiculo: 'LD-310' }
  ];

  banner = 'Diego Ruiz confirmó la preparación de carga del despacho DS-1048 a las 10:42.';

  ngOnInit(): void {
    const d = new Date();
    const meses = ['enero','febrero','marzo','abril','mayo','junio','julio','agosto','setiembre','octubre','noviembre','diciembre'];
    const texto = `${d.getDate()} ${meses[d.getMonth()]} ${d.getFullYear()}`;
    this.tituloFecha = `Despachos del ${texto}`;

    const programados = this.despachos.length;
    const unidades = this.despachos.reduce((a, x) => a + x.unidades, 0);
    const transito = this.despachos.filter(x => x.estado === 'transito').length;
    const entregados = this.despachos.filter(x => x.estado === 'entregado').length;
    this.resumen = `${programados} despachos programados · ${unidades} colchones · ${transito} en tránsito y ${entregados} entregado`;
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

  get clientesHistorial(): string[] {
    return Array.from(new Set(this.historial.map(h => h.cliente)));
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

  aplicarFiltros(): void {
    // Los filtros ya están enlazados con [(ngModel)]
  }
}
