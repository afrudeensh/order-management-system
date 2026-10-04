import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe, TitleCasePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { forkJoin } from 'rxjs';

import {
  NotificationApi,
  OrderApi,
  ProductApi,
} from '../../core/api.service';

import { AuthService } from '../../core/auth.service';
import { Product } from '../../core/models';
import { LOW_STOCK_LIMIT } from '../../core/config';
import { LiveClockComponent } from '../../shared/live-clock.component';

// A product with fewer units than this counts as "low stock"

@Component({
  selector: 'app-dashboard',
  imports: [
    MatCardModule,
    MatButtonModule,
    RouterLink,
    CurrencyPipe,
    TitleCasePipe,
    LiveClockComponent
  ],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);

  private products = inject(ProductApi);
  private orders = inject(OrderApi);
  private notifications = inject(NotificationApi);

  lowStockLimit = LOW_STOCK_LIMIT;

  // Customer numbers
  counts = signal({
    products: 0,
    orders: 0,
    notifications: 0,
  });

  // Admin numbers
  stats = signal({
    products: 0,
    orders: 0,
    pending: 0,
    revenue: 0,
  });

  lowStock = signal<Product[]>([]);

  ngOnInit(): void {
    if (this.auth.isAdmin()) {
      this.loadAdmin();
    } else {
      this.loadCustomer();
    }
  }

  private loadCustomer(): void {
    forkJoin({
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

  private loadAdmin(): void {
    forkJoin({
      p: this.products.getAll(),
      o: this.orders.all(),
    }).subscribe(({ p, o }) => {
      this.lowStock.set(
        p
          .filter((item) => item.stock < LOW_STOCK_LIMIT)
          .sort((a, b) => a.stock - b.stock),
      );

      this.stats.set({
        products: p.length,
        orders: o.length,
        pending: o.filter((order) => order.status === 'CREATED').length,
        revenue: o
          .filter((order) => order.status !== 'CANCELLED')
          .reduce((sum, order) => sum + order.totalAmount, 0),
      });
    });
  }
}