import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
export interface RegistroHistorial { fecha: string; anterior?: string; estado?: string; origen?: string; destino?: string; usuario?: string; responsable?: string; resultado?: string; notas?: string; }
@Component({ selector: 'app-historial', standalone: true, imports: [CommonModule], styleUrls: ['./modal.scss'], template: `
<div class="modal-backdrop" role="dialog" aria-modal="true" (click)="cerrado.emit()">
  <section class="modal-panel" (click)="$event.stopPropagation()">
    <header class="modal-header"><h2 class="modal-title">{{ titulo }}</h2><button class="modal-close" (click)="cerrado.emit()" aria-label="Cerrar">×</button></header>
    <div class="modal-body">
      <button *ngIf="alternativa" class="btn btn-outline-dark mb-3" (click)="alternar.emit()">{{alternativa}}</button>
      <p *ngIf="observaciones"><strong>Instrucciones de fabricación:</strong> {{ observaciones }}</p>
      <p *ngIf="cargando" role="status">Cargando historial…</p><p *ngIf="error" role="alert">{{ error }}</p>
      <p *ngIf="!cargando && !error && !items.length">Sin movimientos registrados.</p>
      <article *ngFor="let r of items" class="border-bottom py-3">
        <div>{{ r.fecha | date:'dd/MM/yyyy HH:mm' }}</div><strong>{{ r.origen || r.anterior || 'Inicio' }} → {{ r.destino || r.estado }}</strong>
        <div>{{ r.responsable || r.usuario }} <span *ngIf="r.resultado">· {{ r.resultado }}</span></div><p>{{ r.notas }}</p>
      </article>
    </div>
  </section>
</div>` })
export class HistorialComponent {
  @Input() alternativa = ''; @Output() alternar = new EventEmitter<void>();
  @Input() titulo = ''; @Input() observaciones = ''; @Input() items: RegistroHistorial[] = [];
  @Input() cargando = false; @Input() error = ''; @Output() cerrado = new EventEmitter<void>();
}
