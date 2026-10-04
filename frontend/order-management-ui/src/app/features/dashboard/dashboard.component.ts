import {
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { RouterLink } from '@angular/router';

import { MatCardModule } from '@angular/material/card';

import { forkJoin } from 'rxjs';

import {
  NotificationApi,
  OrderApi,
  ProductApi,
} from '../../core/api.service';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [
    MatCardModule,
    RouterLink,
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);

  private products = inject(ProductApi);
  private orders = inject(OrderApi);
  private notifications = inject(NotificationApi);

  counts = signal({
    products: 0,
    orders: 0,
    notifications: 0,
  });

  ngOnInit(): void {
    forkJoin({
      // 3 requests run in parallel
      p: this.products.getAll(),
      o: this.orders.mine(),
      n: this.notifications.mine(),
    }).subscribe((result) => {
      this.counts.set({
        products: result.p.length,
        orders: result.o.length,
        notifications: result.n.length,
      });
    });
  }
}