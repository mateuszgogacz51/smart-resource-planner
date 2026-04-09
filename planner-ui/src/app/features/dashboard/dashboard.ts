import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router'; 
import { ApplicationService } from '../../core/services/application';
import { AuthService } from '../../core/services/auth';

import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private appService = inject(ApplicationService);
  private router = inject(Router);
  public authService = inject(AuthService); 
  private cdr = inject(ChangeDetectorRef);

  applications: any[] = [];
  userRole: string = '';
  activeTab: 'applications' | 'employees' = 'applications';
  employeeStats: any[] = [];
  availableResources: any[] = [];

  // Paginacja
  currentPage: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;

  // Nowy wniosek i komentarze
  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  newComments: { [key: number]: string } = {};

  // Live Search
  searchTerm: string = '';
  searchSubject: Subject<string> = new Subject<string>();

  // Pakiet Admina: Filtry i Liczniki
  statusFilter: string = 'ALL';
  pendingCount: number = 0;

  // Toasty i Wykresy
  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';
  statusChart: any;
  workloadChart: any;

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = null;
      this.cdr.detectChanges();
    }, 3000);
    this.cdr.detectChanges();
  }

  ngOnInit() {
    this.userRole = (this.authService.getUserRole() || '').toUpperCase();
    
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe((term: string) => {
      this.searchTerm = term;
      this.currentPage = 0;
      this.loadData();
    });

    this.loadData();
    this.loadResources();
  }

  onSearchInput(event: any) {
    this.searchSubject.next(event.target.value);
  }

  loadResources() {
    this.appService.getAvailableResources().subscribe({
      next: (data) => {
        this.availableResources = data;
        this.cdr.detectChanges();
      }
    });
  }

  loadData() {
    const role = (this.authService.getUserRole() || '').toUpperCase();
    this.userRole = role; 
    const isPrivileged = role.includes('ADMIN') || role.includes('EMPLOYEE');

    const request = isPrivileged
      ? this.appService.getAllApplications(this.currentPage, this.pageSize, this.searchTerm)
      : this.appService.getMyApplications(this.currentPage, this.pageSize, this.searchTerm);

    request.subscribe({
      next: (response) => {
        this.applications = response.content; 
        this.totalPages = response.totalPages;
        
        // Aktualizacja licznika PENDING dla Admina
        this.pendingCount = this.applications.filter(a => a.status === 'PENDING').length;
        
        this.cdr.detectChanges(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  // Funkcje Filtrowania i Raportów
  getFilteredApplications() {
    if (this.statusFilter === 'ALL') return this.applications;
    return this.applications.filter(app => app.status === this.statusFilter);
  }

  setFilter(status: string) {
    this.statusFilter = status;
    this.cdr.detectChanges();
  }

  printReport() {
    window.print();
  }

  renderCharts(empData: any[], statusData: any) {
    if (this.statusChart) this.statusChart.destroy();
    if (this.workloadChart) this.workloadChart.destroy();

    Chart.defaults.color = '#A0A0A0';

    const ctxStatus = document.getElementById('statusChart') as HTMLCanvasElement;
    if (ctxStatus) {
      this.statusChart = new Chart(ctxStatus, {
        type: 'doughnut',
        data: {
          labels: Object.keys(statusData),
          datasets: [{
            data: Object.values(statusData),
            backgroundColor: ['#FDCB6E', '#99FFCC', '#FF6B6B', '#2D4F4F'],
            borderColor: '#252525',
            borderWidth: 2
          }]
        },
        options: { responsive: true, maintainAspectRatio: false }
      });
    }

    const ctxWorkload = document.getElementById('workloadChart') as HTMLCanvasElement;
    if (ctxWorkload) {
      this.workloadChart = new Chart(ctxWorkload, {
        type: 'bar',
        data: {
          labels: empData.map(e => e.username),
          datasets: [{
            label: 'Wnioski',
            data: empData.map(e => e.completedCount),
            backgroundColor: '#2D4F4F',
            hoverBackgroundColor: '#99FFCC',
            borderRadius: 4
          }]
        },
        options: { 
          responsive: true, 
          maintainAspectRatio: false,
          scales: {
            x: { grid: { display: false } },
            y: { grid: { color: '#2A2A2A' } }
          }
        }
      });
    }
  }

  loadEmployeeStats() {
    this.appService.getEmployeeStats().subscribe(empData => {
      this.employeeStats = empData;
      this.appService.getStatusStats().subscribe(statusData => {
        this.cdr.detectChanges();
        this.renderCharts(empData, statusData);
      });
    });
  }

  switchTab(tab: 'applications' | 'employees') {
    this.activeTab = tab;
    if (tab === 'employees') this.loadEmployeeStats();
    else this.loadData();
    this.cdr.detectChanges();
  }

  // Obsługa akcji statusu i komentarzy
  changeStatus(id: number, newStatus: string) {
    this.appService.updateStatus(id, newStatus.toUpperCase()).subscribe({
      next: () => { 
        this.showToast(`Zmieniono status na: ${newStatus}`, 'success'); 
        this.loadData(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  assignToMe(id: number) {
    this.appService.assignApplication(id).subscribe({
      next: () => { this.showToast('Przypisano do Ciebie', 'success'); this.loadData(); },
      error: (err: any) => this.handleError(err)
    });
  }

  addComment(id: number) {
    const content = this.newComments[id];
    if (!content) return;
    this.appService.addComment(id, content).subscribe({
      next: () => { 
        this.showToast('Dodano notatkę', 'success'); 
        this.newComments[id] = ''; 
        this.loadData(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        this.showToast('Wniosek został wysłany!', 'success');
        this.newApp = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
        this.loadData();
      },
      error: (err: any) => this.handleError(err)
    });
  }

  nextPage() { if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.loadData(); } }
  prevPage() { if (this.currentPage > 0) { this.currentPage--; this.loadData(); } }
  private handleError(err: any) { this.showToast(err.error?.message || "Błąd połączenia", 'error'); }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}