import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app'; // <--- Importujemy z pliku app.ts

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));