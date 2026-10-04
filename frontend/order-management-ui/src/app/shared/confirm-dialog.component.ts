import { Component, Injectable, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogModule,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Observable, map } from 'rxjs';

export interface ConfirmData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

@Component({
  selector: 'app-confirm-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    <div class="confirm">
      <div class="icon" [class.danger]="data.danger">
        <mat-icon>{{ data.danger ? 'warning' : 'help_outline' }}</mat-icon>
      </div>

      <h2 mat-dialog-title>{{ data.title }}</h2>

      <mat-dialog-content>{{ data.message }}</mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button [mat-dialog-close]="false">
          {{ data.cancelText ?? 'Cancel' }}
        </button>

        <button
          mat-flat-button
          class="ok"
          [class.danger]="data.danger"
          [mat-dialog-close]="true"
          cdkFocusInitial
        >
          {{ data.confirmText ?? 'Confirm' }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirm { padding: 8px 8px 0; }
    .icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: grid;
      place-items: center;
      margin: 8px 0 4px 24px;
      background: #e0e7ff;
      color: #4f46e5;
    }
    .icon.danger { background: #fee2e2; color: #dc2626; }
    .ok.danger {
      --mat-button-filled-container-color: #dc2626;
      --mat-button-filled-label-text-color: #fff;
    }
  `],
})
export class ConfirmDialogComponent {
  data = inject<ConfirmData>(MAT_DIALOG_DATA);
}

@Injectable({ providedIn: 'root' })
export class ConfirmService {
  private dialog = inject(MatDialog);

  /** Emits true when the user confirms, false otherwise. */
  ask(data: ConfirmData): Observable<boolean> {
    return this.dialog
      .open(ConfirmDialogComponent, {
        data,
        width: '420px',
        maxWidth: '92vw',
      })
      .afterClosed()
      .pipe(map((result) => result === true));
  }
}