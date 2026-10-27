import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="nueva-orden-page">
      <h2 class="h4 mb-4">Nueva Orden de Producción</h2>
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="card p-4">
        <div class="row g-3">
          <div class="col-md-4">
            <label class="form-label">Producto</label>
            <input type="text" formControlName="producto" class="form-control" placeholder="Ej. Colchón Queen">
          </div>
          <div class="col-md-4">
            <label class="form-label">Cantidad</label>
            <input type="number" formControlName="cantidad" class="form-control" placeholder="0">
          </div>
          <div class="col-md-4">
            <label class="form-label">Fecha de entrega</label>
            <input type="date" formControlName="fechaEntrega" class="form-control">
          </div>
        </div>
        <button type="submit" class="btn btn-ld-gold mt-3" [disabled]="form.invalid">Crear Orden</button>
      </form>
    </div>
  `
})
export class NuevaOrdenComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder) {
    this.form = this.fb.group({
      producto: [''],
      cantidad: [0],
      fechaEntrega: ['']
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    console.log('Orden creada:', this.form.value);
  }
}

const routes: Routes = [
  { path: '', component: NuevaOrdenComponent }
];

@NgModule({
  declarations: [NuevaOrdenComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule.forChild(routes)
  ]
})
export class NuevaOrdenModule { }
