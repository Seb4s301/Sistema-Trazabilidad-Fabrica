import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { ProduccionComponent } from './produccion.component';
import { AvanceLoteModalComponent } from './avance-lote-modal.component';

const routes: Routes = [
  { path: '', component: ProduccionComponent }
];

@NgModule({
  declarations: [ProduccionComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes), AvanceLoteModalComponent]
})
export class ProduccionModule {}
