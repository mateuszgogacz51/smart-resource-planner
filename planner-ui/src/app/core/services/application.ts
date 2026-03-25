import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/applications';

  // Pobieranie wszystkich (dla Admina) z paginacją
  getAllApplications(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  // Pobieranie własnych (dla Usera) z paginacją
  getMyApplications(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/my`, { params });
  }

  createApplication(app: any): Observable<any> {
    return this.http.post(this.apiUrl, app);
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  assignApplication(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/assign`, {});
  }

  addComment(id: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/comments`, { content });
  }

  getEmployeeStats(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/admin/stats');
  }

  changeUserRole(username: string, role: string): Observable<any> {
    return this.http.post(`http://localhost:8080/api/admin/users/${username}/role?role=${role}`, {});
  }
}