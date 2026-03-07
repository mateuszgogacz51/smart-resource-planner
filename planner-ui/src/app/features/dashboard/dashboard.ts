import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { ApplicationService } from '../../core/services/application';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule], 
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {
  applications: Application[] = [];
  
  // Obiekt dla nowego wniosku (rezerwacji)
  newApp: Partial<Application> = {
    resourceId: 0,
    startTime: '',
    endTime: '',
    status: 'PENDING'
  };

  constructor(private appService: ApplicationService) {}

  ngOnInit() {
    this.loadApplications();
  }

  // Pobiera listę wszystkich wniosków użytkownika
  loadApplications() {
    this.appService.getApplications().subscribe({
      next: (data) => this.applications = data,
      error: (err) => console.error('Błąd pobierania danych:', err)
    });
  }

  // Wysyła nowy wniosek do backendu i odświeża tabelę
  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        alert('Wniosek wysłany pomyślnie!');
        this.loadApplications(); // Ponowne pobranie danych po sukcesie
        // Resetowanie formularza do stanu początkowego
        this.newApp = { resourceId: 0, startTime: '', endTime: '', status: 'PENDING' }; 
      },
      error: (err) => alert('Błąd wysyłania: ' + err.message)
    });
  }
}