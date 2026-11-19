import { Component, OnChanges, inject, Output, EventEmitter, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProduccionService, Lote, AvanceLote } from '../../core/services/produccion.service';

@Component({
  selector: 'app-avance-lote-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="modal-backdrop" *ngIf="visible" (click)="cerrar()">
      <div class="modal-panel" (click)="$event.stopPropagation()">
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
            <option value="">Selecciona la siguiente etapa</option>
            <option *ngFor="let area of destinos" [value]="area">{{ area }}</option>
          </select>
        </div>
        <div class="form-group" *ngIf="lote?.area_actual === 'Calidad'">
          <label class="form-label">Resultado de Calidad</label>
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
    </div>
  `,
  styleUrls: ['../../shared/modal.scss']
})
export class AvanceLoteModalComponent implements OnChanges {

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

  get destinos(): string[] {
    switch (this.lote?.area_actual) {
      case 'Corte': return ['Ensamblaje'];
      case 'Ensamblaje': return ['Tapizado'];
      case 'Tapizado': return ['Calidad'];
      case 'Calidad': return ['Listo', 'Tapizado'];
      default: return [];
    }
  }

  ngOnChanges(): void {
    if (this.visible) this.avance = { lote_id: this.lote?.id || '', area_destino: '', resultado: '', notas: '' };
  }

  cerrar(): void {
    this.visible = false;
    this.cerrado.emit();
  }

  guardar(): void {
    if (!this.lote || this.guardando || !this.avance.area_destino) return;

    this.guardando = true;
    this.avance.lote_id = this.lote.id || this.lote.codigo;
    this.avance.area_origen = this.lote.area_actual;

    this.produccionService.registrarAvance(this.avance).subscribe({
      next: () => {
        this.guardando = false;
        this.avanceRegistrado.emit();
        this.cerrar();
      },
      error: () => {
        this.guardando = false;
        alert('No se pudo registrar el avance. Verifica la siguiente etapa y el resultado de Calidad.');
      }
    });
  }
}
