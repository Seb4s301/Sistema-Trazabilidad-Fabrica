import { Component } from '@angular/core';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {
  menu = [
    { path: '/resumen', label: 'Resumen', icon: 'bi-speedometer2' },
    { path: '/ordenes', label: 'Órdenes', icon: 'bi-journal-text' },
    { path: '/ordenes/nueva', label: 'Nueva Orden', icon: 'bi-plus-circle' },
    { path: '/produccion', label: 'Producción', icon: 'bi-gear' },
    { path: '/comisiones', label: 'Comisiones', icon: 'bi-percent' },
    { path: '/despacho', label: 'Despacho', icon: 'bi-truck' }
  ];

  isActive(path: string): boolean {
    return location.pathname === path;
  }
}
