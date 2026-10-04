import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { DatePipe } from '@angular/common';

import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';

import { NotificationStore } from '../../core/notification-store.service';

@Component({
  selector: 'app-notifications',
  imports: [MatListModule, MatIconModule, DatePipe],
  templateUrl: './notifications.component.html',
})
export class NotificationsComponent implements OnInit, OnDestroy {
  store = inject(NotificationStore);
  items = this.store.items; 

  ngOnInit(): void {
    this.store.startWatching();
  }

  ngOnDestroy(): void {
    this.store.stopWatching();
  }
}