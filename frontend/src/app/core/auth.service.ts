import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';

export interface Usuario {
  id?: number;
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
  private currentUserSubject = new BehaviorSubject<Usuario | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.tieneToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(request: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('usuario', JSON.stringify({
          nombre: response.nombre,
          correo: response.correo,
          rol: response.rol
        }));
        this.currentUserSubject.next({
          nombre: response.nombre,
          correo: response.correo,
          rol: response.rol
        });
        this.isAuthenticatedSubject.next(true);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('usuario');
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getUsuario(): Usuario | null {
    const u = localStorage.getItem('usuario');
    return u ? JSON.parse(u) : null;
  }

  tieneToken(): boolean {
    return !!localStorage.getItem('token');
  }

  esRol(rol: string): boolean {
    const usuario = this.getUsuario();
    return usuario?.rol === rol;
  }

  puedeVerComisiones(): boolean {
    const usuario = this.getUsuario();
    return ['gerente', 'supervisora', 'contabilidad', 'caja_chica'].includes(usuario?.rol ?? '');
  }

  puedeGestionarDespacho(): boolean {
    const usuario = this.getUsuario();
    return ['gerente', 'supervisora', 'conductor'].includes(usuario?.rol ?? '');
  }
}
