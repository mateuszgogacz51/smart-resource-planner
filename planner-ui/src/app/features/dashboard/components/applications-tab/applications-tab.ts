import { Component, OnInit, inject, ChangeDetectorRef, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationService } from '../../../../core/services/application';
import { AuthService } from '../../../../core/services/auth';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-applications-tab',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './applications-tab.html'
})
export class ApplicationsTabComponent implements OnInit {
  private appService = inject(ApplicationService);
  public authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  @Output() toast = new EventEmitter<{message: string, type: 'success' | 'error'}>();
  @Output() pendingCountChange = new EventEmitter<number>(); // Informuje Dashboard o liczbie oczekujących

  applications: any[] = [];
  availableResources: any[] = [];
  
  currentPage: number = 0;
  totalPages: number = 0;
  pageSize: number = 10;
  searchTerm: string = '';
  searchSubject: Subject<string> = new Subject<string>();
  statusFilter: string = 'ALL';
  appCategoryFilter: string = 'ALL';
  pendingCount: number = 0;

  newApp: any = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
  selectedCategory: string = '';
  newComments: { [key: number]: string } = {};

  showFulfillmentModal: boolean = false;
  appToFulfill: number | null = null;
  fulfillmentSerialNumber: string = '';

  ngOnInit() {
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

  loadData() {
    const role = (this.authService.getUserRole() || '').toUpperCase();
    const isPrivileged = role.includes('ADMIN') || role.includes('EMPLOYEE');

    const request = isPrivileged
      ? this.appService.getAllApplications(this.currentPage, this.pageSize, this.searchTerm)
      : this.appService.getMyApplications(this.currentPage, this.pageSize, this.searchTerm);

    request.subscribe({
      next: (response) => {
        this.applications = response.content; 
        this.totalPages = response.totalPages;
        this.pendingCount = this.applications.filter(a => a.status === 'PENDING').length;
        this.pendingCountChange.emit(this.pendingCount); // Aktualizacja dymku (badge) na górze
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

  get filteredResourcesForNewApp() {
    if (!this.selectedCategory) return [];
    return this.availableResources.filter(res => res.type === this.selectedCategory);
  }

  getFilteredApplications() {
    let filtered = this.applications;
    if (this.statusFilter !== 'ALL') filtered = filtered.filter(app => app.status === this.statusFilter);
    if (this.appCategoryFilter !== 'ALL') filtered = filtered.filter(app => app.resource?.type === this.appCategoryFilter);
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(app => 
        (app.resource?.name || '').toLowerCase().includes(term) ||
        (app.userId || '').toLowerCase().includes(term) ||
        (app.assignedEmployee || '').toLowerCase().includes(term)
      );
    }
    return filtered;
  }

  setFilter(status: string) { this.statusFilter = status; this.cdr.detectChanges(); }
  onSearchInput(event: any) { this.searchSubject.next(event.target.value); }
  onCategoryChange() { this.newApp.resourceId = null; }

  submitApplication() {
    this.appService.createApplication(this.newApp).subscribe({
      next: () => {
        this.toast.emit({message: 'Wniosek został wysłany!', type: 'success'});
        this.newApp = { resourceId: null, startTime: '', endTime: '', status: 'PENDING' };
        this.loadData();
      },
      error: (err: any) => this.handleError(err)
    });
  }

  changeStatus(id: number, newStatus: string) {
    this.appService.updateStatus(id, newStatus.toUpperCase()).subscribe({
      next: () => { 
        this.toast.emit({message: `Zmieniono status na: ${newStatus}`, type: 'success'}); 
        this.loadData(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

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
        this.appService.addComment(this.appToFulfill, officialNote).subscribe(() => this.loadData());
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
      next: () => { this.toast.emit({message: 'Przypisano do Ciebie', type: 'success'}); this.loadData(); },
      error: (err: any) => this.handleError(err)
    });
  }

  addComment(id: number) {
    const content = this.newComments[id];
    if (!content) return;
    this.appService.addComment(id, content).subscribe({
      next: () => { 
        this.toast.emit({message: 'Dodano notatkę', type: 'success'}); 
        this.newComments[id] = ''; 
        this.loadData(); 
      },
      error: (err: any) => this.handleError(err)
    });
  }

  nextPage() { if (this.currentPage < this.totalPages - 1) { this.currentPage++; this.loadData(); } }
  prevPage() { if (this.currentPage > 0) { this.currentPage--; this.loadData(); } }
  printReport() { window.print(); }
  private handleError(err: any) { this.toast.emit({message: err.error?.message || "Błąd połączenia", type: 'error'}); }
}