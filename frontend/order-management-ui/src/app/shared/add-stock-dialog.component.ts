import { Component, inject } from '@angular/core';
import {
  AbstractControl,
  FormControl,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface AddStockData {
  name: string;
  current: number;
}

@Component({
  selector: 'app-add-stock-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  template: `
    <h2 mat-dialog-title>Add stock</h2>

    <mat-dialog-content>
      <p class="muted">
        {{ data.name }} currently has <strong>{{ data.current }}</strong> in stock.
      </p>

      <mat-form-field appearance="outline" style="width: 100%">
        <mat-label>Quantity to add</mat-label>
        <input
          matInput
          type="number"
          min="1"
          [formControl]="qty"
          (keyup.enter)="save()"
          cdkFocusInitial
        />
        <mat-error>Enter a whole number of at least 1</mat-error>
      </mat-form-field>

      @if (qty.valid) {
        <p class="muted">New stock will be {{ data.current + qty.value! }}.</p>
      }
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button [mat-dialog-close]="null">Cancel</button>
      <button mat-flat-button [disabled]="qty.invalid" (click)="save()">
        Add stock
      </button>
    </mat-dialog-actions>
  `,
})
export class AddStockDialogComponent {
  data = inject<AddStockData>(MAT_DIALOG_DATA);
  private ref = inject<MatDialogRef<AddStockDialogComponent, number>>(MatDialogRef);

  qty = new FormControl<number | null>(null, [
    Validators.required,
    Validators.min(1),
    (c: AbstractControl) =>
      c.value === null || Number.isInteger(c.value) ? null : { integer: true },
  ]);

  save(): void {
    if (this.qty.valid) {
      this.ref.close(this.qty.value!);
    }
  }
}
