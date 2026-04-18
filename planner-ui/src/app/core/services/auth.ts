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
  private adminUsersUrl = 'http://localhost:8080/api/admin/users'; // Centralna zmienna dla panelu Admina

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

  getUserRole(): string {
    const token = this.getToken();
    if (!token) return '';
    try {
      const decoded: any = jwtDecode(token);
      const roles = decoded.role || decoded.roles || '';

      if (Array.isArray(roles)) {
        return roles.join(',').toUpperCase();
      }
      return String(roles).toUpperCase();
    } catch (e) {
      return '';
    }
  }

  isAdmin(): boolean {
    const role = this.getUserRole();
    return role.includes('ADMIN');
  }

  // --- PANEL ADMINISTRATORA: ZARZĄDZANIE KONTAMI I AUDYT ---

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(this.adminUsersUrl);
  }

  changeUserRole(userId: number, newRole: string): Observable<any> {
    return this.http.patch(`${this.adminUsersUrl}/${userId}/role?newRole=${newRole}`, {});
  }

  // Nowa funkcja audytowa (Enterprise)
  getUserFullProfile(userId: number): Observable<any> {
    return this.http.get<any>(`${this.adminUsersUrl}/${userId}/full-profile`);
  }
  resetPassword(userId: number): Observable<any> {
    return this.http.post(`${this.adminUsersUrl}/${userId}/reset-password`, {});
  }
}