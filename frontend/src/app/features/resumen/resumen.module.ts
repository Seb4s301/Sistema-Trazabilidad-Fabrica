import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="resumen-page">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="h4 mb-0">Resumen de Producción</h2>
      </div>
      <div class="row">
        <div class="col-md-3 mb-3">
          <div class="card text-white bg-primary">
            <div class="card-body">
              <h5 class="card-title">Órdenes activas</h5>
              <p class="card-text display-4 fw-bold">0</p>
            </div>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="card text-white bg-success">
            <div class="card-body">
              <h5 class="card-title">Completadas hoy</h5>
              <p class="card-text display-4 fw-bold">0</p>
            </div>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="card text-white bg-warning">
            <div class="card-body">
              <h5 class="card-title">Pendientes</h5>
              <p class="card-text display-4 fw-bold">0</p>
            </div>
          </div>
        </div>
        <div class="col-md-3 mb-3">
          <div class="card text-white bg-danger">
            <div class="card-body">
              <h5 class="card-title">En alerta</h5>
              <p class="card-text display-4 fw-bold">0</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ResumenComponent {}

const routes: Routes = [
  { path: '', component: ResumenComponent }
];

@NgModule({
  declarations: [ResumenComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ResumenModule { }
