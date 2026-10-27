import { Component } from '@angular/core';

interface Modelo {
  id: number;
  codigo: string;
  nombre: string;
}

interface Dimension {
  id: string;
  label: string;
  medidas: string;
}

interface Relleno {
  id: string;
  titulo: string;
  descripcion: string;
}

@Component({
  selector: 'app-nueva-orden',
  templateUrl: './nueva.component.html',
  styleUrls: ['./nueva.component.scss']
})
export class NuevaOrdenComponent {

  paso = 1; // 1 Producto, 2 Planificación, 3 Confirmación

  // Sección 01
  modelos: Modelo[] = [
    { id: 1, codigo: 'CP-001', nombre: 'Comfort Plus' },
    { id: 2, codigo: 'DR-002', nombre: 'Dream Rest' },
    { id: 3, codigo: 'ES-003', nombre: 'Eco Sleep' },
    { id: 4, codigo: 'PR-004', nombre: 'Premium Rest' }
  ];

  modeloId: number | null = null;

  dimensiones: Dimension[] = [
    { id: '1.5', label: '1.5 Plazas', medidas: '1.5 Plazas: 120 × 190 cm · Altura: 26 cm' },
    { id: '2', label: '2 Plazas', medidas: '2 Plazas: 140 × 190 cm · Altura: 26 cm' },
    { id: 'queen', label: 'Queen', medidas: 'Queen: 160 × 200 cm · Altura: 30 cm' },
    { id: 'king', label: 'King', medidas: 'King: 180 × 200 cm · Altura: 30 cm' }
  ];
  dimensionId: string | null = null;

  rellenos: Relleno[] = [
    { id: 'resortes', titulo: 'Resortes Ensacados', descripcion: 'Núcleo de resortes individuales envueltos en bolsas de tela para soporte independiente.' },
    { id: 'memory', titulo: 'Memory Foam', descripcion: 'Espuma viscoelástica que se adapta al cuerpo y alivia la presión.' }
  ];
  rellenoId: string | null = null;

  cantidad: number | null = null;

  // Sección 02
  fechaEntrega: string = '';
  prioridad: string = '';
  observaciones: string = '';

  // Resumen
  numeroOrden = 'OP-2026-0421';
  registradoPor = 'María Rojas';
  turno = 'Turno A';
  guardando = false;

  get modeloSeleccionado(): Modelo | undefined {
    return this.modelos.find(m => m.id === this.modeloId);
  }

  get dimensionSeleccionada(): Dimension | undefined {
    return this.dimensiones.find(d => d.id === this.dimensionId);
  }

  get rellenoSeleccionado(): Relleno | undefined {
    return this.rellenos.find(r => r.id === this.rellenoId);
  }

  get esBorradorValido(): boolean {
    return this.modeloId !== null;
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
    this.guardando = true;
    setTimeout(() => this.guardando = false, 800);
  }

  crearOrden(): void {
    if (!this.esCompleto) return;
    this.guardando = true;
    setTimeout(() => this.guardando = false, 800);
  }

  cancelar(): void {
    history.back();
  }
}
