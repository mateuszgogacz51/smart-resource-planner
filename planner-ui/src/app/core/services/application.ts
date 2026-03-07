import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application } from '../models/application.model';
import { AuthService } from './auth';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private apiUrl = 'http://localhost:8080/api/applications';

  constructor(private http: HttpClient, private authService: AuthService) {}

  getApplications(): Observable<Application[]> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    return this.http.get<Application[]>(this.apiUrl, { headers });
  }

  // DODAJ TĘ METODĘ:
  createApplication(application: Partial<Application>): Observable<Application> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.getToken()}`);
    return this.http.post<Application>(this.apiUrl, application, { headers });
  }
}