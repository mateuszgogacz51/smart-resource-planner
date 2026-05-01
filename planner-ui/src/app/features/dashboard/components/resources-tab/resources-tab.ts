import { Component, OnInit, inject, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../../core/services/application';

@Component({
  selector: 'app-resources-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './resources-tab.html'
})
export class ResourcesTabComponent implements OnInit {
  private appService = inject(ApplicationService);
  
  @Output() toast = new EventEmitter<{message: string, type: 'success' | 'error'}>();

  availableResources: any[] = [];
  newResourceData = { name: '', type: 'LAPTOP' };
  
  resourceSearchTerm: string = '';
  resourceCategoryFilter: string = 'ALL';

  ngOnInit() {
    this.loadResources();
  }

  loadResources() {
    this.appService.getAvailableResources().subscribe({
      next: (data) => this.availableResources = data,
      error: () => this.toast.emit({message: 'Błąd pobierania bazy zasobów', type: 'error'})
    });
  }

  get filteredResourcesList() {
    return this.availableResources.filter(res => {
      const matchesSearch = (res.name || '').toLowerCase().includes(this.resourceSearchTerm.toLowerCase());
      const matchesType = this.resourceCategoryFilter === 'ALL' || res.type === this.resourceCategoryFilter;
      return matchesSearch && matchesType;
    });
  }

  addNewResource() {
    this.appService.createResource(this.newResourceData).subscribe({
      next: () => {
        this.toast.emit({message: '✅ Dodano nowy zasób do magazynu!', type: 'success'});
        this.newResourceData.name = ''; 
        this.loadResources(); 
      },
      error: () => this.toast.emit({message: '❌ Błąd podczas dodawania zasobu.', type: 'error'})
    });
  }

  removeResource(id: number) {
    if(confirm('Na pewno chcesz usunąć ten zasób? Powiązane wnioski mogą zgłosić błąd.')) {
      this.appService.deleteResource(id).subscribe({
        next: () => {
          this.toast.emit({message: '🗑️ Usunięto zasób.', type: 'success'});
          this.loadResources();
        },
        error: () => this.toast.emit({message: '❌ Błąd: Zasób może być w użyciu.', type: 'error'})
      });
    }
  }
}