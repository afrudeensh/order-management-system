import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormControl,
  ReactiveFormsModule,
} from '@angular/forms';
import { RouterLink } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';

import {
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  merge,
  of,
  startWith,
  switchMap,
  tap,
} from 'rxjs';

import { AuthService } from '../../core/auth.service';
import { ProductApi } from '../../core/api.service';
import { LOW_STOCK_LIMIT } from '../../core/config';
import { errorMessage } from '../../core/http-error';
import { Product } from '../../core/models';
import { AddStockDialogComponent } from '../../shared/add-stock-dialog.component';
import { ConfirmService } from '../../shared/confirm-dialog.component';

@Component({
  selector: 'app-product-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    RouterLink,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './product-list.component.html',
})
export class ProductListComponent {
  auth = inject(AuthService);

  private api = inject(ProductApi);
  private snack = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmService);

  lowLimit = LOW_STOCK_LIMIT;

  search = new FormControl('', { nonNullable: true });

  products = signal<Product[]>([]);
  total = signal(0);
  pageIndex = signal(0);
  pageSize = signal(10);
  sortField = signal('name');
  sortDir = signal<'asc' | 'desc'>('asc');

  loading = signal(true);
  error = signal('');

  cols = this.auth.isAdmin()
    ? ['image', 'name', 'color', 'price', 'stock', 'actions']
    : ['image', 'name', 'color', 'price', 'stock'];

  private reload = new Subject<void>();

  constructor() {
    merge(
      // typing in the search box: wait, then go back to page 1
      this.search.valueChanges.pipe(
        debounceTime(300),
        distinctUntilChanged(),
        tap(() => this.pageIndex.set(0)),
      ),
      // page change, sort change, delete, ...
      this.reload,
    )
      .pipe(
        startWith(null),                        // first load

        tap(() => {
          this.loading.set(true);
          this.error.set('');
        }),

        // cancels the previous request if a new one starts
        switchMap(() =>
          this.api
            .page({
              search: this.search.value.trim(),
              page: this.pageIndex(),
              size: this.pageSize(),
              sort: this.sortField(),
              direction: this.sortDir(),
            })
            .pipe(
              catchError((err) => {
                this.error.set(errorMessage(err));
                return of(null);
              }),
            ),
        ),

        takeUntilDestroyed(),
      )
      .subscribe((res) => {
        this.products.set(res?.content ?? []);
        this.total.set(res?.totalElements ?? 0);
        this.loading.set(false);
      });
  }

  onPage(e: PageEvent): void {
    this.pageIndex.set(e.pageIndex);
    this.pageSize.set(e.pageSize);
    this.reload.next();
  }

  onSort(s: Sort): void {
    this.sortField.set(s.active);
    this.sortDir.set(s.direction === 'desc' ? 'desc' : 'asc');
    this.pageIndex.set(0);
    this.reload.next();
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
            // deleted the last row of a page: step back one page
            if (this.products().length === 1 && this.pageIndex() > 0) {
              this.pageIndex.update((i) => i - 1);
            }
            this.reload.next();
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
      .subscribe((qty: number | null) => {
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