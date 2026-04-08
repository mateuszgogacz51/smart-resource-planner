import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/applications';
  private resourceUrl = 'http://localhost:8080/api/resources';

  /** Pobieranie wszystkich wniosków (dla Admina/Pracownika) z paginacją */
  getAllApplications(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  /** Pobieranie własnych wniosków użytkownika z paginacją */
  getMyApplications(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/my`, { params });
  }

  /** Tworzenie nowej rezerwacji */
  createApplication(app: any): Observable<any> {
    return this.http.post(this.apiUrl, app);
  }

  /** Zmiana statusu wniosku (Zatwierdź/Odrzuć) */
  updateStatus(id: number, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  /** Przypisanie wniosku do zalogowanego pracownika */
  assignApplication(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/assign`, {});
  }

  /** Dodawanie komentarza do wniosku */
  addComment(id: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/comments`, { content });
  }

  /** Pobieranie historii zmian (Audit Log) dla wniosku */
  getHistory(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/history`);
  }

  /** Pobieranie statystyk pracowników dla Admina */
  getEmployeeStats(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/admin/stats');
  }

  /** Zmiana roli użytkownika */
  changeUserRole(username: string, role: string): Observable<any> {
    return this.http.post(`http://localhost:8080/api/admin/users/${username}/role?role=${role}`, {});
  }

  /** Pobiera listę dostępnego sprzętu ze słownika zasobów */
  getAvailableResources(): Observable<any[]> {
    return this.http.get<any[]>(`${this.resourceUrl}/available`);
  }
}