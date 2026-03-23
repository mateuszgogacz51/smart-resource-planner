import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/applications';
  private adminUrl = 'http://localhost:8080/api/admin';

  getMyApplications(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/my`);
  }

  getAllApplications(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  updateStatus(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  createApplication(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }

  getEmployeeStats(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}/users/stats`);
  }

  // --- NOWE FUNKCJE ---
  assignApplication(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/assign`, {});
  }

  addComment(id: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/comments`, { content });
  }

  changeUserRole(username: string, newRole: string): Observable<any> {
    return this.http.patch(`${this.adminUrl}/users/${username}/role?newRole=${newRole}`, {});
  }
}