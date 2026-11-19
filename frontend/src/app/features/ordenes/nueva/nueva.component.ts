import { Component, inject } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { OrdenesService, Modelo, Catalogos, OrdenCreateRequest } from '../../../core/services/ordenes.service';
import { switchMap } from 'rxjs';
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
  private route = inject(ActivatedRoute);
  borradorId: string | null = null;
  cargandoBorrador = false;
  private solicitudId = crypto.randomUUID();
  private ultimaSolicitud = '';

  paso = 1; // 1 Producto, 2 Planificación, 3 Confirmación

  // Catálogos del backend
  modelos: Modelo[] = [];

  modeloId: string | null = null;

  cantidad: number | null = null;

  // Sección 02
  fechaEntrega: string = '';
  prioridad: string = '';
  observaciones: string = '';

  // Resumen
  numeroOrden = 'Se asignará al confirmar';
  registradoPor = 'Usuario';
  turno = 'Turno A';
  guardando = false;

  errorCarga = '';

  constructor() {
    this.cargarCatalogos();
    const numero = this.route.snapshot?.paramMap?.get('numero') || this.route.parent?.snapshot.paramMap.get('numero');
    if (numero) {
      this.cargandoBorrador = true;
      this.ordenesService.getOrden(numero).subscribe({
        next: o => {
          if (o.estado !== 'borrador') { this.errorCarga = 'Solo se pueden editar borradores.'; this.cargandoBorrador = false; return; }
          this.borradorId = o.id || null; this.numeroOrden = o.numero; this.modeloId = o.modeloId || null;
          this.cantidad = o.cantidad; this.fechaEntrega = o.fechaEntrega || ''; this.prioridad = o.prioridad || ''; this.observaciones = o.observaciones || '';
          this.cargandoBorrador = false;
        },
        error: () => { this.errorCarga = 'No se pudo recuperar el borrador.'; this.cargandoBorrador = false; }
      });
    }
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
      },
      error: () => {
        this.errorCarga = 'No se pudieron cargar los datos. Comprueba la conexión y tus permisos.';
        this.modelos = [];
      }
    });
  }

  get modeloSeleccionado(): Modelo | undefined {
    return this.modelos.find(m => m.id === this.modeloId);
  }

  /** Campos que pertenecen al paso 1 (Producto). */
  get esPaso1Completo(): boolean {
    return this.modeloId !== null &&
           this.cantidad !== null && Number.isInteger(this.cantidad) && this.cantidad >= 1;
  }

  get esBorradorValido(): boolean {
    return !this.cargandoBorrador && !this.guardando && (this.modeloId !== null || this.cantidad !== null || !!this.fechaEntrega || !!this.prioridad || !!this.observaciones)
      && (this.cantidad === null || (Number.isInteger(this.cantidad) && this.cantidad >= 1));
  }

  get esCompleto(): boolean {
    return this.modeloId !== null &&
           this.cantidad !== null && Number.isInteger(this.cantidad) && this.cantidad >= 1 &&
           !!this.fechaEntrega && !this.fechaPasada &&
           !!this.prioridad;
  }

  get diasHabilesDisponibles(): number {
    if (!this.fechaEntrega) return 0;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const entrega = new Date(this.fechaEntrega + 'T00:00:00');
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
    return new Date(this.fechaEntrega + 'T00:00:00') < new Date(new Date().toDateString());
  }

  get insuficiente(): boolean {
    return this.fechaEntrega !== '' && (this.fechaPasada || this.diasHabilesDisponibles < 3);
  }

  siguiente(): void {
    if (this.guardando || this.cargandoBorrador) return;
    if ((this.paso === 1 && this.esPaso1Completo) || (this.paso === 2 && this.esCompleto)) this.paso++;
  }

  anterior(): void {
    if (this.paso > 1) this.paso--;
  }

  guardarBorrador(): void {
    if (this.guardando || !this.esBorradorValido) return;

    this.guardando = true;
    const datos = this.construirRequest();
    const guardar = this.borradorId ? this.ordenesService.editarBorrador(this.borradorId, datos) : this.ordenesService.guardarBorrador(datos);
    guardar.subscribe({
      next: (orden) => {
        this.guardando = false;
        this.numeroOrden = orden.numero;
        this.router.navigate(['/ordenes']);
      },
      error: (err) => {
        this.guardando = false;
        alert(this.mensajeError(err, 'No se pudo guardar el borrador.'));
      }
    });
  }

  crearOrden(): void {
    if (this.guardando || !this.esCompleto) return;

    this.guardando = true;

    const datos = this.construirRequest();
    const contenido = JSON.stringify({ ...datos, solicitudId: undefined });
    if (contenido !== this.ultimaSolicitud) { this.solicitudId = crypto.randomUUID(); this.ultimaSolicitud = contenido; }
    datos.solicitudId = this.solicitudId;
    const crear = this.borradorId
      ? this.ordenesService.editarBorrador(this.borradorId, datos).pipe(switchMap(o => this.ordenesService.confirmarBorrador(o.id!)))
      : this.ordenesService.crearOrden(datos);
    crear.subscribe({
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
      modelo_id: this.modeloId,
      cantidad: this.cantidad,
      prioridad: this.prioridad || null,
      fecha_entrega: this.fechaEntrega || null,
      observaciones: this.observaciones,
    };
  }

  private mensajeError(err: any, porDefecto: string): string {
    return err?.error?.mensaje || porDefecto;
  }

  cancelar(): void {
    history.back();
  }
}
