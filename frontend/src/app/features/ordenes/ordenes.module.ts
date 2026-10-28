import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { OrdenesComponent } from './ordenes.component';

const routes: Routes = [
  { path: '', component: OrdenesComponent }
];

@NgModule({
  declarations: [OrdenesComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)]
})
export class OrdenesModule {}
