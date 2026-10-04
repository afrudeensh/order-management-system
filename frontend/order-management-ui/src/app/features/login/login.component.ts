import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/http-error';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private fb = inject(FormBuilder).nonNullable;
  private auth = inject(AuthService);
  private router = inject(Router);

  registerMode = signal(false);
  loading = signal(false);
  error = signal('');

  form = this.fb.group({
    name: [''],
    email: [
      '',
      [
        Validators.required,
        Validators.email,
      ],
    ],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
      ],
    ],
  });

  toggle(): void {
    this.registerMode.update(
      (value) => !value,
    );

    this.error.set('');

    const name = this.form.controls.name;

    if (this.registerMode()) {
      name.addValidators(
        Validators.required,
      );
    } else {
      name.clearValidators();
    }

    name.updateValueAndValidity();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);

    const {
      name,
      email,
      password,
    } = this.form.getRawValue();

    const call = this.registerMode()
      ? this.auth.register({
          name,
          email,
          password,
        })
      : this.auth.login({
          email,
          password,
        });

    call.subscribe({
      next: () => {
        this.router.navigate([
          '/dashboard',
        ]);
      },

      error: (err) => {
        this.error.set(
          errorMessage(err),
        );

        this.loading.set(false);
      },
    });
  }
}