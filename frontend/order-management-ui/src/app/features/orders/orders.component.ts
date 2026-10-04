import { Component, inject, signal, viewChild } from '@angular/core';
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

  get lines(): ReturnType<typeof this.newLine>[] {
    return this.form.controls.lines.controls;
  }

  constructor() {
    this.loadProducts();
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
    this.orderApi.mine().subscribe({
      next: (orders) => {
        this.orders.set(orders);
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