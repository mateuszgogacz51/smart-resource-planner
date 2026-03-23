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
    // responseType: 'text', bo nasz backend zwraca zwykły tekst ("Rejestracja zakończona..."), a nie JSON
    return this.http.post(`${this.apiUrl}/register`, userData, { responseType: 'text' });
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('jwt_token');
    }
    return null;
  }
  getUserRole(): string {
  const token = this.getToken();
  if (!token) return '';
  const decoded: any = jwtDecode(token);
  return decoded.role || ''; // Spring Boot przesyła rolę w polu 'role'
}

isAdmin(): boolean {
  return this.getUserRole() === 'ROLE_ADMIN';
}
}