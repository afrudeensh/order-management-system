import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { API_URL } from './config';
import {
  AppNotification,
  Order,
  OrderItemRequest,
  PageQuery,
  PageResponse,
  Product,
  ProductRequest,
  UserSummary,
} from './models';

function toParams(q: PageQuery): HttpParams {
  let params = new HttpParams()
    .set('page', q.page)
    .set('size', q.size)
    .set('sort', q.sort)
    .set('direction', q.direction);

  if (q.search) {
    params = params.set('search', q.search);
  }
  return params;
}

@Injectable({ providedIn: 'root' })
export class ProductApi {
  private http = inject(HttpClient);
  private url = `${API_URL}/products`;

  getAll(search = '') {
    const params = search ? new HttpParams().set('search', search) : undefined;
    return this.http.get<Product[]>(this.url, { params });
  }

  page(q: PageQuery) {
    return this.http.get<PageResponse<Product>>(`${this.url}/page`, {
      params: toParams(q),
    });
  }

  get(id: number) {
    return this.http.get<Product>(`${this.url}/${id}`);
  }

  create(p: ProductRequest) {
    return this.http.post<Product>(this.url, p);
  }

  update(id: number, p: ProductRequest) {
    return this.http.put<Product>(`${this.url}/${id}`, p);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  addStock(id: number, quantity: number) {
    return this.http.put<Product>(`${this.url}/${id}/stock/increase`, null, {
      params: { quantity },
    });
  }
}

@Injectable({ providedIn: 'root' })
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
    return this.http.get<Order[]>(this.url);          // admin only
  }

  minePage(q: PageQuery) {
    return this.http.get<PageResponse<Order>>(`${this.url}/my/page`, {
      params: toParams(q),
    });
  }

  allPage(q: PageQuery) {                              // admin only
    return this.http.get<PageResponse<Order>>(`${this.url}/page`, {
      params: toParams(q),
    });
  }

  updateStatus(id: number, status: string) {
    return this.http.put<Order>(`${this.url}/${id}/status`, { status });
  }

  cancel(id: number) {
    return this.http.put<Order>(`${this.url}/${id}/cancel`, {});
  }
}

@Injectable({ providedIn: 'root' })
export class NotificationApi {
  private http = inject(HttpClient);

  mine() {
    return this.http.get<AppNotification[]>(`${API_URL}/notifications/my`);
  }
}

@Injectable({ providedIn: 'root' })
export class UserApi {
  private http = inject(HttpClient);

  lookup(ids: number[]) {
    return this.http.get<UserSummary[]>(`${API_URL}/users/lookup`, {
      params: { ids: ids.join(',') },
    });
  }
}