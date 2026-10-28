import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { NuevaOrdenComponent } from './nueva.component';

const routes: Routes = [
  { path: '', component: NuevaOrdenComponent }
];

@NgModule({
  declarations: [NuevaOrdenComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)]
})
export class NuevaOrdenModule {}
