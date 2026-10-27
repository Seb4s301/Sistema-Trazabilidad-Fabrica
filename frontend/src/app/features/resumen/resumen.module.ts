import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ResumenComponent } from './resumen.component';

@NgModule({
  declarations: [ResumenComponent],
  imports: [CommonModule, FormsModule, RouterModule]
})
export class ResumenModule {}
