import { Component, inject, signal } from '@angular/core';
import {
  FormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  ActivatedRoute,
  Router,
  RouterLink,
} from '@angular/router';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ProductApi } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';

@Component({
  selector: 'app-product-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './product-form.component.html',
})
export class ProductFormComponent {
  private fb = inject(FormBuilder).nonNullable;
  private api = inject(ProductApi);
  private router = inject(Router);
  private snack = inject(MatSnackBar);

  id = Number(
    inject(ActivatedRoute).snapshot.paramMap.get('id'),
  ) || null;

  error = signal('');

  form = this.fb.group({
    name: ['', Validators.required],
    price: [
      0,
      [
        Validators.required,
        Validators.min(0.01),
      ],
    ],
    stock: [
      0,
      [
        Validators.required,
        Validators.min(0),
      ],
    ],
  });

  constructor() {
    if (this.id) {
      this.api.get(this.id).subscribe({
        next: (product) => {
          this.form.patchValue(product);
        },
        error: (err) => {
          this.error.set(errorMessage(err));
        },
      });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const body = this.form.getRawValue();

    const call = this.id
      ? this.api.update(this.id, body)
      : this.api.create(body);

    call.subscribe({
      next: () => {
        this.snack.open('Saved', 'OK', {
          duration: 2000,
        });

        this.router.navigate(['/products']);
      },

      error: (err) => {
        this.error.set(errorMessage(err));
      },
    });
  }
}