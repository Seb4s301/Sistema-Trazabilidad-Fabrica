import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  standalone: false,
  template: `
    <div class="comisiones-page">
      <h2 class="h4 mb-4">Comisiones</h2>
      <div class="card">
        <div class="card-body">
          <p class="text-muted">Módulo de comisiones en desarrollo.</p>
        </div>
      </div>
    </div>
  `
})
export class ComisionesComponent {}

const routes: Routes = [
  { path: '', component: ComisionesComponent }
];

@NgModule({
  declarations: [ComisionesComponent],
  imports: [
    CommonModule,
    RouterModule.forChild(routes)
  ]
})
export class ComisionesModule { }
