import { Component, OnChanges, inject, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DespachosService, Despacho } from '../../core/services/despachos.service';

@Component({
  selector: 'app-estado-despacho-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-backdrop" *ngIf="visible" (click)="cerrar()">
      <div class="modal-panel" (click)="$event.stopPropagation()">
        <div class="modal-header">
          <h5 class="modal-title">Cambiar estado de despacho</h5>
          <button class="modal-close" (click)="cerrar()">✕</button>
        </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Despacho</label>
          <span class="form-value">{{ despacho?.codigo }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Cliente</label>
          <span class="form-value">{{ despacho?.cliente }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Estado actual</label>
          <span class="form-value">{{ estadoLabel(despacho?.estado) }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Nuevo estado <span class="req">*</span></label>
          <select class="form-select" [(ngModel)]="nuevoEstado">
            <option *ngFor="let estado of estadosPermitidos" [value]="estado">{{ estadoLabel(estado) }}</option>
          </select>
        </div>
      </div>
        <div class="modal-footer">
          <button class="btn btn-outline-secondary" (click)="cerrar()">Cancelar</button>
          <button class="btn btn-ld-gold" (click)="guardar()" [disabled]="guardando">Guardar cambios</button>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['../../shared/modal.scss']
})
export class EstadoDespachoModalComponent implements OnChanges {

  private despachosService = inject(DespachosService);

  @Input() visible = false;
  @Input() despacho: Despacho | null = null;
  @Output() estadoCambiado = new EventEmitter<void>();
  @Output() cerrado = new EventEmitter<void>();

  nuevoEstado = 'programado';
  guardando = false;

  estadoLabel(e: string | undefined): string {
    if (e === 'programado') return 'Programado';
    if (e === 'transito') return 'En tránsito';
    if (e === 'entregado') return 'Entregado';
    return 'Cancelado';
  }

  get estadosPermitidos(): string[] {
    if (this.despacho?.estado === 'programado') return ['programado', 'transito', 'cancelado'];
    if (this.despacho?.estado === 'transito') return ['transito', 'entregado'];
    return [this.despacho?.estado || 'programado'];
  }
  ngOnChanges(): void {
    if (this.visible) this.nuevoEstado = this.despacho?.estado || 'programado';
  }

  cerrar(): void {
    this.visible = false;
    this.cerrado.emit();
  }

  guardar(): void {
    if (!this.despacho || this.guardando) return;
    this.guardando = true;
    this.despachosService.actualizarEstadoDespacho(this.despacho.codigo, this.nuevoEstado).subscribe({
      next: () => {
        this.guardando = false;
        this.estadoCambiado.emit();
        this.cerrar();
      },
      error: (err) => {
        this.guardando = false;
        alert(err?.error?.mensaje || 'No se pudo actualizar el estado. Intenta de nuevo.');
      }
    });
  }
}
