import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ResumenComponent } from './resumen.component';

const routes: Routes = [
  { path: '', component: ResumenComponent }
];

@NgModule({
  declarations: [ResumenComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)]
})
export class ResumenModule {}
