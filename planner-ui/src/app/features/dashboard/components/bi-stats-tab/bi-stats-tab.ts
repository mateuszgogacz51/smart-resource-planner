import { Component, OnInit, inject, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../../core/services/application';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-bi-stats-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bi-stats-tab.html' // <-- NAPRAWIONA ŚCIEŻKA DO HTML
})
export class BiStatsTabComponent implements OnInit {
  private appService = inject(ApplicationService);
  private cdr = inject(ChangeDetectorRef);

  @Output() showError = new EventEmitter<string>();

  employeeStats: any[] = [];
  totalReservations: number = 0;
  
  selectedDept: string = 'WSZYSTKIE';
  filterStart: string = '';
  filterEnd: string = '';

  statusChart: any;
  workloadChart: any;

  ngOnInit() {
    this.loadEmployeeStats();
  }

  loadEmployeeStats() {
    this.appService.getDashboardStats(this.selectedDept, this.filterStart, this.filterEnd).subscribe({
      next: (stats: any) => {
        this.totalReservations = stats.totalReservations;
        this.cdr.detectChanges();
        this.renderCharts(stats); 
      },
      error: (err: any) => this.showError.emit('Błąd pobierania statystyk')
    });
  }

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

  printReport() { window.print(); }
}