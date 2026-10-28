import { Component, OnInit, AfterViewInit, ElementRef, ViewChild, inject } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { DashboardService, DashboardData, Kpi, RitmoProduccion, DonutItem, OrdenSeguimiento } from '../../core/services/dashboard.service';

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

  periodoSeleccionado = 'semana42';
  rangoActualizado = 'Datos actualizados a las 10:42';
  vistaGrafico: 'dia' | 'semana' = 'semana';

  semana = 42;

  kpis: Kpi[] = [];
  ritmo: RitmoProduccion[] = [];
  maxRitmo = 280;
  donut: DonutItem[] = [];
  totalOrdenesActivas = 0;
  seguimiento: OrdenSeguimiento[] = [];

  donutStyle: string = '';
  private chartRitmoInstance: any = null;
  private chartDonutInstance: any = null;

  ngOnInit(): void {
    this.cargarDatos();
  }

  ngAfterViewInit(): void {
    this.inicializarGraficos();
  }

  cargarDatos(): void {
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.kpis = data.kpis;
        this.ritmo = data.ritmo;
        this.maxRitmo = data.maxRitmo;
        this.donut = data.donut;
        this.totalOrdenesActivas = data.totalOrdenesActivas;
        this.seguimiento = data.seguimiento;
        this.semana = data.semana;
        this.calcularDonut();
        this.actualizarGraficos();
      },
      error: () => {
        // Fallback a datos demo si el backend no responde
        const demo = this.dashboardService.getDemoData();
        this.kpis = demo.kpis;
        this.ritmo = demo.ritmo;
        this.maxRitmo = demo.maxRitmo;
        this.donut = demo.donut;
        this.totalOrdenesActivas = demo.totalOrdenesActivas;
        this.seguimiento = demo.seguimiento;
        this.semana = demo.semana;
        this.calcularDonut();
        this.actualizarGraficos();
      }
    });
  }

  calcularDonut(): void {
    const total = this.donut.reduce((acc, d) => acc + d.valor, 0);
    let acumulado = 0;
    const segmentos = this.donut.map(d => {
      const inicio = (acumulado / total) * 360;
      acumulado += d.valor;
      const fin = (acumulado / total) * 360;
      return `${d.color} ${inicio}deg ${fin}deg`;
    });
    this.donutStyle = `conic-gradient(${segmentos.join(', ')})`;
  }

  porcentaje(valor: number): number {
    return Math.round((valor / this.totalOrdenesActivas) * 100);
  }

  alturaBarra(valor: number): number {
    return (valor / this.maxRitmo) * 100;
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
          }, {
            label: 'Meta',
            data: this.ritmo.map(r => r.meta),
            type: 'line',
            borderColor: '#6B7A8A',
            borderWidth: 2,
            pointRadius: 0,
            fill: false,
            tension: 0
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
      this.chartRitmoInstance.data.datasets[1].data = this.ritmo.map(r => r.meta);
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

  onPeriodoCambio(): void {
    this.cargarDatos();
  }

  cambiarVista(vista: 'dia' | 'semana'): void {
    this.vistaGrafico = vista;
    this.actualizarGraficos();
  }

  ngOnDestroy(): void {
    this.chartRitmoInstance?.destroy();
    this.chartDonutInstance?.destroy();
  }
}
