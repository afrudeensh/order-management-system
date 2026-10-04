import { DatePipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';

@Component({
  selector: 'app-live-clock',
  imports: [DatePipe],
  template: `
    <div class="clock">
      <span class="clock-date">{{ now() | date: 'EEEE, d MMM y' }}</span>
      <span class="clock-time">{{ now() | date: 'h:mm:ss a' }}</span>
    </div>
  `,
  styles: [`
    .clock { text-align: right; line-height: 1.3; }
    .clock-date { display: block; font-size: 13px; color: #6b7280; }
    .clock-time {
      display: block;
      font-size: 20px;
      font-weight: 600;
      font-variant-numeric: tabular-nums;   /* digits keep the same width, so no jitter */
    }
  `],
})
export class LiveClockComponent {
  now = signal(new Date());

  constructor() {
    const timer = setInterval(() => this.now.set(new Date()), 1000);
    inject(DestroyRef).onDestroy(() => clearInterval(timer));
  }
}