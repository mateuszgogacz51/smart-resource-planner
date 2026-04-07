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

  // --- DANE I STATUSY ---
  applications: any[] = [];
  userRole: string = '';
  activeTab: 'applications' | 'employees' = 'applications';
  employeeStats: any[] = [];
  
  // --- NOWE: SŁOWNIK ZASOBÓW ---
  availableResources: any[] = [];

  // --- PAGINACJA ---
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  pageSize: number = 10;

  // --- FORMULARZE I KOMENTARZE ---
  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  newComments: { [key: number]: string } = {};

  ngOnInit() {
    this.userRole = this.authService.getUserRole();
    this.loadData();
    this.loadResources();
  }

  /**
   * Pobiera listę dostępnego sprzętu do dropdowna
   */
  loadResources() {
    if (this.userRole === 'ROLE_USER' || this.userRole === 'USER') {
      this.appService.getAvailableResources().subscribe({
        next: (data) => this.availableResources = data,
        error: (err) => console.error('Błąd pobierania zasobów:', err)
      });
    }
  }

  /**
   * Główna funkcja ładująca dane z uwzględnieniem paginacji
   */
  loadData() {
    const role = this.authService.getUserRole();
    const isPrivileged = role === 'ROLE_ADMIN' || role === 'ADMIN' || 
                         role === 'ROLE_EMPLOYEE' || role === 'EMPLOYEE';

    const request = isPrivileged
      ? this.appService.getAllApplications(this.currentPage, this.pageSize)
      : this.appService.getMyApplications(this.currentPage, this.pageSize);

    request.subscribe({
      next: (response) => {
        this.applications = response.content; 
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
      },
      error: (err) => this.handleError(err)
    });
  }

  private handleError(err: any) {
    const errorTitle = err.error?.message || "Błąd systemu";
    const errorDetail = err.error?.details || "Nie udało się połączyć z serwerem.";
    alert(`❌ ${errorTitle}\nSzczegóły: ${errorDetail}`);
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadData();
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadData();
    }
  }

  submitApplication() {
    if (!this.newApp.resourceId) {
      alert('Proszę wybrać sprzęt z listy!');
      return;
    }
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('✅ Wniosek wysłany pomyślnie.');
        this.currentPage = 0; 
        this.loadData();
        this.newApp = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err) => this.handleError(err)
    });
  }

  changeStatus(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    this.appService.updateStatus(id, newStatus).subscribe({
      next: () => { 
        alert(`✅ Status zmieniony na: ${newStatus}`); 
        this.loadData(); 
      },
      error: (err) => this.handleError(err)
    });
  }

  assignToMe(id: number) {
    this.appService.assignApplication(id).subscribe({
      next: () => { 
        alert('👷 Wniosek przypisany do Twojej obsługi.'); 
        this.loadData(); 
      },
      error: (err) => this.handleError(err)
    });
  }

  addComment(id: number) {
    const content = this.newComments[id];
    if (!content || content.trim() === '') return;

    this.appService.addComment(id, content).subscribe({
      next: () => { 
        this.newComments[id] = ''; 
        this.loadData(); 
      },
      error: (err) => this.handleError(err)
    });
  }

  switchTab(tab: 'applications' | 'employees') {
    this.activeTab = tab;
    if (tab === 'employees') {
      this.appService.getEmployeeStats().subscribe({
        next: (data) => this.employeeStats = data,
        error: (err) => this.handleError(err)
      });
    } else {
      this.currentPage = 0; 
      this.loadData();
    }
  }

  changeRole(username: string, newRole: string) {
    if (confirm(`Zmienić rolę użytkownika ${username} na ${newRole}?`)) {
      this.appService.changeUserRole(username, newRole).subscribe({
        next: () => { 
          alert('✅ Rola zaktualizowana.');
          this.switchTab('employees'); 
        },
        error: (err) => this.handleError(err)
      });
    }
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  logout() {
    this.authService.logout(); 
    this.router.navigate(['/login']); 
  }
}