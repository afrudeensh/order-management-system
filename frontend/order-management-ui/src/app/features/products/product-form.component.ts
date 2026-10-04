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
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';

import { ProductApi } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';
import { fileToResizedDataUrl } from '../../shared/image-resize';

@Component({
  selector: 'app-product-form',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
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
  imageError = signal('');

  form = this.fb.group({
    name: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    image: this.fb.control<string | null>(null),
    color: this.fb.control<string | null>(null),
  });

  constructor() {
    if (this.id) {
      this.form.controls.stock.disable();   // stock changes via "Add stock"

      this.api.get(this.id).subscribe({
        next: (product) => {
          this.form.patchValue({
            name: product.name,
            price: product.price,
            stock: product.stock,
            image: product.image ?? null,
            color: product.color ?? null,
          });
        },
        error: (err) => {
          this.error.set(errorMessage(err));
        },
      });
    }
  }

  async onFile(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';                        // lets the same file be picked again

    if (!file) return;
    this.imageError.set('');

    if (!file.type.startsWith('image/')) {
      this.imageError.set('Please choose an image file');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.imageError.set('Image must be under 5 MB');
      return;
    }

    try {
      this.form.controls.image.setValue(await fileToResizedDataUrl(file));
    } catch (e) {
      this.imageError.set((e as Error).message);
    }
  }

  removeImage(): void {
    this.form.controls.image.setValue(null);
  }

  addColor(): void {
    this.form.controls.color.setValue('#4f46e5');
  }

  onColor(event: Event): void {
    this.form.controls.color.setValue((event.target as HTMLInputElement).value);
  }

  clearColor(): void {
    this.form.controls.color.setValue(null);
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
        this.snack.open('Saved', 'OK', { duration: 2000 });
        this.router.navigate(['/products']);
      },

      error: (err) => {
        this.error.set(errorMessage(err));
      },
    });
  }
}