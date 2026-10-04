import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';

import { AuthService } from '../core/auth.service';
import { errorMessage } from '../core/http-error';

function passwordsMatch(g: AbstractControl): ValidationErrors | null {
  return g.get('newPassword')?.value === g.get('confirmPassword')?.value
    ? null
    : { mismatch: true };
}

@Component({
  selector: 'app-account-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
  ],
  template: `
    <h2 mat-dialog-title>Account settings</h2>

    <mat-dialog-content>
      <mat-tab-group>
        <!-- PROFILE -->
        <mat-tab label="Profile">
          <form class="stack pad" [formGroup]="profile" (ngSubmit)="saveProfile()">
            <mat-form-field appearance="outline">
              <mat-label>Name</mat-label>
              <input matInput formControlName="name" autocomplete="name" />
              <mat-error>Name is required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Email</mat-label>
              <input matInput type="email" formControlName="email" autocomplete="username" />
              <mat-error>Enter a valid email</mat-error>
            </mat-form-field>

            @if (emailChanged()) {
              <mat-form-field appearance="outline">
                <mat-label>Current password</mat-label>
                <input
                  matInput
                  [type]="showPw() ? 'text' : 'password'"
                  formControlName="currentPassword"
                  autocomplete="current-password"
                />
                <button mat-icon-button matSuffix type="button" (click)="showPw.set(!showPw())"
                        [attr.aria-label]="showPw() ? 'Hide password' : 'Show password'">
                  <mat-icon>{{ showPw() ? 'visibility' : 'visibility_off' }}</mat-icon>
                </button>
                <mat-hint>Needed to change your email</mat-hint>
              </mat-form-field>
            }

            @if (profileError()) {
              <p class="error">{{ profileError() }}</p>
            }

            <div class="actions-end">
              <button mat-flat-button type="submit" [disabled]="profile.invalid || savingProfile()">
                {{ savingProfile() ? 'Saving...' : 'Save changes' }}
              </button>
            </div>
          </form>
        </mat-tab>

        <!-- PASSWORD -->
        <mat-tab label="Password">
          <form class="stack pad" [formGroup]="passwordForm" (ngSubmit)="savePassword()">
            <mat-form-field appearance="outline">
              <mat-label>Current password</mat-label>
              <input
                matInput
                [type]="showPw() ? 'text' : 'password'"
                formControlName="currentPassword"
                autocomplete="current-password"
              />
              <button mat-icon-button matSuffix type="button" (click)="showPw.set(!showPw())"
                      [attr.aria-label]="showPw() ? 'Hide password' : 'Show password'">
                <mat-icon>{{ showPw() ? 'visibility' : 'visibility_off' }}</mat-icon>
              </button>
              <mat-error>Required</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>New password</mat-label>
              <input
                matInput
                [type]="showPw() ? 'text' : 'password'"
                formControlName="newPassword"
                autocomplete="new-password"
              />
              <button mat-icon-button matSuffix type="button" (click)="showPw.set(!showPw())"
                      [attr.aria-label]="showPw() ? 'Hide password' : 'Show password'">
                <mat-icon>{{ showPw() ? 'visibility' : 'visibility_off' }}</mat-icon>
              </button>
              <mat-error>Minimum 8 characters</mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Confirm new password</mat-label>
              <input
                matInput
                [type]="showPw() ? 'text' : 'password'"
                formControlName="confirmPassword"
                autocomplete="new-password"
              />
              <button mat-icon-button matSuffix type="button" (click)="showPw.set(!showPw())"
                      [attr.aria-label]="showPw() ? 'Hide password' : 'Show password'">
                <mat-icon>{{ showPw() ? 'visibility' : 'visibility_off' }}</mat-icon>
              </button>
            </mat-form-field>

            @if (passwordForm.hasError('mismatch') && passwordForm.controls.confirmPassword.dirty) {
              <p class="error">Passwords do not match</p>
            }
            @if (passwordError()) {
              <p class="error">{{ passwordError() }}</p>
            }

            <div class="actions-end">
              <button mat-flat-button type="submit" [disabled]="passwordForm.invalid || savingPassword()">
                {{ savingPassword() ? 'Saving...' : 'Change password' }}
              </button>
            </div>
          </form>
        </mat-tab>
      </mat-tab-group>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .pad { padding-top: 24px; }
    .stack mat-form-field { width: 100%; }
    .actions-end { display: flex; justify-content: flex-end; margin-top: 4px; }
    .error { color: #dc2626; margin: 0 0 8px; }
  `],
})
export class AccountDialogComponent {
  private fb = inject(FormBuilder).nonNullable;
  private auth = inject(AuthService);
  private snack = inject(MatSnackBar);
  private ref = inject<MatDialogRef<AccountDialogComponent>>(MatDialogRef);

  showPw = signal(false);

  savingProfile = signal(false);
  profileError = signal('');
  savingPassword = signal(false);
  passwordError = signal('');

  profile = this.fb.group({
    name: [this.auth.user()?.name ?? '', Validators.required],
    email: [this.auth.user()?.email ?? '', [Validators.required, Validators.email]],
    currentPassword: [''],
  });

  passwordForm = this.fb.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required],
    },
    { validators: passwordsMatch },
  );

  emailChanged(): boolean {
    const current = this.auth.user()?.email ?? '';
    return this.profile.controls.email.value.trim().toLowerCase() !== current.toLowerCase();
  }

  saveProfile(): void {
    if (this.profile.invalid) {
      this.profile.markAllAsTouched();
      return;
    }

    const { name, email, currentPassword } = this.profile.getRawValue();
    const changingEmail = this.emailChanged();

    if (changingEmail && !currentPassword) {
      this.profileError.set('Enter your current password to change your email');
      return;
    }

    this.profileError.set('');
    this.savingProfile.set(true);

    this.auth
      .updateProfile({
        name: name.trim(),
        email: email.trim(),
        currentPassword: changingEmail ? currentPassword : null,
      })
      .subscribe({
        next: () => {
          this.snack.open('Profile updated', 'OK', { duration: 2500 });
          this.ref.close();
        },
        error: (err) => {
          this.profileError.set(errorMessage(err));
          this.savingProfile.set(false);
        },
      });
  }

  savePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { currentPassword, newPassword } = this.passwordForm.getRawValue();

    this.passwordError.set('');
    this.savingPassword.set(true);

    this.auth.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.snack.open('Password changed', 'OK', { duration: 2500 });
        this.passwordForm.reset();
        this.savingPassword.set(false);
      },
      error: (err) => {
        this.passwordError.set(errorMessage(err));
        this.savingPassword.set(false);
      },
    });
  }
}