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
  // Przełącznik między trybem logowania a rejestracji
  isRegisterMode = false;

  // Zmienna do wyświetlania komunikatów o błędach
  errorMessage = '';

  // Obiekt przechowujący dane do logowania
  credentials = { username: '', password: '' };

  // Obiekt przechowujący dane do rejestracji (TO LIKWIDUJE CZERWONE PODKREŚLENIA)
  registerData = {
    username: '',
    password: '',
    email: '',
    firstName: '',
    lastName: ''
  };

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  // Metoda do przełączania widoku (Logowanie <-> Rejestracja)
  toggleMode() {
    this.isRegisterMode = !this.isRegisterMode;
    this.errorMessage = ''; // Czyścimy błędy przy przełączaniu
  }

  // Metoda logowania
  login() {
    this.authService.login(this.credentials).subscribe({
      next: (res: any) => {
        console.log('Logowanie udane, otrzymano odpowiedź:', res);
        this.router.navigate(['/dashboard']); 
      },
      error: (err: any) => {
        console.error('Błąd podczas logowania:', err);
        this.errorMessage = 'Nieprawidłowy login lub hasło. Sprawdź połączenie z bazą danych.';
        alert(this.errorMessage);
      }
    });
  }

  // Metoda rejestracji
  register() {
    this.authService.register(this.registerData).subscribe({
      next: (msg: string) => {
        alert(msg); // Pokazuje z serwera: "Rejestracja zakończona sukcesem!"
        this.toggleMode(); // Wraca do ekranu logowania
        this.credentials.username = this.registerData.username; // Przepisuje wpisany login
      },
      error: (err: any) => {
        console.error('Błąd rejestracji:', err);
        alert('Błąd rejestracji: ' + (err.error || err.message));
      }
    });
  }
}