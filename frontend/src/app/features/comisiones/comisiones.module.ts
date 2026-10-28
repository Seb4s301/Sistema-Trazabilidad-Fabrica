import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { ComisionesComponent } from './comisiones.component';

const routes: Routes = [
  { path: '', component: ComisionesComponent }
];

@NgModule({
  declarations: [ComisionesComponent],
  imports: [CommonModule, RouterModule.forChild(routes)]
})
export class ComisionesModule {}
