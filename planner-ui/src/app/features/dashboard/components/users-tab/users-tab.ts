import { Component, OnInit, inject, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth';

@Component({
  selector: 'app-users-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users-tab.html' // <--- TUTAJ JEST POPRAWKA, BEZ ".component"
})
export class UsersTabComponent implements OnInit {
  public authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  // Zdarzenie do wysyłania powiadomień Toast do głównego komponentu
  @Output() toast = new EventEmitter<{message: string, type: 'success' | 'error'}>();

  allUsers: any[] = [];
  userSearchTerm: string = '';
  userDeptFilter: string = 'WSZYSTKIE';

  showEditUserModal: boolean = false;
  editingUser: any = {};

  selectedUserAudit: any = null;
  showAuditModal: boolean = false;

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.authService.getAllUsers().subscribe({
      next: (data) => {
        this.allUsers = data;
        this.cdr.detectChanges();
      },
      error: () => this.toast.emit({ message: 'Błąd pobierania użytkowników', type: 'error' })
    });
  }

  get filteredUsers() {
    return this.allUsers.filter(u => {
      const searchStr = `${u.firstName || ''} ${u.lastName || ''} ${u.username || ''}`.toLowerCase();
      const matchesSearch = searchStr.includes(this.userSearchTerm.toLowerCase());
      const matchesDept = this.userDeptFilter === 'WSZYSTKIE' || u.department === this.userDeptFilter;
      return matchesSearch && matchesDept;
    });
  }

  updateUserRole(userId: number, event: any) {
    const newRole = event.target.value;
    this.authService.changeUserRole(userId, newRole).subscribe({
      next: () => {
        this.toast.emit({ message: 'Zmieniono uprawnienia użytkownika', type: 'success' });
        this.loadUsers();
      },
      error: () => this.toast.emit({ message: 'Nie udało się zmienić roli', type: 'error' })
    });
  }

  resetUserPassword(userId: number) {
    if (confirm('Czy na pewno chcesz zresetować hasło i wysłać maila do tego użytkownika?')) {
      this.authService.resetPassword(userId).subscribe({
        next: () => this.toast.emit({ message: '✅ Nowe hasło zostało wysłane na e-mail!', type: 'success' }),
        error: (err) => this.toast.emit({ message: '❌ Błąd: ' + (err.error?.message || 'Nie udało się wysłać emaila'), type: 'error' })
      });
    }
  }

  openEditUser(user: any) {
    this.editingUser = { ...user };
    this.showEditUserModal = true;
  }

  saveUserEdit() {
    this.authService.updateUserDetails(this.editingUser.id, this.editingUser).subscribe({
      next: () => {
        this.toast.emit({ message: '✅ Dane użytkownika zostały zaktualizowane!', type: 'success' });
        this.closeEditUser();
        this.loadUsers();
      },
      error: () => this.toast.emit({ message: '❌ Błąd podczas aktualizacji danych.', type: 'error' })
    });
  }

  closeEditUser() {
    this.showEditUserModal = false;
    this.editingUser = {};
  }

  openUserAudit(userId: number) {
    this.authService.getUserFullProfile(userId).subscribe({
      next: (data) => {
        this.selectedUserAudit = data;
        this.showAuditModal = true;
        this.cdr.detectChanges();
      },
      error: () => this.toast.emit({ message: 'Błąd pobierania danych audytowych', type: 'error' })
    });
  }

  closeAudit() {
    this.showAuditModal = false;
    this.selectedUserAudit = null;
  }
}