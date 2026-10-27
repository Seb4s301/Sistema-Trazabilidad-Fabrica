import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProduccionComponent } from './produccion.component';

@NgModule({
  declarations: [ProduccionComponent],
  imports: [CommonModule, FormsModule, RouterModule]
})
export class ProduccionModule {}
