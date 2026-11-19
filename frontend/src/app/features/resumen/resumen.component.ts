import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { DashboardService, Kpi, RitmoProduccion, DonutItem, OrdenSeguimiento } from '../../core/services/dashboard.service';

Chart.register(...registerables);

@Component({
  selector: 'app-resumen',
  templateUrl: './resumen.component.html',
  styleUrls: ['./resumen.component.scss']
})
export class ResumenComponent implements OnInit, AfterViewInit {

  private dashboardService = inject(DashboardService);

  @ViewChild('chartRitmo') chartRitmoRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('chartDonut') chartDonutRef!: ElementRef<HTMLCanvasElement>;

  actualizado = '';
  get unidadesSemana(): number { return this.ritmo.reduce((total, dia) => total + dia.valor, 0); }

  errorCarga = '';
  semana = 0;

  kpis: Kpi[] = [];
  ritmo: RitmoProduccion[] = [];
  maxRitmo = 280;
  donut: DonutItem[] = [];
  totalOrdenesActivas = 0;
  seguimiento: OrdenSeguimiento[] = [];

  private chartRitmoInstance: any = null;
  private chartDonutInstance: any = null;

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngAfterViewInit(): void {
    this.inicializarGraficos();
  }

  cargarDatos(): void {
    this.errorCarga = '';
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.actualizado = new Date().toLocaleTimeString('es-PE');
        this.kpis = data.kpis;
        this.ritmo = data.ritmo;
        this.maxRitmo = data.maxRitmo;
        this.donut = data.donut;
        this.totalOrdenesActivas = data.totalOrdenesActivas;
        this.seguimiento = data.seguimiento;
        this.semana = data.semana;
        this.actualizarGraficos();
      },
      error: () => {
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.kpis = []; this.ritmo = []; this.donut = []; this.seguimiento = []; this.totalOrdenesActivas = 0; this.actualizarGraficos();
      }
    });
  }

  porcentaje(valor: number): number {
    return this.totalOrdenesActivas ? Math.round((valor / this.totalOrdenesActivas) * 100) : 0;
  }

  private inicializarGraficos(): void {
    // Gráfico de barras - Ritmo de producción
    const ctxRitmo = this.chartRitmoRef?.nativeElement;
    if (ctxRitmo) {
      this.chartRitmoInstance = new Chart(ctxRitmo, {
        type: 'bar',
        data: {
          labels: this.ritmo.map(r => r.dia),
          datasets: [{
            label: 'Producción',
            data: this.ritmo.map(r => r.valor),
            backgroundColor: '#D4A935',
            borderRadius: 6,
            barThickness: 40
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false },
            tooltip: { mode: 'index', intersect: false }
          },
          scales: {
            y: {
              beginAtZero: true,
              max: this.maxRitmo,
              grid: { color: '#f0f0f0' },
              ticks: { stepSize: 50 }
            },
            x: {
              grid: { display: false }
            }
          }
        }
      });
    }

    // Gráfico de donut - Estado de órdenes
    const ctxDonut = this.chartDonutRef?.nativeElement;
    if (ctxDonut) {
      this.chartDonutInstance = new Chart(ctxDonut, {
        type: 'doughnut',
        data: {
          labels: this.donut.map(d => d.label),
          datasets: [{
            data: this.donut.map(d => d.valor),
            backgroundColor: this.donut.map(d => d.color),
            borderWidth: 0,
            hoverOffset: 10
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '70%',
          plugins: {
            legend: { display: false },
            tooltip: { enabled: true }
          }
        }
      });
    }
  }

  private actualizarGraficos(): void {
    if (this.chartRitmoInstance) {
      this.chartRitmoInstance.data.labels = this.ritmo.map(r => r.dia);
      this.chartRitmoInstance.data.datasets[0].data = this.ritmo.map(r => r.valor);
      (this.chartRitmoInstance.options.scales as any)['y'].max = this.maxRitmo;
      this.chartRitmoInstance.update();
    }
    if (this.chartDonutInstance) {
      this.chartDonutInstance.data.labels = this.donut.map(d => d.label);
      this.chartDonutInstance.data.datasets[0].data = this.donut.map(d => d.valor);
      this.chartDonutInstance.data.datasets[0].backgroundColor = this.donut.map(d => d.color);
      this.chartDonutInstance.update();
    }
  }

  ngOnDestroy(): void {
    this.chartRitmoInstance?.destroy();
    this.chartDonutInstance?.destroy();
  }
}
