import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-stat-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatCardModule, MatIconModule],
  template: `
    <mat-card appearance="outlined" class="stat-card">
      <mat-card-content>
        <mat-icon aria-hidden="true">{{ icon() }}</mat-icon>
        <div>
          <span class="label">{{ label() }}</span>
          <span class="value">{{ value() }}</span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: `
    mat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }
    mat-icon {
      color: var(--mat-sys-primary);
    }
    .label {
      display: block;
      color: var(--mat-sys-on-surface-variant);
      font: var(--mat-sys-label-large);
    }
    .value {
      display: block;
      font: var(--mat-sys-headline-small);
    }
  `,
})
export class StatCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<string | number>();
  readonly icon = input('insights');
}
