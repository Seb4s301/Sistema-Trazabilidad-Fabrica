import { DashboardService } from '../../core/services/dashboard.service';
import { Router } from '@angular/router';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  busquedaAbierta = false;
  busqueda = ''; alertasAbiertas = false; cargandoAlertas = false; errorAlertas = '';
  alertas: { texto: string; ruta: string }[] = [];
  constructor(private router: Router, private dashboard: DashboardService) {}
  buscar(): void { if (this.busqueda.trim()) { this.router.navigate(['/ordenes'], { queryParams: { q: this.busqueda.trim() } }); this.busqueda = ''; this.busquedaAbierta = false; } }
  abrirAlertas(): void {
    this.alertasAbiertas = !this.alertasAbiertas; if (!this.alertasAbiertas) return;
    this.cargandoAlertas = true; this.errorAlertas = ''; this.alertas = [];
    this.dashboard.getDashboard().subscribe({ next: d => {
      const retrasos = d.kpis.find(k => k.etiqueta === 'Lotes con retraso');
      if (Number(retrasos?.valor) > 0) this.alertas.push({ texto: retrasos!.valor + ' lotes con retraso', ruta: '/produccion' });
      for (const o of d.seguimiento.filter(o => o.estadoClase === 'danger')) this.alertas.push({ texto: o.orden + ' · ' + o.estado, ruta: '/ordenes' });
      this.cargandoAlertas = false;
    }, error: () => { this.errorAlertas = 'No se pudieron consultar las alertas.'; this.cargandoAlertas = false; } });
  }
  @Input() breadcrumb = 'Planta principal';
  @Input() pageTitle = 'Resumen';
  @Input() today = '';
}
