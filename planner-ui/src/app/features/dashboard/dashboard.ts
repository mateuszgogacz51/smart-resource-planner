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
  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  newComments: { [key: number]: string } = {};

  ngOnInit() {
    this.userRole = this.authService.getUserRole();
    this.loadData();
  }

  loadData() {
    // Pobieramy aktualną rolę bezpośrednio z serwisu przy każdym wywołaniu
    const currentRole = this.authService.getUserRole();
    const isAdminUser = currentRole === 'ROLE_ADMIN' || currentRole === 'ADMIN' || 
                        currentRole === 'ROLE_EMPLOYEE' || currentRole === 'EMPLOYEE';

    const request = isAdminUser
      ? this.appService.getAllApplications() 
      : this.appService.getMyApplications();

    request.subscribe({
      next: (data) => {
        this.applications = data;
        console.log('Dane załadowane:', data);
      },
      error: (err) => console.error('Błąd pobierania danych:', err)
    });
  }

  logout() {
    this.authService.logout(); 
    this.router.navigate(['/login']); 
  }

  changeStatus(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    this.appService.updateStatus(id, newStatus).subscribe({
      next: () => { 
        alert(`Status zmieniony na: ${newStatus}`); 
        this.loadData(); 
      },
      error: (err) => alert('Błąd: ' + err.message)
    });
  }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('Wysłano wniosek!');
        this.loadData();
        this.newApp = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err) => alert('Błąd wysyłania: ' + err.message)
    });
  }

  switchTab(tab: 'applications' | 'employees') {
    this.activeTab = tab;
    if (tab === 'employees') {
      this.appService.getEmployeeStats().subscribe({
        next: (data) => this.employeeStats = data,
        error: (err) => console.error('Błąd statystyk:', err)
      });
    } else {
      this.loadData();
    }
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  assignToMe(id: number) {
    this.appService.assignApplication(id).subscribe({
      next: () => { 
        alert('Przypisano!'); 
        this.loadData(); 
      },
      error: (err) => alert('Błąd: ' + err.message)
    });
  }

  addComment(id: number) {
    if (!this.newComments[id]?.trim()) return;
    this.appService.addComment(id, this.newComments[id]).subscribe({
      next: () => { 
        this.newComments[id] = ''; 
        this.loadData(); 
      },
      error: (err) => alert('Błąd: ' + err.message)
    });
  }

  changeRole(username: string, newRole: string) {
    if (confirm(`Zmienić rolę dla ${username}?`)) {
      this.appService.changeUserRole(username, newRole).subscribe({
        next: () => { this.switchTab('employees'); },
        error: (err) => alert('Błąd: ' + err.message)
      });
    }
  }
}