import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent {
  // Obiekt przechowujący dane z pól formularza
  credentials = { username: '', password: '' };
  
  // Zmienna do wyświetlania komunikatów o błędach
  errorMessage = '';

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  onSubmit() {
    this.authService.login(this.credentials).subscribe({
      next: (res: any) => {
        console.log('Logowanie udane, otrzymano odpowiedź:', res);
        
       
        this.router.navigate(['/dashboard']); 
      },
      error: (err: any) => {
        console.error('Błąd podczas logowania:', err);
        this.errorMessage = 'Nieprawidłowy login lub hasło. Sprawdź połączenie z bazą danych.';
      }
    });
  }
}