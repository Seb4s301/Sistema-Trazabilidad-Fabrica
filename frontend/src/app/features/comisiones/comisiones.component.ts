import { Component, OnInit, inject } from '@angular/core';
import { ComisionesService, ComisionesData, ComisionFila, DistribucionItem } from '../../core/services/comisiones.service';

@Component({
  selector: 'app-comisiones',
  templateUrl: './comisiones.component.html',
  styleUrls: ['./comisiones.component.scss']
})
export class ComisionesComponent implements OnInit {

  private comisionesService = inject(ComisionesService);

  periodo = [new Date().getFullYear(), String(new Date().getMonth() + 1).padStart(2, '0')].join('-');
  accionEnCurso = false; pago: ComisionFila | null = null; referencia = ''; mensaje = '';
  kpis: ComisionesData['kpis'] = [];
  filas: ComisionFila[] = [];
  barrasModelo: DistribucionItem[] = [];
  barrasRelleno: DistribucionItem[] = [];
  cargando = false;
  errorCarga = '';

  ngOnInit(): void {
    this.cargarComisiones();
  }

  cargarComisiones(): void {
    this.errorCarga = '';
    this.cargando = true;
    this.comisionesService.getComisiones(this.periodo).subscribe({
      next: (data) => {
        this.aplicarDatos(data);
      },
      error: () => {
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.kpis = []; this.filas = []; this.barrasModelo = []; this.barrasRelleno = [];
        this.cargando = false;
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
      'Estimado': 'pill-slate',
      'Sin tarifa': 'pill-warning',
      'Pendiente': 'pill-warning',
      'Pagado': 'pill-success'
    };
  }

  montoTexto(valor: number): string {
    return this.comisionesService.montoTexto(valor);
  }

  exportarReporte(): void {
    this.comisionesService.exportarReporteExcel(this.periodo).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'Reporte_Comisiones_LatinDream.xlsx';
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => { this.errorCarga = 'No se pudo exportar el reporte.'; }
    });
  }

  liquidar(f: ComisionFila): void {
    if (this.accionEnCurso || !f.puedeLiquidar || !confirm('¿Liquidar la comisión de ' + f.tipo + ' por ' + this.montoTexto(f.monto) + '? El importe quedará fijado.')) return;
    this.accionEnCurso = true; this.errorCarga = ''; this.mensaje = '';
    this.comisionesService.liquidar(f.ordenId).subscribe({ next: () => { this.accionEnCurso = false; this.mensaje = 'Liquidación registrada.'; this.cargarComisiones(); }, error: e => { this.accionEnCurso = false; this.errorCarga = e?.error?.mensaje || 'No se pudo liquidar.'; } });
  }
  abrirPago(f: ComisionFila): void { this.pago = f; this.referencia = ''; this.errorCarga = ''; }
  registrarPago(): void {
    if (!this.pago?.liquidacionId || !this.referencia.trim() || this.accionEnCurso) return;
    this.accionEnCurso = true;
    this.comisionesService.pagar(this.pago.liquidacionId, this.referencia).subscribe({ next: () => { this.accionEnCurso = false; this.pago = null; this.mensaje = 'Pago registrado.'; this.cargarComisiones(); }, error: e => { this.accionEnCurso = false; this.errorCarga = e?.error?.mensaje || 'No se pudo registrar el pago.'; } });
  }
  porcentajeBarra(valor: number, total: number): number {
    if (!total) return 0;
    return Math.round((valor / total) * 100);
  }
}
