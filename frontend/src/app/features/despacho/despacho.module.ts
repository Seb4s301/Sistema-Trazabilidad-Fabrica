import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DespachoComponent } from './despacho.component';
import { EstadoDespachoModalComponent } from './estado-despacho-modal.component';

const routes: Routes = [
  { path: '', component: DespachoComponent }
];

@NgModule({
  declarations: [DespachoComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes), EstadoDespachoModalComponent]
})
export class DespachoModule {}
