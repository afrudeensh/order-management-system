import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { API_URL } from './config';
import {
  AppNotification,
  Order,
  OrderItemRequest,
  Product,
  ProductRequest,
} from './models';

@Injectable({
  providedIn: 'root',
})
export class ProductApi {
  private http = inject(HttpClient);
  private url = `${API_URL}/products`;

  getAll(search = '') {
    const params = search
      ? new HttpParams().set('search', search)
      : undefined;

    return this.http.get<Product[]>(this.url, { params });
  }

  get(id: number) {
    return this.http.get<Product>(`${this.url}/${id}`);
  }

  create(product: ProductRequest) {
    return this.http.post<Product>(this.url, product);
  }

  update(id: number, product: ProductRequest) {
    return this.http.put<Product>(
      `${this.url}/${id}`,
      product,
    );
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

@Injectable({
  providedIn: 'root',
})
export class OrderApi {
  private http = inject(HttpClient);
  private url = `${API_URL}/orders`;

  create(items: OrderItemRequest[]) {
    return this.http.post<Order>(this.url, { items });
  }

  mine() {
    return this.http.get<Order[]>(`${this.url}/my`);
  }

  all() {
    return this.http.get<Order[]>(this.url);   // admin only
  }

  updateStatus(id: number, status: string) {
    return this.http.put<Order>(`${this.url}/${id}/status`, { status });
  }

  cancel(id: number) {
    return this.http.put<Order>(`${this.url}/${id}/cancel`, {});
  }
}

@Injectable({
  providedIn: 'root',
})
export class NotificationApi {
  private http = inject(HttpClient);

  mine() {
    return this.http.get<AppNotification[]>(
      `${API_URL}/notifications/my`,
    );
  }
}

