import { Component, inject, signal, viewChild } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import {
  FormBuilder,
  FormGroupDirective,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';

import { OrderApi, ProductApi, UserApi } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/http-error';
import { Order, Product, UserSummary } from '../../core/models';
import { NotificationStore } from '../../core/notification-store.service';
import { ConfirmService } from '../../shared/confirm-dialog.component';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-orders',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule
  ],
  templateUrl: './orders.component.html',
})
export class OrdersComponent {
  auth = inject(AuthService);

  private fb = inject(FormBuilder).nonNullable;
  private productApi = inject(ProductApi);
  private orderApi = inject(OrderApi);
  private userApi = inject(UserApi);
  private snack = inject(MatSnackBar);
  private notificationStore = inject(NotificationStore);
  private confirmDialog = inject(ConfirmService);

  // The <form [formGroup]> in the template, used to clear the "submitted" state
  private formDir = viewChild(FormGroupDirective);

  products = signal<Product[]>([]);
  orders = signal<Order[]>([]);
  customers = signal<Record<number, UserSummary>>({});

  placing = signal(false);
  error = signal('');

  total = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  sortIndex = signal(0);

  sortOptions = [
    { label: 'Newest first',  sort: 'createdAt',   direction: 'desc' },
    { label: 'Oldest first',  sort: 'createdAt',   direction: 'asc' },
    { label: 'Highest total', sort: 'totalAmount', direction: 'desc' },
    { label: 'Lowest total',  sort: 'totalAmount', direction: 'asc' },
    { label: 'Status A-Z',    sort: 'status',      direction: 'asc' },
  ];

  form = this.fb.group({
    lines: this.fb.array([this.newLine()]),
  });

  private steps: Record<string, { status: string; label: string }> = {
    CREATED:   { status: 'CONFIRMED', label: 'Confirm' },
    CONFIRMED: { status: 'SHIPPED',   label: 'Mark shipped' },
    SHIPPED:   { status: 'DELIVERED', label: 'Mark delivered' },
  };

  get lines(): ReturnType<typeof this.newLine>[] {
    return this.form.controls.lines.controls;
  }

  constructor() {
    // Only customers need the product list, because only they place orders
    if (!this.auth.isAdmin()) {
      this.loadProducts();
    }
    this.loadOrders();
  }

  newLine() {
    return this.fb.group({
      productId: [null as number | null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
    });
  }

  addLine(): void {
    this.form.controls.lines.push(this.newLine());
  }

  removeLine(index: number): void {
    this.form.controls.lines.removeAt(index);
  }

  loadProducts(): void {
    this.productApi.getAll().subscribe({
      next: (products) => this.products.set(products),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  loadOrders(): void {
  const opt = this.sortOptions[this.sortIndex()];
  const query = {
    page: this.pageIndex(),
    size: this.pageSize(),
    sort: opt.sort,
    direction: opt.direction,
  };

  const request = this.auth.isAdmin()
    ? this.orderApi.allPage(query)
    : this.orderApi.minePage(query);

  request.subscribe({
    next: (res) => {
      this.orders.set(res.content);
      this.total.set(res.totalElements);

      if (this.auth.isAdmin()) {
        this.loadCustomers(res.content);
      }
    },
    error: (err) => this.error.set(errorMessage(err)),
  });
}

  /** Admin only: fetch the names of customers we haven't looked up yet. */
  private loadCustomers(orders: Order[]): void {
    const known = this.customers();
    const missing = [...new Set(orders.map((o) => o.userId))]
      .filter((id) => !(id in known));

    if (missing.length === 0) return;

    this.userApi.lookup(missing).subscribe({
      next: (users) => {
        this.customers.update((map) => {
          const copy = { ...map };
          users.forEach((u) => (copy[u.id] = u));
          return copy;
        });
      },
      error: () => {},       // not fatal: the page falls back to "#id"
    });
  }

  customerName(userId: number): string {
    return this.customers()[userId]?.name ?? `#${userId}`;
  }

  place(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.placing.set(true);
    this.error.set('');

    const items = this.form.controls.lines
      .getRawValue()
      .map((line) => ({
        productId: line.productId!,
        quantity: line.quantity,
      }));

    this.orderApi.create(items).subscribe({
      next: (order) => {
        this.snack.open(
          `Order #${order.id} placed. Total ${order.totalAmount}`,
          'OK',
          { duration: 4000 },
        );

        // Back to a single empty line
        this.form.setControl('lines', this.fb.array([this.newLine()]));

        // Clear the "submitted" state so the Product field is not red
        this.formDir()?.resetForm();

        this.placing.set(false);
        this.sortIndex.set(0);
        this.pageIndex.set(0);
        this.loadOrders();
        this.loadProducts();
        this.notificationStore.refreshSoon();
      },

      error: (err) => {
        this.error.set(errorMessage(err));
        this.placing.set(false);
      },
    });
  }

  nextStep(order: Order) {
    return this.steps[order.status];
  }

  canCancel(order: Order): boolean {
    return this.auth.isAdmin()
      ? ['CREATED', 'CONFIRMED'].includes(order.status)
      : order.status === 'CREATED';
  }

  advance(order: Order): void {
    const step = this.nextStep(order);
    if (!step) return;

    this.orderApi.updateStatus(order.id, step.status).subscribe({
      next: (updated) => {
        this.replace(updated);
        this.snack.open(`Order #${order.id} is now ${updated.status}`, 'OK', {
          duration: 2500,
        });
      },
      error: (err) =>
        this.snack.open(errorMessage(err), 'OK', { duration: 4000 }),
    });
  }

  cancel(order: Order): void {
    this.confirmDialog
      .ask({
        title: `Cancel order #${order.id}?`,
        message: 'The items will go back into stock. This cannot be undone.',
        confirmText: 'Yes, cancel order',
        cancelText: 'Keep order',
        danger: true,
      })
      .subscribe((ok) => {
        if (!ok) return;

        this.orderApi.cancel(order.id).subscribe({
          next: (updated) => {
            this.replace(updated);
            if (!this.auth.isAdmin()) {
              this.loadProducts();
            }
            this.snack.open(`Order #${order.id} cancelled`, 'OK', {
              duration: 2500,
            });
          },
          error: (err) =>
            this.snack.open(errorMessage(err), 'OK', { duration: 4000 }),
        });
      });
  }

  private replace(updated: Order): void {
    this.orders.update((list) =>
      list.map((o) => (o.id === updated.id ? updated : o)),
    );
  }

  onPage(e: PageEvent): void {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.loadOrders();
  }

  onSortChange(index: number): void {
    this.sortIndex.set(index);
    this.pageIndex.set(0);
    this.loadOrders();
  }
}