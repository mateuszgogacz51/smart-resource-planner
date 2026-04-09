import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  isLoginMode = true;
  confirmPassword = '';

  loginData = {
    username: '',
    password: ''
  };

  registerData = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: ''
  };

  login() {
    if (!this.loginData.username || !this.loginData.password) {
      alert('Wypełnij pola logowania.');
      return;
    }
    this.authService.login(this.loginData).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => alert('Błędny login lub hasło.')
    });
  }

  register() {
    if (this.registerData.password !== this.confirmPassword) {
      alert('Hasła nie są zgodne!');
      return;
    }

    this.authService.register(this.registerData).subscribe({
      next: () => {
        alert('✅ Konto utworzone! Możesz się teraz zalogować.');
        this.isLoginMode = true;
        this.registerData = { username: '', password: '', email: '', firstName: '', lastName: '' };
        this.confirmPassword = '';
      },
      error: (err) => alert('❌ Błąd rejestracji: ' + (err.error || 'Spróbuj ponownie.'))
    });
  }
}