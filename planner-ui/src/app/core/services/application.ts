import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private apiUrl = 'http://localhost:8080/api/applications';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // --- NOWE: Metoda automatycznie pobierająca Twój token ---
  private getHeaders(): HttpHeaders {
    let token = null;
    if (isPlatformBrowser(this.platformId)) {
      token = localStorage.getItem('jwt_token'); // Pobieramy token zapisany przy logowaniu
    }
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`, // Dodajemy przepustkę dla Spring Security
      'Content-Type': 'application/json'
    });
  }

  // Zaktualizowane metody, które teraz wysyłają token w nagłówku:
  
  createApplication(appData: any): Observable<any> {
    return this.http.post(this.apiUrl, appData, { headers: this.getHeaders() });
  }

  getAllApplications(): Observable<any> {
    return this.http.get(this.apiUrl, { headers: this.getHeaders() });
  }

  getMyApplications(): Observable<any> {
    return this.http.get(`${this.apiUrl}/my`, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: string): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/status?status=${status}`, {}, { headers: this.getHeaders() });
  }

  addComment(id: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/comments`, { content }, { headers: this.getHeaders() });
  }
}