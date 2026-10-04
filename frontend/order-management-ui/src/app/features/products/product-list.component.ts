import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  ReactiveFormsModule,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AddStockDialogComponent } from '../../shared/add-stock-dialog.component';

import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { ProductApi } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';
import { Product } from '../../core/models';
import { ConfirmService } from '../../shared/confirm-dialog.component';
import { LOW_STOCK_LIMIT } from '../../core/config';

@Component({
  selector: 'app-product-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    RouterLink,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './product-list.component.html',
})
export class ProductListComponent {
  auth = inject(AuthService);

  private api = inject(ProductApi);
  private snack = inject(MatSnackBar);
  private confirmDialog = inject(ConfirmService);

  lowLimit = LOW_STOCK_LIMIT;

  search = new FormControl('', {
    nonNullable: true,
  });

  products = signal<Product[]>([]);
  loading = signal(true);
  error = signal('');

  cols = ['name', 'price', 'stock', 'actions'];
    dialog: any;

  constructor() {
    this.search.valueChanges
      .pipe(
        startWith(''),

        // Wait until the user stops typing
        debounceTime(300),

        distinctUntilChanged(),

        tap(() => {
          this.loading.set(true);
          this.error.set('');
        }),

        // Cancels the previous request
        switchMap((query) =>
          this.api.getAll(query).pipe(
            catchError((err) => {
              this.error.set(errorMessage(err));

              return of([] as Product[]);
            }),
          ),
        ),

        // Automatically unsubscribe when component is destroyed
        takeUntilDestroyed(),
      )
      .subscribe((list) => {
        this.products.set(list);
        this.loading.set(false);
      });
  }

  remove(product: Product): void {
  this.confirmDialog
    .ask({
      title: 'Delete product?',
      message: `"${product.name}" will be removed from the catalogue.`,
      confirmText: 'Delete',
      danger: true,
    })
    .subscribe((ok) => {
      if (!ok) return;

      this.api.delete(product.id).subscribe({
        next: () => {
          this.products.update((list) =>
            list.filter((item) => item.id !== product.id),
          );
          this.snack.open('Product deleted', 'OK', { duration: 2500 });
        },
        error: (err) => {
          this.snack.open(errorMessage(err), 'OK', { duration: 4000 });
        },
      });
    });
}

addStock(product: Product): void {
  this.dialog
    .open(AddStockDialogComponent, {
      data: { name: product.name, current: product.stock },
      width: '380px',
      maxWidth: '92vw',
    })
    .afterClosed()
    .subscribe((qty: number) => {
      if (!qty) return;

      this.api.addStock(product.id, qty).subscribe({
        next: (updated) => {
          this.products.update((list) =>
            list.map((p) => (p.id === updated.id ? updated : p)),
          );
          this.snack.open(
            `Added ${qty} to ${updated.name}. Stock is now ${updated.stock}`,
            'OK',
            { duration: 3000 },
          );
        },
        error: (err) =>
          this.snack.open(errorMessage(err), 'OK', { duration: 4000 }),
      });
    });
}
}