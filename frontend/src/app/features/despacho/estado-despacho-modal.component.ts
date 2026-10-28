import { Component, inject, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DespachosService, Despacho } from '../../core/services/despachos.service';

@Component({
  selector: 'app-estado-despacho-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-backdrop" *ngIf="visible" (click)="cerrar()"></div>
    <div class="modal-panel" *ngIf="visible" (click)="$event.stopPropagation()">
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
            <option value="programado">Programado</option>
            <option value="transito">En tránsito</option>
            <option value="entregado">Entregado</option>
            <option value="cancelado">Cancelado</option>
          </select>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline-secondary" (click)="cerrar()">Cancelar</button>
        <button class="btn btn-ld-gold" (click)="guardar()" [disabled]="guardando">Guardar cambios</button>
      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop {
      position: fixed; inset: 0; background: rgba(0,0,0,0.5);
      display: flex; align-items: center; justify-content: center;
      z-index: 1050; backdrop-filter: blur(2px);
    }
    .modal-panel {
      background: #fff; border-radius: 12px; width: 100%; max-width: 480px;
      max-height: 90vh; overflow: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      animation: modalIn 0.25s ease-out;
    }
    @keyframes modalIn {
      from { opacity: 0; transform: translateY(-20px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .modal-header {
      display: flex; justify-content: space-between; align-items: center;
      padding: 1.5rem 1.5rem 1rem; border-bottom: 1px solid #e8e8e8;
    }
    .modal-title { margin: 0; font-size: 1.125rem; font-weight: 700; color: #1a1a1a; }
    .modal-close {
      background: none; border: none; font-size: 1.5rem; cursor: pointer;
      color: #888; line-height: 1;
    }
    .modal-body { padding: 1.5rem; }
    .form-group { margin-bottom: 1rem; }
    .form-label { display: block; font-size: 0.875rem; font-weight: 600;
      color: #666; margin-bottom: 0.4rem; }
    .req { color: #d32f2f; }
    .form-value { display: block; font-size: 1rem; color: #333;
      padding: 0.5rem 0.75rem; background: #f8f9fa; border: 1px solid #e8e8e8;
      border-radius: 6px; }
    .form-select { width: 100%; padding: 0.6rem 0.75rem; border: 1px solid #ccc;
      border-radius: 6px; font-size: 1rem; background: #fff; }
    .modal-footer {
      display: flex; justify-content: flex-end; gap: 0.75rem;
      padding: 1rem 1.5rem; border-top: 1px solid #e8e8e8;
    }
    .btn {
      padding: 0.6rem 1.25rem; border-radius: 6px; font-weight: 600;
      font-size: 0.95rem; cursor: pointer; border: 1px solid transparent;
    }
    .btn-outline-secondary {
      background: transparent; border-color: #ccc; color: #555;
    }
    .btn-ld-gold {
      background: #D4A935; color: #fff; border: none;
    }
    .btn:disabled { opacity: 0.6; cursor: not-allowed; }
  `]
})
export class EstadoDespachoModalComponent {

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
      error: () => {
        this.guardando = false;
        alert('No se pudo actualizar el estado. Intenta de nuevo.');
      }
    });
  }
}
