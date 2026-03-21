import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { ApplicationService } from '../../core/services/application';
import { AuthService } from '../../core/services/auth'; // <--- DODANE
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  // Korzystamy z nowoczesnego 'inject' zamiast tradycyjnego konstruktora
  private appService = inject(ApplicationService);
  private authService = inject(AuthService);

  applications: Application[] = [];
  userRole: string = '';
  
  newApp: Partial<Application> = {
    resourceId: 0,
    startTime: '',
    endTime: '',
    status: 'PENDING'
  };

  ngOnInit() {
    this.userRole = this.authService.getUserRole(); // Pobieramy rolę (np. ROLE_USER lub ROLE_EMPLOYEE)
    this.loadData();
  }

  // Decyduje czy pobrać tylko "moje" czy "wszystkie" wnioski
  loadData() {
    const request = (this.userRole === 'ROLE_EMPLOYEE' || this.userRole === 'ROLE_ADMIN')
      ? this.appService.getAllApplications() 
      : this.appService.getMyApplications();

    request.subscribe({
      next: (data) => this.applications = data,
      error: (err) => console.error('Błąd pobierania danych:', err)
    });
  }

  // Metoda dla Pracownika: Akceptacja/Odrzucenie
  changeStatus(id: number, newStatus: 'ACCEPTED' | 'REJECTED') {
    this.appService.updateStatus(id, newStatus).subscribe({
      next: () => {
        alert(`Status zmieniony na: ${newStatus}`);
        this.loadData(); // Odświeżamy tabelę
      },
      error: (err) => alert('Błąd zmiany statusu: ' + err.message)
    });
  }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('Wniosek wysłany pomyślnie!');
        this.loadData();
        this.newApp = { resourceId: 0, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err) => alert('Błąd wysyłania: ' + err.message)
    });
  }
}