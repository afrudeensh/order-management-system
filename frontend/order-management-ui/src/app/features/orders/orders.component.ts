import { Component, inject, signal, viewChild } from '@angular/core';
import { ConfirmService } from '../../shared/confirm-dialog.component';
import {
  CurrencyPipe,
  DatePipe,
} from '@angular/common';
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

import {
  OrderApi,
  ProductApi,
} from '../../core/api.service';

import {
  Order,
  Product,
} from '../../core/models';

import { errorMessage } from '../../core/http-error';
import { NotificationStore } from '../../core/notification-store.service';
import { AuthService } from '../../core/auth.service';

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
  ],
  templateUrl: './orders.component.html',
})
export class OrdersComponent {
  private fb = inject(FormBuilder).nonNullable;
  private productApi = inject(ProductApi);
  private orderApi = inject(OrderApi);
  private snack = inject(MatSnackBar);
  private notificationStore = inject(NotificationStore);
  private confirmDialog = inject(ConfirmService);
  auth = inject(AuthService);

  // The <form [formGroup]> in the template, used to clear the "submitted" state
  private formDir = viewChild(FormGroupDirective);

  products = signal<Product[]>([]);
  orders = signal<Order[]>([]);

  placing = signal(false);
  error = signal('');

  form = this.fb.group({
    lines: this.fb.array([
      this.newLine(),
    ]),
  });

  private steps: Record<string, { status: string; label: string }> = {
  CREATED:   { status: 'CONFIRMED', label: 'Confirm' },
  CONFIRMED: { status: 'SHIPPED',   label: 'Mark shipped' },
  SHIPPED:   { status: 'DELIVERED', label: 'Mark delivered' },
};

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
      this.snack.open(`Order #${order.id} is now ${updated.status}`, 'OK', { duration: 2500 });
    },
    error: (err) => this.snack.open(errorMessage(err), 'OK', { duration: 4000 }),
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
          this.snack.open(`Order #${order.id} cancelled`, 'OK', { duration: 2500 });
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
      productId: [
        null as number | null,
        Validators.required,
      ],

      quantity: [
        1,
        [
          Validators.required,
          Validators.min(1),
        ],
      ],
    });
  }

  addLine(): void {
    this.form.controls.lines.push(
      this.newLine(),
    );
  }

  removeLine(index: number): void {
    this.form.controls.lines.removeAt(index);
  }

  loadProducts(): void {
    this.productApi.getAll().subscribe({
      next: (products) => {
        this.products.set(products);
      },

      error: (err) => {
        this.error.set(errorMessage(err));
      },
    });
  }

  loadOrders(): void {
    const request = this.auth.isAdmin()
    ? this.orderApi.all()
    : this.orderApi.mine();

  request.subscribe({
    next: (orders) => {
      // Newest first
      this.orders.set([...orders].sort((a, b) => b.id - a.id));
    },

    error: (err) => {
      this.error.set(errorMessage(err));
    },
  });
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
          {
            duration: 4000,
          },
        );

        // Back to a single empty line
        this.form.setControl(
          'lines',
          this.fb.array([
            this.newLine(),
          ]),
        );

        // Clear the "submitted" state so the Product field is not red
        this.formDir()?.resetForm();

        this.placing.set(false);

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
}