import {
  HttpErrorResponse,
  HttpInterceptorFn,
  HttpResponse,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, map, throwError } from 'rxjs';

import { AuthService } from './auth.service';
import { API_URL } from './config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;

  const request =
    token && req.url.startsWith(API_URL)
      ? req.clone({
          setHeaders: { Authorization: `Bearer ${token}` },
        })
      : req;

  return next(request).pipe(
    // Unwrap { success, status, message, data } -> data
    map((event) => {
      if (event instanceof HttpResponse) {
        const body: any = event.body;

        if (
          body &&
          typeof body === 'object' &&
          'success' in body &&
          'data' in body
        ) {
          return event.clone({ body: body.data });
        }
      }

      return event;
    }),

    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/users/login')) {
        auth.logout();
      }

      return throwError(() => err);
    }),
  );
};