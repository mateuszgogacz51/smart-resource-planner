import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router'; 
import { AuthService } from '../../core/services/auth';

import { BiStatsTabComponent } from './components/bi-stats-tab/bi-stats-tab';
import { UsersTabComponent } from './components/users-tab/users-tab';
import { ApplicationsTabComponent } from './components/applications-tab/applications-tab';
import { ResourcesTabComponent } from './components/resources-tab/resources-tab';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BiStatsTabComponent, UsersTabComponent, ApplicationsTabComponent, ResourcesTabComponent], 
  templateUrl: './dashboard.html'
})
export class DashboardComponent {
  private router = inject(Router);
  public authService = inject(AuthService); 
  private cdr = inject(ChangeDetectorRef);

  activeTab: 'applications' | 'employees' | 'admin-users' | 'admin-resources' = 'applications';
  pendingCount: number = 0;

  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';

  showToast(message: string, type: 'success' | 'error' = 'success') {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = null;
      this.cdr.detectChanges();
    }, 3000);
    this.cdr.detectChanges();
  }

  updatePendingCount(count: number) {
    this.pendingCount = count;
    this.cdr.detectChanges();
  }

  switchTab(tab: 'applications' | 'employees' | 'admin-users' | 'admin-resources') {
    this.activeTab = tab;
    this.cdr.detectChanges();
  }

  logout() { 
    this.authService.logout(); 
    this.router.navigate(['/login']); 
  }
}