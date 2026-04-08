import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router'; 
import { ApplicationService } from '../../core/services/application';
import { AuthService } from '../../core/services/auth';

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

  applications: any[] = [];
  userRole: string = '';
  activeTab: 'applications' | 'employees' = 'applications';
  employeeStats: any[] = [];
  availableResources: any[] = [];

  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  pageSize: number = 10;

  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  newComments: { [key: number]: string } = {};

  ngOnInit() {
    this.userRole = (this.authService.getUserRole() || '').toUpperCase();
    this.loadData();
    this.loadResources();
  }

  loadResources() {
    this.appService.getAvailableResources().subscribe({
      next: (data) => this.availableResources = data
    });
  }

  loadData() {
    const role = (this.authService.getUserRole() || '').toUpperCase();
    const isPrivileged = role.includes('ADMIN') || role.includes('EMPLOYEE');

    const request = isPrivileged
      ? this.appService.getAllApplications(this.currentPage, this.pageSize)
      : this.appService.getMyApplications(this.currentPage, this.pageSize);

    request.subscribe({
      next: (response) => {
        this.applications = response.content; 
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
      },
      error: (err: any) => this.handleError(err)
    });
  }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('✅ Wysłano! SYSTEM-BOT zweryfikuje wniosek.');
        this.loadData();
        this.newApp = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  showHistory(id: number) {
    this.appService.getHistory(id).subscribe({
      next: (logs: any[]) => {
        if (!logs || logs.length === 0) { alert("Brak historii."); return; }
        const historyStr = logs.map(l => 
          `🕒 ${new Date(l.timestamp).toLocaleString()}: ${l.oldStatus} -> ${l.newStatus} (${l.changedBy})`
        ).join('\n');
        alert(`Historia wniosku #${id}:\n\n${historyStr}`);
      }
    });
  }

  changeStatus(id: number, newStatus: string) {
    this.appService.updateStatus(id, newStatus.toUpperCase()).subscribe({
      next: () => {
        alert('✅ Zmieniono status!');
        this.loadData();
      },
      error: (err: any) => this.handleError(err)
    });
  }

  assignToMe(id: number) {
    this.appService.assignApplication(id).subscribe(() => this.loadData());
  }

  addComment(id: number) {
    const content = this.newComments[id];
    if (!content) return;
    this.appService.addComment(id, content).subscribe(() => {
      this.newComments[id] = '';
      this.loadData();
    });
  }

  switchTab(tab: 'applications' | 'employees') {
    this.activeTab = tab;
    if (tab === 'employees') this.loadEmployeeStats();
    else this.loadData();
  }

  loadEmployeeStats() {
    this.appService.getEmployeeStats().subscribe(data => this.employeeStats = data);
  }

  nextPage() { if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.loadData(); } }
  prevPage() { if (this.currentPage > 0) { this.currentPage--; this.loadData(); } }
  private handleError(err: any) { alert("❌ Błąd: " + (err.error?.message || "Brak połączenia")); }
  isAdmin(): boolean { return this.authService.isAdmin(); }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}