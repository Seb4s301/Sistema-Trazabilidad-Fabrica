import { Component } from '@angular/core';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent {

  lotesEnPlanta = 8; // badge de lotes en planta

  constructor(public authService: AuthService) {}

  logout(): void {
    this.authService.logout();
  }
}
