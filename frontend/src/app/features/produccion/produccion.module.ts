import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="produccion-page">
      <h2 class="h4 mb-4">Panel de Producción</h2>
      <div class="card">
        <div class="card-body">
          <p class="text-muted">Módulo de producción en desarrollo.</p>
        </div>
      </div>
    </div>
  `
})
export class ProduccionComponent {}

const routes: Routes = [
  { path: '', component: ProduccionComponent }
];

@NgModule({
  declarations: [ProduccionComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ProduccionModule { }
