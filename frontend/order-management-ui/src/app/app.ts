import { Component, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { interval } from 'rxjs';

import { AuthService } from './core/auth.service';
import { NotificationStore } from './core/notification-store.service';

@Component({
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive,
    MatToolbarModule, MatButtonModule, MatIconModule, MatBadgeModule,
  ],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('order-management-ui');
  auth = inject(AuthService);
  store = inject(NotificationStore);

  constructor() {
    // Load when the user logs in, clear when they log out
    effect(() => {
      if (this.auth.isLoggedIn()) {
        this.store.load();
      } else {
        this.store.clear();
      }
    });

    // Notifications arrive through Kafka a moment after an order, so poll
    interval(15000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        if (this.auth.isLoggedIn()) {
          this.store.refresh();
        }
      });
  }
}