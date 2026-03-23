import { Component, OnInit, inject, PLATFORM_ID, Inject } from '@angular/core';
import { CommonModule, DatePipe, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { Router } from '@angular/router';
import { ApplicationService } from '../../core/services/application';
import { AuthService } from '../../core/services/auth';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe], 
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  private appService = inject(ApplicationService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private platformId = inject(PLATFORM_ID);

  applications: Application[] = [];
  userRole: string = '';
  
  newApp: Partial<Application> = {
    resourceId: 0,
    startTime: '',
    endTime: '',
    status: 'PENDING'
  };

  activeCommentId: number | null = null;
  commentText: string = '';

  ngOnInit() {
    this.userRole = this.authService.getUserRole();
    console.log('Twoja rola z tokena:', this.userRole);
    this.loadData();
  }

  // Funkcja pomocnicza sprawdzająca uprawnienia (elastyczna dla ADMIN / ROLE_ADMIN)
  isUserAdmin(): boolean {
    return this.userRole.includes('ADMIN') || this.userRole.includes('EMPLOYEE');
  }

  loadData() {
    // Jeśli rola zawiera ADMIN, pobieramy wszystko. Jeśli nie - tylko wnioski użytkownika.
    const request = this.isUserAdmin()
      ? this.appService.getAllApplications() 
      : this.appService.getMyApplications();

    request.subscribe({
      next: (data) => {
        this.applications = data;
        console.log('Pobrane wnioski dla widoku:', data);
      },
      error: (err) => {
        console.error('Błąd ładowania danych:', err);
        // Jeśli 401, wyloguj
        if (err.status === 401) this.logout();
      }
    });
  }

  logout() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('jwt_token');
    }
    this.router.navigate(['/login']);
  }

  openCommentBox(id: number) {
    this.activeCommentId = id;
    this.commentText = '';
  }

  cancelComment() {
    this.activeCommentId = null;
    this.commentText = '';
  }

  processDecision(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    if (this.commentText.trim()) {
      this.appService.addComment(id, this.commentText).subscribe({
        next: () => this.changeStatus(id, newStatus),
        error: (err) => alert('Błąd notatki: ' + err.message)
      });
    } else {
      this.changeStatus(id, newStatus);
    }
  }

  changeStatus(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    this.appService.updateStatus(id, newStatus).subscribe({
      next: () => {
        this.activeCommentId = null;
        this.loadData();
      },
      error: (err) => alert('Błąd zmiany statusu: ' + err.message)
    });
  }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('Wniosek wysłany!');
        this.loadData();
        this.newApp = { resourceId: 0, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err) => alert('Błąd wysyłania: ' + err.message)
    });
  }
}