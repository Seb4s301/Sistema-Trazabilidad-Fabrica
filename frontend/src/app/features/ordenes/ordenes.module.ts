import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { OrdenesComponent } from './ordenes.component';

@NgModule({
  declarations: [OrdenesComponent],
  imports: [CommonModule, FormsModule, RouterModule]
})
export class OrdenesModule {}
