import { Injectable, computed, inject, signal } from '@angular/core';

import { NotificationApi } from './api.service';
import { AuthService } from './auth.service';
import { AppNotification } from './models';

@Injectable({ providedIn: 'root' })
export class NotificationStore {
  private api = inject(NotificationApi);
  private auth = inject(AuthService);

  private _items = signal<AppNotification[]>([]);
  readonly items = this._items.asReadonly();

  private seenId = signal(0);
  private watching = false; // true while the Notifications page is open

  readonly unread = computed(
    () => this._items().filter((n) => n.id > this.seenId()).length,
  );

  private key(): string {
    return `oms_seen_notification_${this.auth.user()?.userId}`;
  }

  load(): void {
    this.seenId.set(Number(localStorage.getItem(this.key()) ?? 0));
    this.refresh();
  }

  refresh(): void {
    this.api.mine().subscribe({
      next: (list) => {
        this._items.set(list);

        // If the user is looking at the page, new items count as read
        if (this.watching) {
          this.saveSeen(list);
        }
      },
      error: () => {},
    });
  }

  // Kafka delivers the notification a moment after the order, so check twice
  refreshSoon(): void {
    [1000, 3000].forEach((ms) => setTimeout(() => this.refresh(), ms));
  }

  startWatching(): void {
    this.watching = true;
    this.saveSeen(this._items());
    this.refresh();
  }

  stopWatching(): void {
    this.watching = false;
  }

  clear(): void {
    this._items.set([]);
    this.seenId.set(0);
  }

  private saveSeen(list: AppNotification[]): void {
    const maxId = list.reduce((max, n) => Math.max(max, n.id), 0);
    const next = Math.max(this.seenId(), maxId);

    this.seenId.set(next);
    localStorage.setItem(this.key(), String(next));
  }
}