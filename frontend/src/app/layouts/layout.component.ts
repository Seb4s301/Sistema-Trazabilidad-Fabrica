import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent implements OnInit {

  pageTitle = 'Resumen';
  breadcrumb = 'Planta principal';
  today = '';
  ultimaSincronizacion = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.updateFecha();
    this.ultimaSincronizacion = new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit', hour12: false });

    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd)
    ).subscribe(() => {
      this.pageTitle = this.tituloDesdeRuta(this.router.url);
    });
  }

  private updateFecha(): void {
    const d = new Date();
    const meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'set', 'oct', 'nov', 'dic'];
    this.today = `${d.getDate()} ${meses[d.getMonth()]}. ${d.getFullYear()}`;
  }

  private tituloDesdeRuta(url: string): string {
    if (url.startsWith('/resumen')) return 'Resumen';
    if (url.startsWith('/ordenes/nueva')) return 'Nueva orden';
    if (url.startsWith('/ordenes')) return 'Órdenes';
    if (url.startsWith('/produccion')) return 'Producción';
    if (url.startsWith('/comisiones')) return 'Comisiones';
    if (url.startsWith('/despacho')) return 'Despacho y Transporte';
    return 'Resumen';
  }
}
