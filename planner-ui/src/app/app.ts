import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.html', // <-- upewnij się, że ten plik HTML też tak się nazywa!
})
export class AppComponent {  // <-- WAŻNE: Tu musi być AppComponent
  title = 'planner-ui';
}
