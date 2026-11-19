import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, UrlTree, Router } from '@angular/router';
import { Observable, map, take } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate, CanActivateChild {

  constructor(private authService: AuthService, private router: Router) {}

  canActivateChild(route: ActivatedRouteSnapshot): boolean | UrlTree {
    if (!this.authService.tieneToken()) return this.router.createUrlTree(['/login']);
    if (route.routeConfig?.path === 'administracion' && !this.authService.puedeAdministrar()) return this.router.createUrlTree(['/resumen']);
    if (route.routeConfig?.path === 'comisiones' && !this.authService.puedeVerComisiones()) return this.router.createUrlTree(['/resumen']);
    if (['ordenes/nueva', 'ordenes/:numero/editar'].includes(route.routeConfig?.path || '') && !this.authService.puedeCrearOrden()) return this.router.createUrlTree(['/ordenes']);
    return true;
  }

  canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.authService.isAuthenticated$.pipe(
      take(1),
      map(isAuthenticated => {
        if (isAuthenticated && this.authService.tieneToken()) {
          return true;
        }
        return this.router.createUrlTree(['/login']);
      })
    );
  }
}
