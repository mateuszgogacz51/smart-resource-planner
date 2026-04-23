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

  // --- ZMIENNE STANU ---
  applications: any[] = [];
  userRole: string = '';
  activeTab: 'applications' | 'employees' | 'admin-users' | 'admin-resources' = 'applications';
  
  allUsers: any[] = [];
  availableResources: any[] = [];
  employeeStats: any[] = [];
  totalReservations: number = 0; 
  
  selectedDept: string = 'WSZYSTKIE';
  filterStart: string = '';
  filterEnd: string = '';
  
  selectedUserAudit: any = null;
  showAuditModal: boolean = false;

  currentPage: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;
  searchTerm: string = '';
  searchSubject: Subject<string> = new Subject<string>();
  statusFilter: string = 'ALL';
  pendingCount: number = 0;

  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  selectedCategory: string = '';
  newComments: { [key: number]: string } = {};
  newResourceData = { name: '', type: 'LAPTOP' };

  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';
  statusChart: any;
  workloadChart: any;

  // --- INICJALIZACJA ---
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

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = null;
      this.cdr.detectChanges();
    }, 3000);
    this.cdr.detectChanges();
  }

  // --- ŁADOWANIE DANYCH ---
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
        this.pendingCount = this.applications.filter(a => a.status === 'PENDING').length;
        this.cdr.detectChanges(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  loadResources() {
    this.appService.getAvailableResources().subscribe({
      next: (data) => {
        this.availableResources = data;
        this.cdr.detectChanges();
      }
    });
  }

  // --- ZARZĄDZANIE KONTAMI (ADMIN) ---
  loadUsers() {
    this.authService.getAllUsers().subscribe({
      next: (data) => {
        this.allUsers = data;
        this.cdr.detectChanges();
      },
      error: () => this.showToast('Błąd pobierania użytkowników', 'error')
    });
  }

  updateUserRole(userId: number, event: any) {
    const newRole = event.target.value;
    this.authService.changeUserRole(userId, newRole).subscribe({
      next: () => {
        this.showToast('Zmieniono uprawnienia użytkownika', 'success');
        this.loadUsers();
      },
      error: () => this.showToast('Nie udało się zmienić roli', 'error')
    });
  }

  resetUserPassword(userId: number) {
    if (confirm('Czy na pewno chcesz zresetować hasło i wysłać maila do tego użytkownika?')) {
      this.authService.resetPassword(userId).subscribe({
        next: () => this.showToast('✅ Nowe hasło zostało wysłane na e-mail!', 'success'),
        error: (err) => this.showToast('❌ Błąd: ' + (err.error?.message || 'Nie udało się wysłać emaila'), 'error')
      });
    }
  }

  // --- EDYCJA UŻYTKOWNIKA ---
  showEditUserModal: boolean = false;
  editingUser: any = {};

  openEditUser(user: any) {
    this.editingUser = { ...user };
    this.showEditUserModal = true;
  }

  saveUserEdit() {
    this.authService.updateUserDetails(this.editingUser.id, this.editingUser).subscribe({
      next: () => {
        this.showToast('✅ Dane użytkownika zostały zaktualizowane!', 'success');
        this.closeEditUser();
        this.loadUsers(); 
      },
      error: () => this.showToast('❌ Błąd podczas aktualizacji danych.', 'error')
    });
  }

  closeEditUser() {
    this.showEditUserModal = false;
    this.editingUser = {};
  }

  // --- AUDYT UŻYTKOWNIKA ---
  openUserAudit(userId: number) {
    this.authService.getUserFullProfile(userId).subscribe({
      next: (data) => {
        this.selectedUserAudit = data;
        this.showAuditModal = true;
        this.cdr.detectChanges();
      },
      error: () => this.showToast('Błąd pobierania danych audytowych', 'error')
    });
  }

  closeAudit() {
    this.showAuditModal = false;
    this.selectedUserAudit = null;
  }

  // --- NAWIGACJA ZAKŁADEK ---
  switchTab(tab: 'applications' | 'employees' | 'admin-users' | 'admin-resources') {
    this.activeTab = tab;
    if (tab === 'employees') {
      this.loadEmployeeStats();
    } else if (tab === 'applications') {
      this.loadData();
    } else if (tab === 'admin-users') {
      this.loadUsers();
    }
    this.cdr.detectChanges();
  }

  getFilteredApplications() {
    if (this.statusFilter === 'ALL') return this.applications;
    return this.applications.filter(app => app.status === this.statusFilter);
  }

  setFilter(status: string) {
    this.statusFilter = status;
    this.cdr.detectChanges();
  }

  onSearchInput(event: any) {
    this.searchSubject.next(event.target.value);
  }

  get filteredResourcesForNewApp() {
    if (!this.selectedCategory) return [];
    return this.availableResources.filter(res => res.type === this.selectedCategory);
  }

  onCategoryChange() {
    this.newApp.resourceId = null; 
  }

  // --- AKCJE WNIOSKÓW ---
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

  changeStatus(id: number, newStatus: string) {
    this.appService.updateStatus(id, newStatus.toUpperCase()).subscribe({
      next: () => { 
        this.showToast(`Zmieniono status na: ${newStatus}`, 'success'); 
        this.loadData(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  showFulfillmentModal: boolean = false;
  appToFulfill: number | null = null;
  fulfillmentSerialNumber: string = '';

  openFulfillment(appId: number) {
    this.appToFulfill = appId;
    this.fulfillmentSerialNumber = '';
    this.showFulfillmentModal = true;
  }

  confirmFulfillment() {
    if (this.appToFulfill) {
      this.changeStatus(this.appToFulfill, 'ACCEPTED');
      if (this.fulfillmentSerialNumber) {
        const officialNote = `[MAGAZYN - WYDANO SPRZĘT] Przypisany numer inwentarzowy / S/N: ${this.fulfillmentSerialNumber}`;
        this.appService.addComment(this.appToFulfill, officialNote).subscribe(() => {
          this.loadData();
        });
      }
    }
    this.closeFulfillment();
  }

  closeFulfillment() {
    this.showFulfillmentModal = false;
    this.appToFulfill = null;
    this.fulfillmentSerialNumber = '';
  }

  assignToMe(id: number) {
    this.appService.assignApplication(id).subscribe({
      next: () => { 
        this.showToast('Przypisano do Ciebie', 'success'); 
        this.loadData(); 
      },
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

  // --- AKCJE MAGAZYNU ---
  addNewResource() {
    this.appService.createResource(this.newResourceData).subscribe({
      next: () => {
        this.showToast('✅ Dodano nowy zasób do magazynu!');
        this.newResourceData.name = ''; 
        this.loadResources(); 
      },
      error: () => this.showToast('❌ Błąd podczas dodawania zasobu.', 'error')
    });
  }

  removeResource(id: number) {
    if(confirm('Na pewno chcesz usunąć ten zasób? Powiązane wnioski mogą zgłosić błąd.')) {
      this.appService.deleteResource(id).subscribe({
        next: () => {
          this.showToast('🗑️ Usunięto zasób.');
          this.loadResources();
        },
        error: () => this.showToast('❌ Błąd: Zasób może być w użyciu.', 'error')
      });
    }
  }

  // --- WYKRESY ---
  renderCharts(stats: any) {
    if (this.statusChart) this.statusChart.destroy();
    if (this.workloadChart) this.workloadChart.destroy();

    Chart.defaults.color = '#A0A0A0';

    const ctxStatus = document.getElementById('statusChart') as HTMLCanvasElement;
    if (ctxStatus) {
      this.statusChart = new Chart(ctxStatus, {
        type: 'doughnut',
        data: {
          labels: Object.keys(stats.statusDistribution),
          datasets: [{
            data: Object.values(stats.statusDistribution),
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
          labels: Object.keys(stats.employeeRanking),
          datasets: [{
            label: 'Obsłużone wnioski',
            data: Object.values(stats.employeeRanking),
            backgroundColor: '#2D4F4F',
            hoverBackgroundColor: '#99FFCC',
            borderRadius: 4
          }]
        },
        options: { 
          responsive: true, 
          maintainAspectRatio: false,
          scales: { x: { grid: { display: false } }, y: { grid: { color: '#2A2A2A' } } }
        }
      });
    }
  }

  loadEmployeeStats() {
    this.appService.getDashboardStats(this.selectedDept, this.filterStart, this.filterEnd).subscribe({
      next: (stats: any) => {
        this.totalReservations = stats.totalReservations;
        this.cdr.detectChanges();
        this.renderCharts(stats); 
      },
      error: (err: any) => this.showToast('Błąd pobierania statystyk', 'error')
    });
  }

  // --- NARZĘDZIA POMOCNICZE ---
  nextPage() { if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.loadData(); } }
  prevPage() { if (this.currentPage > 0) { this.currentPage--; this.loadData(); } }
  private handleError(err: any) { this.showToast(err.error?.message || "Błąd połączenia", 'error'); }
  printReport() { window.print(); }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}