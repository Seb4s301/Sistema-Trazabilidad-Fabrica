import { Component, OnInit, inject } from '@angular/core';
import { ComisionesService, ComisionesData, ComisionFila, DistribucionItem } from '../../core/services/comisiones.service';

@Component({
  selector: 'app-comisiones',
  templateUrl: './comisiones.component.html',
  styleUrls: ['./comisiones.component.scss']
})
export class ComisionesComponent implements OnInit {

  private comisionesService = inject(ComisionesService);

  kpis: ComisionesData['kpis'] = [];
  filas: ComisionFila[] = [];
  barrasModelo: DistribucionItem[] = [];
  barrasRelleno: DistribucionItem[] = [];
  cargando = false;

  ngOnInit(): void {
    this.cargarComisiones();
  }

  cargarComisiones(): void {
    this.cargando = true;
    this.comisionesService.getComisiones().subscribe({
      next: (data) => {
        this.aplicarDatos(data);
      },
      error: () => {
        this.aplicarDatos(this.comisionesService.getDemoData());
      }
    });
  }

  private aplicarDatos(data: ComisionesData): void {
    this.kpis = data.kpis;
    this.filas = data.filas;
    this.barrasModelo = data.distribucionModelo;
    this.barrasRelleno = data.distribucionRelleno;
    this.cargando = false;
  }

  get totalMonto(): number {
    return this.filas.reduce((sum, f) => sum + f.monto, 0);
  }

  get totalModelo(): number {
    return this.barrasModelo.reduce((sum, b) => sum + b.valor, 0);
  }

  get totalRelleno(): number {
    return this.barrasRelleno.reduce((sum, b) => sum + b.valor, 0);
  }

  get claseEstado(): Record<string, string> {
    return {
      'Pagado': 'pill-success',
      'Pendiente': 'pill-warning',
      'En revisión': 'pill-slate'
    };
  }

  montoTexto(valor: number): string {
    return this.comisionesService.montoTexto(valor);
  }

  exportarReporte(): void {
    this.comisionesService.exportarReporteExcel().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'Reporte_Comisiones_LatinDream.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: (err) => console.error('Error al exportar reporte Excel', err)
    });
  }

  porcentajeBarra(valor: number, total: number): number {
    if (!total) return 0;
    return Math.round((valor / total) * 100);
  }
}
