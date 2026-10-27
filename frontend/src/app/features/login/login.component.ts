import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, AuthRequest } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  form: FormGroup;
  loading = false;
  errorMessage = '';
  mostrarPassword = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      correo: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
      recordar: [false]
    });
  }

  togglePassword(): void {
    this.mostrarPassword = !this.mostrarPassword;
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const request: AuthRequest = {
      correo: this.form.value.correo,
      password: this.form.value.password
    };

    this.authService.login(request).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/resumen']);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Credenciales inválidas. Verifica tu correo y contraseña.';
      }
    });
  }
}
