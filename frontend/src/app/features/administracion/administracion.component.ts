import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdministracionService, ModeloGestion, TarifaGestion } from '../../core/services/administracion.service';
@Component({standalone:true, imports:[CommonModule,FormsModule], templateUrl:'./administracion.component.html'})
export class AdministracionComponent implements OnInit {
 private servicio=inject(AdministracionService);
 modelos: ModeloGestion[]=[]; tarifas: TarifaGestion[]=[];
 modelo=this.nuevoModelo(); tarifa=this.nuevaTarifa(); error=''; mensaje=''; cargando=false; guardando=false;
 ngOnInit(): void { this.cargar(); }
 nuevoModelo(): ModeloGestion { return { codigo:'',nombre:'',categoria:'',dimensiones:'',relleno:'',activo:true }; }
 nuevaTarifa(): TarifaGestion { return { modeloId:'', precioBase:0, porcentaje:0, vigenteDesde:'', vigenteHasta:null }; }
 cargar(): void { this.cargando=true; this.servicio.listar().subscribe({ next:d=>{this.modelos=d.modelos;this.tarifas=d.tarifas;this.cargando=false;},error:()=>{this.error='No se pudo cargar la administración.';this.cargando=false;} }); }
 editarModelo(m: ModeloGestion): void { this.modelo={...m}; this.error=''; this.mensaje=''; }
 editarTarifa(t: TarifaGestion): void { this.tarifa={...t}; this.error=''; this.mensaje=''; }
 guardarModelo(): void {
  if(this.guardando)return; this.guardando=true;this.error='';this.mensaje='';
  this.servicio.guardarModelo(this.modelo).subscribe({next:()=>{this.guardando=false;this.modelo=this.nuevoModelo();this.mensaje='Modelo guardado.';this.cargar();},error:e=>{this.guardando=false;this.error=e?.error?.mensaje||'No se pudo guardar el modelo.';}});
 }
 guardarTarifa(): void {
  if(this.guardando)return;this.guardando=true;this.error='';this.mensaje='';
  this.servicio.guardarTarifa({...this.tarifa,vigenteHasta:this.tarifa.vigenteHasta||null}).subscribe({next:()=>{this.guardando=false;this.tarifa=this.nuevaTarifa();this.mensaje='Tarifa guardada.';this.cargar();},error:e=>{this.guardando=false;this.error=e?.error?.mensaje||'No se pudo guardar la tarifa.';}});
 }
}
