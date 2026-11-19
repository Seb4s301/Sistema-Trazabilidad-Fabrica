import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface Usuario {
  id?: string;
  nombre: string;
  correo: string;
  rol: string;
  turno?: string;
  activo?: boolean;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  correo: string;
  rol: string;
  nombre: string;
}

export interface AuthRequest {
  correo: string;
  password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiBaseUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<Usuario | null>(this.getUsuario());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.tieneToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(request: AuthRequest, recordar = false): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        for (const storage of [localStorage, sessionStorage]) { storage.removeItem('token'); storage.removeItem('usuario'); }
        const storage = recordar ? localStorage : sessionStorage;
        storage.setItem('token', response.token);
        storage.setItem('usuario', JSON.stringify({
          nombre: response.nombre,
          correo: response.correo,
          rol: response.rol,
          turno: this.turnoDelToken(response.token)
        }));
        this.currentUserSubject.next({
          nombre: response.nombre,
          correo: response.correo,
          rol: response.rol,
          turno: this.turnoDelToken(response.token)
        });
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  logout(): void {
    for (const storage of [localStorage, sessionStorage]) { storage.removeItem('token'); storage.removeItem('usuario'); }
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return sessionStorage.getItem('token') || localStorage.getItem('token');
  }

  getUsuario(): Usuario | null {
    const u = sessionStorage.getItem('usuario') || localStorage.getItem('usuario');
    try { return u ? JSON.parse(u) : null; } catch { return null; }
  }

  tieneToken(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).exp * 1000 > Date.now();
    } catch { return false; }
  }

  private turnoDelToken(token: string): string | undefined {
    try { return JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')), c => c.charCodeAt(0)))).turno; }
    catch { return undefined; }
  }

  puedeCrearOrden(): boolean {
    return ['gerente', 'supervisora', 'vendedora'].includes(this.getUsuario()?.rol ?? '');
  }

  puedeRegistrarAvance(): boolean {
    return ['gerente', 'supervisora', 'encargado_area'].includes(this.getUsuario()?.rol ?? '');
  }

  puedeAdministrar(): boolean { return ['gerente', 'supervisora'].includes(this.getUsuario()?.rol ?? ''); }

  puedeVerComisiones(): boolean {
    const usuario = this.getUsuario();
    return ['gerente', 'supervisora', 'contabilidad', 'caja_chica'].includes(usuario?.rol ?? '');
  }

  puedeGestionarDespacho(): boolean {
    const usuario = this.getUsuario();
    return ['gerente', 'supervisora', 'conductor'].includes(usuario?.rol ?? '');
  }
}
