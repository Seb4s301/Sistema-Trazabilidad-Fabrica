import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NuevaOrdenComponent } from './nueva.component';

@NgModule({
  declarations: [NuevaOrdenComponent],
  imports: [CommonModule, FormsModule]
})
export class NuevaOrdenModule {}
