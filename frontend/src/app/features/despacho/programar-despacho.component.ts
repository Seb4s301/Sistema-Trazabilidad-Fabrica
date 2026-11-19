import { Component, EventEmitter, Input, OnChanges, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Despacho, DespachosService, DespachoRequest, LoteDisponible } from '../../core/services/despachos.service';
type Seleccion = LoteDisponible & { seleccionado: boolean; completo: boolean; cantidad: number };
@Component({ selector: 'app-programar-despacho', standalone: true, imports: [CommonModule, FormsModule], styleUrls: ['../../shared/modal.scss'], template: `
<div class="modal-backdrop" role="dialog" aria-modal="true" (click)="cerrar()">
 <section class="modal-panel" style="max-width:850px" (click)="$event.stopPropagation()">
 <header class="modal-header"><h2 class="modal-title">{{ despacho ? 'Editar despacho' : 'Programar despacho' }}</h2><button class="modal-close" (click)="cerrar()">×</button></header>
 <form class="modal-body" (ngSubmit)="guardar()" #form="ngForm">
 <div *ngIf="error" class="alert alert-danger" role="alert">{{ error }}</div>
 <div class="row g-3">
  <label class="col-md-6">Cliente<input class="form-control" name="cliente" [(ngModel)]="datos.cliente" required maxlength="150"></label>
  <label class="col-md-6">Destino<input class="form-control" name="destino" [(ngModel)]="datos.destino" required maxlength="200"></label>
  <label class="col-md-6">Vehículo / placa<input class="form-control" name="vehiculo" [(ngModel)]="datos.vehiculo" required maxlength="50"></label>
  <label class="col-md-6">Conductor<input class="form-control" name="conductor" [(ngModel)]="datos.conductor" required maxlength="100"></label>
  <label class="col-md-6">Fecha de salida<input class="form-control" name="salida" type="date" [(ngModel)]="datos.fechaSalida" required></label>
  <label class="col-md-6">Entrega estimada<input class="form-control" name="entrega" type="date" [(ngModel)]="datos.fechaEntregaEstimada" [min]="datos.fechaSalida" required></label>
 </div>
 <h3 class="h6 mt-4">Lotes aprobados</h3><p>Selecciona cada lote y elige el lote completo o una cantidad parcial. Las unidades quedan reservadas al guardar.</p>
 <p *ngIf="cargando">Consultando disponibilidad…</p><p *ngIf="!cargando && !lotes.length">No hay lotes disponibles. Completa su aprobación de Calidad en Producción.</p>
 <div class="border rounded p-3 mb-2" *ngFor="let l of lotes; let i = index">
  <label><input type="checkbox" [name]="'lote'+i" [(ngModel)]="l.seleccionado"> <strong>{{ l.codigo }}</strong> · {{ l.modelo }} · {{ l.disponibles }} disponibles de {{ l.unidades }}</label>
  <div class="d-flex gap-2 mt-2" *ngIf="l.seleccionado">
   <select class="form-select" [name]="'modo'+i" [(ngModel)]="l.completo" (ngModelChange)="cambiarModo(l)">
    <option [ngValue]="false">Cantidad parcial</option><option [ngValue]="true" [disabled]="l.disponibles < l.unidades">Lote completo</option>
   </select>
   <input class="form-control" style="max-width:140px" type="number" [name]="'cantidad'+i" [(ngModel)]="l.cantidad" [readonly]="l.completo" min="1" [max]="l.disponibles" step="1" aria-label="Cantidad del lote">
  </div>
 </div>
 <footer class="modal-footer"><span>{{ total }} unidades seleccionadas</span><button class="btn btn-outline-dark" type="button" (click)="cerrar()" [disabled]="guardando">Cancelar</button><button class="btn btn-ld-gold" type="submit" [disabled]="guardando || cargando || !form.valid || !valido">{{ guardando ? 'Guardando…' : 'Guardar despacho' }}</button></footer>
 </form></section>
</div>` })
export class ProgramarDespachoComponent implements OnChanges {
 private servicio = inject(DespachosService);
 @Input() despacho: Despacho | null = null;
 @Output() cerrado = new EventEmitter<void>(); @Output() guardado = new EventEmitter<void>();
 datos: DespachoRequest = this.vacios(); lotes: Seleccion[] = []; cargando = false; guardando = false; error = ''; private huella = '';
 private vacios(): DespachoRequest { const hoy = new Date(); const fecha = [hoy.getFullYear(), String(hoy.getMonth()+1).padStart(2,'0'), String(hoy.getDate()).padStart(2,'0')].join('-'); return { solicitudId: crypto.randomUUID(), cliente: '', destino: '', vehiculo: '', conductor: '', fechaSalida: fecha, fechaEntregaEstimada: fecha, lotes: [] }; }
 ngOnChanges(): void {
  this.datos = this.vacios(); this.huella = ''; this.error = ''; this.cargando = true;
  if (this.despacho) Object.assign(this.datos, { cliente: this.despacho.cliente, destino: this.despacho.destino, vehiculo: this.despacho.vehiculo, conductor: this.despacho.conductor, fechaSalida: this.despacho.fecha_salida, fechaEntregaEstimada: this.despacho.fecha_entrega_estimada });
  this.servicio.disponibles().subscribe({ next: disponibles => {
   const mapa = new Map(disponibles.map(l => [l.id, { ...l, seleccionado: false, completo: false, cantidad: 1 }]));
   for (const a of this.despacho?.lotes || []) {
    const l = mapa.get(a.loteId) || { id: a.loteId, codigo: a.codigo, modelo: a.modelo, orden: '', unidades: a.unidades, disponibles: 0, seleccionado: false, completo: false, cantidad: 1 };
    l.disponibles += a.cantidad; l.seleccionado = true; l.cantidad = a.cantidad; l.completo = a.cantidad === l.unidades; mapa.set(l.id, l);
   }
   this.lotes = [...mapa.values()]; this.cargando = false;
  }, error: () => { this.error = 'No se pudo consultar la disponibilidad.'; this.cargando = false; } });
 }
 cambiarModo(l: Seleccion): void { l.cantidad = l.completo ? l.unidades : Math.min(l.cantidad, l.disponibles); }
 get total(): number { return this.lotes.filter(l => l.seleccionado).reduce((s,l) => s + (Number(l.cantidad) || 0), 0); }
 get valido(): boolean { const lista = this.lotes.filter(l => l.seleccionado); return lista.length > 0 && lista.every(l => Number.isInteger(l.cantidad) && l.cantidad > 0 && l.cantidad <= l.disponibles && (!l.completo || l.cantidad === l.unidades)); }
 cerrar(): void { if (!this.guardando) this.cerrado.emit(); }
 guardar(): void {
  if (this.guardando || !this.valido) return;
  const datos = { ...this.datos, lotes: this.lotes.filter(l => l.seleccionado).map(l => ({ loteId: l.id, cantidad: l.cantidad })) };
  const huella = JSON.stringify({ ...datos, solicitudId: undefined });
  if (this.huella && this.huella !== huella) datos.solicitudId = crypto.randomUUID();
  this.datos.solicitudId = datos.solicitudId; this.huella = huella; this.guardando = true; this.error = '';
  const peticion = this.despacho ? this.servicio.editar(this.despacho.codigo, datos) : this.servicio.crear(datos);
  peticion.subscribe({ next: () => { this.guardando = false; this.guardado.emit(); }, error: e => { this.guardando = false; this.error = e?.error?.mensaje || 'No se pudo guardar. Puedes reintentar sin duplicar el despacho.'; } });
 }
}
