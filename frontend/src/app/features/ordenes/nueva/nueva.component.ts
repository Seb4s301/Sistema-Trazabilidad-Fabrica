import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OrdenesService, Modelo, Dimension, Relleno, Catalogos, OrdenCreateRequest } from '../../../core/services/ordenes.service';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-nueva-orden',
  templateUrl: './nueva.component.html',
  styleUrls: ['./nueva.component.scss']
})
export class NuevaOrdenComponent {

  private ordenesService = inject(OrdenesService);
  private authService = inject(AuthService);
  private router = inject(Router);

  paso = 1; // 1 Producto, 2 Planificación, 3 Confirmación

  // Catálogos del backend
  modelos: Modelo[] = [];
  dimensiones: Dimension[] = [];
  rellenos: Relleno[] = [];

  modeloId: string | null = null;
  dimensionId: string | null = null;
  rellenoId: string | null = null;

  cantidad: number | null = null;

  // Sección 02
  fechaEntrega: string = '';
  prioridad: string = '';
  observaciones: string = '';

  // Resumen
  numeroOrden = 'OP-2026-0421';
  registradoPor = 'Usuario';
  turno = 'Turno A';
  guardando = false;

  constructor() {
    this.cargarCatalogos();
    const usuario = this.authService.getUsuario();
    if (usuario) {
      this.registradoPor = usuario.nombre;
      this.turno = usuario.turno || 'Turno A';
    }
  }

  cargarCatalogos(): void {
    this.ordenesService.getCatalogos().subscribe({
      next: (data: Catalogos) => {
        this.modelos = data.modelos;
        this.dimensiones = data.dimensiones;
        this.rellenos = data.rellenos;
      },
      error: () => {
        // Fallback a datos demo
        const demo = this.ordenesService.getDemoCatalogos();
        this.modelos = demo.modelos;
        this.dimensiones = demo.dimensiones;
        this.rellenos = demo.rellenos;
      }
    });
  }

  get modeloSeleccionado(): Modelo | undefined {
    return this.modelos.find(m => m.id === this.modeloId);
  }

  get dimensionSeleccionada(): Dimension | undefined {
    return this.dimensiones.find(d => d.id === this.dimensionId);
  }

  get rellenoSeleccionado(): Relleno | undefined {
    return this.rellenos.find(r => r.id === this.rellenoId);
  }

  /** Campos que pertenecen al paso 1 (Producto). */
  get esPaso1Completo(): boolean {
    return this.modeloId !== null &&
           this.dimensionId !== null &&
           this.rellenoId !== null &&
           this.cantidad !== null && this.cantidad >= 1;
  }

  get esBorradorValido(): boolean {
    // El backend exige todos los datos para poder persistir el borrador.
    return this.esCompleto;
  }

  get esCompleto(): boolean {
    return this.modeloId !== null &&
           this.dimensionId !== null &&
           this.rellenoId !== null &&
           this.cantidad !== null && this.cantidad >= 1 &&
           !!this.fechaEntrega &&
           !!this.prioridad;
  }

  get diasHabilesDisponibles(): number {
    if (!this.fechaEntrega) return 0;
    const hoy = new Date();
    const entrega = new Date(this.fechaEntrega);
    if (entrega <= hoy) return 0;
    let dias = 0;
    const actual = new Date(hoy);
    while (actual < entrega) {
      actual.setDate(actual.getDate() + 1);
      const dow = actual.getDay();
      if (dow !== 0 && dow !== 6) dias++;
    }
    return dias;
  }

  get fechaPasada(): boolean {
    if (!this.fechaEntrega) return false;
    return new Date(this.fechaEntrega) < new Date(new Date().toDateString());
  }

  get insuficiente(): boolean {
    return this.fechaEntrega !== '' && (this.fechaPasada || this.diasHabilesDisponibles < 3);
  }

  siguiente(): void {
    if (this.paso < 3) this.paso++;
  }

  anterior(): void {
    if (this.paso > 1) this.paso--;
  }

  guardarBorrador(): void {
    if (this.guardando || !this.esBorradorValido) return;

    this.guardando = true;
    this.ordenesService.guardarBorrador(this.construirRequest()).subscribe({
      next: (orden) => {
        this.guardando = false;
        this.numeroOrden = orden.numero;
        alert('Borrador guardado: ' + orden.numero);
      },
      error: (err) => {
        this.guardando = false;
        alert(this.mensajeError(err, 'No se pudo guardar el borrador.'));
      }
    });
  }

  crearOrden(): void {
    if (!this.esCompleto) return;

    this.guardando = true;

    this.ordenesService.crearOrden(this.construirRequest()).subscribe({
      next: () => {
        this.guardando = false;
        this.router.navigate(['/ordenes']);
      },
      error: (err) => {
        this.guardando = false;
        alert(this.mensajeError(err, 'No se pudo crear la orden.'));
      }
    });
  }

  private construirRequest(): OrdenCreateRequest {
    return {
      modelo_id: this.modeloId!,
      dimension_id: this.dimensionId!,
      relleno_id: this.rellenoId!,
      cantidad: this.cantidad!,
      prioridad: this.prioridad,
      fecha_entrega: this.fechaEntrega,
      observaciones: this.observaciones,
      turno: this.turno
    };
  }

  private mensajeError(err: any, porDefecto: string): string {
    return err?.error?.mensaje || porDefecto;
  }

  cancelar(): void {
    history.back();
  }
}
