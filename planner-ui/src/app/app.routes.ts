import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
// RĘCZNY IMPORT: Zwróć uwagę na końcówkę './features/dashboard/dashboard'
import { DashboardComponent } from './features/dashboard/dashboard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent }, // To naprawi błąd NG04002
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];