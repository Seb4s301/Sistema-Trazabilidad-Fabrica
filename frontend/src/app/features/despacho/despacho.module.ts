import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="despacho-page">
      <h2 class="h4 mb-4">Despacho</h2>
      <div class="card">
        <div class="card-body">
          <p class="text-muted">Módulo de despacho en desarrollo.</p>
        </div>
      </div>
    </div>
  `
})
export class DespachoComponent {}

const routes: Routes = [
  { path: '', component: DespachoComponent }
];

@NgModule({
  declarations: [DespachoComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class DespachoModule { }
