import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Sprawdzamy czy token istnieje
  if (authService.getToken()) {
    return true; 
  }

  // Brak tokena -> powrót do logowania
  router.navigate(['/login']);
  return false;
};