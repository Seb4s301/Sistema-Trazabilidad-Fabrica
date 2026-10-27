import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layouts/layout.component';
import { LoginComponent } from './features/login/login.component';
import { AuthGuard } from './core/auth.guard';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'resumen', loadChildren: () => import('./features/resumen/resumen.module').then(m => m.ResumenModule) },
      { path: 'ordenes', loadChildren: () => import('./features/ordenes/ordenes.module').then(m => m.OrdenesModule) },
      { path: 'ordenes/nueva', loadChildren: () => import('./features/ordenes/nueva/nueva.module').then(m => m.NuevaOrdenModule) },
      { path: 'produccion', loadChildren: () => import('./features/produccion/produccion.module').then(m => m.ProduccionModule) },
      { path: 'comisiones', loadChildren: () => import('./features/comisiones/comisiones.module').then(m => m.ComisionesModule) },
      { path: 'despacho', loadChildren: () => import('./features/despacho/despacho.module').then(m => m.DespachoModule) },
      { path: '', redirectTo: '/resumen', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
