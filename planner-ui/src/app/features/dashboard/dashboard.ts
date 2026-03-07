import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div style="padding: 20px;">
      <h1>Panel Sterowania (Enterprise Planner)</h1>
      <p>Status: Zalogowano pomyślnie!</p>
      <hr>
      <p>Tutaj pojawią się Twoje aplikacje i wnioski.</p>
    </div>
  `
})
export class DashboardComponent { } // Klasa nazywa się DashboardComponent