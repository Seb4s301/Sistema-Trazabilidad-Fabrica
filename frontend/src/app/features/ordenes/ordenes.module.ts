import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="ordenes-page">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="h4 mb-0">Órdenes de Producción</h2>
        <button class="btn btn-ld-gold" routerLink="nueva">Nueva Orden</button>
      </div>
      <div class="card">
        <div class="card-body">
          <p class="text-muted">No hay órdenes registradas aún.</p>
        </div>
      </div>
    </div>
  `
})
export class OrdenesComponent {}

const routes: Routes = [
  { path: '', component: OrdenesComponent },
  { path: 'nueva', loadChildren: () => import('./nueva/nueva.module').then(m => m.NuevaOrdenModule) }
];

@NgModule({
  declarations: [OrdenesComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class OrdenesModule { }
