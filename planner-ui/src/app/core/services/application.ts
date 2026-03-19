import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApplicationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/applications';

  // Pobieranie wniosków zalogowanego użytkownika (widok USER)
  getMyApplications(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  // NOWE: Pobieranie WSZYSTKICH wniosków (widok EMPLOYEE/ADMIN)
  getAllApplications(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all`);
  }

  // NOWE: Zmiana statusu wniosku (Akceptacja/Odrzucenie)
  updateStatus(id: number, status: 'ACCEPTED' | 'REJECTED'): Observable<any> {
    // Wysyłamy pusty body {}, bo status przekazujemy w parametrze URL (?status=...)
    return this.http.patch(`${this.apiUrl}/${id}/status?status=${status}`, {});
  }

  // Składanie nowego wniosku
  createApplication(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }
}