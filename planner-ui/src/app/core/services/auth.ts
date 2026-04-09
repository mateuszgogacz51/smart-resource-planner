import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) { }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        if (response && response.token && isPlatformBrowser(this.platformId)) {
          localStorage.setItem('jwt_token', response.token);
        }
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('jwt_token');
    }
    return null;
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('jwt_token');
    }
  }

  getUsername(): string {
    const token = this.getToken();
    if (!token) return 'Użytkownik';
    try {
      const decoded: any = jwtDecode(token);
      return decoded.sub || 'Użytkownik';
    } catch (e) {
      return 'Użytkownik';
    }
  }

  // --- ZMIANA 1: Bezpieczne pobieranie ról (Zawsze zwraca String) ---
  getUserRole(): string {
    const token = this.getToken();
    if (!token) return '';
    try {
      const decoded: any = jwtDecode(token);
      const roles = decoded.role || decoded.roles || '';
      
      // Jeśli Spring Boot wysłał role jako tablicę (np. ['ROLE_ADMIN', 'ROLE_USER'])
      // zamieniamy to na jeden string, żeby dashboard.ts nie crashował przy .toUpperCase()
      if (Array.isArray(roles)) {
        return roles.join(',').toUpperCase();
      }
      return String(roles).toUpperCase();
    } catch (e) {
      return '';
    }
  }

  // --- ZMIANA 2: Pancerne sprawdzanie roli Admina ---
  isAdmin(): boolean {
    const role = this.getUserRole();
    // Sprawdzamy, czy ciąg zawiera słowo ADMIN, niezależnie od tego, czy to ROLE_ADMIN,ROLE_USER itp.
    return role.includes('ADMIN');
  }
}