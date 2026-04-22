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

  getAllApplications(page: number = 0, size: number = 10, search?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) params = params.set('search', search); 
    
    return this.http.get<any>(this.apiUrl, { params });
  }

  getMyApplications(page: number = 0, size: number = 10, search?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (search) params = params.set('search', search);
    
    return this.http.get<any>(this.apiUrl, { params });
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

  unassignApplication(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/unassign`, {});
  }

  addComment(id: number, content: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/comments`, { content });
  }

  getHistory(id: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/${id}/history`);
  }

  getEmployeeStats(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/admin/stats');
  }

  getStatusStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/stats/statuses`);
  }

  getAvailableResources(): Observable<any[]> {
    return this.http.get<any[]>(`${this.resourceUrl}/available`);
  }

  // --- NOWE METODY MAGAZYNU (TYLKO ADMIN) ---
  createResource(resource: { name: string, type: string }): Observable<any> {
    return this.http.post(this.resourceUrl, resource);
  }

  deleteResource(id: number): Observable<any> {
    return this.http.delete(`${this.resourceUrl}/${id}`);
  }
getDashboardStats(department: string = 'WSZYSTKIE', startDate: string = '', endDate: string = '') {
    let params = `?department=${department}`;
    if (startDate) params += `&startDate=${startDate}`;
    if (endDate) params += `&endDate=${endDate}`;
    
    return this.http.get<any>(`${this.apiUrl}/stats/dashboard${params}`);
  }
}