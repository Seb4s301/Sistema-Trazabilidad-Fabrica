import { Component, inject, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProduccionService, Lote, AvanceLote } from '../../core/services/produccion.service';

@Component({
  selector: 'app-avance-lote-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-backdrop" *ngIf="visible" (click)="cerrar()"></div>
    <div class="modal-panel" *ngIf="visible" (click)="$event.stopPropagation()">
      <div class="modal-header">
        <h5 class="modal-title">Registrar avance de lote</h5>
        <button class="modal-close" (click)="cerrar()">✕</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label class="form-label">Lote</label>
          <span class="form-value">{{ lote?.codigo }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Orden</label>
          <span class="form-value">{{ lote?.orden }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Modelo</label>
          <span class="form-value">{{ lote?.modelo }} · {{ lote?.plaza }}</span>
        </div>
        <div class="form-group">
          <label class="form-label">Área destino <span class="req">*</span></label>
          <select class="form-select" [(ngModel)]="avance.area_destino">
            <option value="Corte">Corte</option>
            <option value="Ensamblaje">Ensamblaje</option>
            <option value="Tapizado">Tapizado</option>
            <option value="Calidad">Calidad</option>
            <option value="Listo">Listo para despacho</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Resultado (solo Calidad)</label>
          <select class="form-select" [(ngModel)]="avance.resultado">
            <option value="">--</option>
            <option value="aprobado">Aprobado</option>
            <option value="rechazado">Rechazado</option>
          </select>
        </div>
        <div class="form-group">
          <label class="form-label">Notas</label>
          <textarea class="form-control" rows="3" [(ngModel)]="avance.notas"
                    placeholder="Observaciones del avance..."></textarea>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-outline-secondary" (click)="cerrar()">Cancelar</button>
        <button class="btn btn-ld-gold" (click)="guardar()" [disabled]="guardando || !avance.area_destino">
          Registrar avance
        </button>
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
    .form-select, .form-control {
      width: 100%; padding: 0.6rem 0.75rem; border: 1px solid #ccc;
      border-radius: 6px; font-size: 1rem; background: #fff;
    }
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
export class AvanceLoteModalComponent {

  private produccionService = inject(ProduccionService);

  @Input() visible = false;
  @Input() lote: Lote | null = null;
  @Output() avanceRegistrado = new EventEmitter<void>();
  @Output() cerrado = new EventEmitter<void>();

  avance: AvanceLote = {
    lote_id: '',
    area_destino: '',
    resultado: '',
    notas: ''
  };
  guardando = false;

  cerrar(): void {
    this.visible = false;
    this.cerrado.emit();
  }

  guardar(): void {
    if (!this.lote || this.guardando || !this.avance.area_destino) return;

    this.guardando = true;
    this.avance.lote_id = this.lote.id || '';
    this.avance.area_origen = this.lote.area_actual;

    this.produccionService.registrarAvance(this.avance).subscribe({
      next: () => {
        this.guardando = false;
        this.avanceRegistrado.emit();
        this.cerrar();
      },
      error: () => {
        this.guardando = false;
        alert('No se pudo registrar el avance. Intenta de nuevo.');
      }
    });
  }
}
