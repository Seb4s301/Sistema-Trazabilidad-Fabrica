import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../api.config';
export interface ModeloGestion { id?: string; codigo: string; nombre: string; categoria: string; dimensiones: string; relleno: string; activo: boolean; usado?: boolean; }
export interface TarifaGestion { id?: string; modeloId: string; modelo?: string; precioBase: number; porcentaje: number; vigenteDesde: string; vigenteHasta: string | null; }
@Injectable({providedIn:'root'})
export class AdministracionService {
 constructor(private http: HttpClient) {}
 listar(): Observable<{ modelos: ModeloGestion[]; tarifas: TarifaGestion[] }> { return this.http.get<{ modelos: ModeloGestion[]; tarifas: TarifaGestion[] }>(`${API_BASE}/administracion`); }
 guardarModelo(m: ModeloGestion): Observable<ModeloGestion> { return m.id ? this.http.put<ModeloGestion>(`${API_BASE}/administracion/modelos/${m.id}`,m) : this.http.post<ModeloGestion>(`${API_BASE}/administracion/modelos`,m); }
 guardarTarifa(t: TarifaGestion): Observable<TarifaGestion> { return t.id ? this.http.put<TarifaGestion>(`${API_BASE}/administracion/tarifas/${t.id}`,t) : this.http.post<TarifaGestion>(`${API_BASE}/administracion/tarifas`,t); }
}
