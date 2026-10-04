import { Injectable, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

import { API_URL } from './config';
import { AuthResponse } from './models';

const TOKEN_KEY = 'oms_token';
const USER_KEY = 'oms_user';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private _user = signal<AuthResponse | null>(this.loadUser());

  readonly user = this._user.asReadonly();

  readonly isLoggedIn = computed(() => this._user() !== null);

  readonly isAdmin = computed(() => this._user()?.role === 'ADMIN');

  login(body: { email: string; password: string }) {
    return this.http
      .post<AuthResponse>(`${API_URL}/users/login`, body)
      .pipe(tap((res) => this.save(res)));
  }

  register(body: {
    name: string;
    email: string;
    password: string;
  }) {
    return this.http
      .post<AuthResponse>(`${API_URL}/users/register`, body)
      .pipe(tap((res) => this.save(res)));
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);

    this._user.set(null);

    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  hasValidToken(): boolean {
    const token = this.token;

    if (!token) {
      return false;
    }

    try {
      const b64 = token
        .split('.')[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/');

      const payload = JSON.parse(atob(b64));

      // JWT exp is in seconds, Date.now() is in milliseconds.
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  private save(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);

    localStorage.setItem(
      USER_KEY,
      JSON.stringify({
        ...res,
        token: '',
      }),
    );

    this._user.set(res);
  }

  private loadUser(): AuthResponse | null {
    const raw = localStorage.getItem(USER_KEY);

    return raw ? (JSON.parse(raw) as AuthResponse) : null;
  }
}
