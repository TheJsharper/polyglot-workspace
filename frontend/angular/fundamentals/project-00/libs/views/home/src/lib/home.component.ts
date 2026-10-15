import { ChangeDetectionStrategy, Component } from '@angular/core';
import { HOME_STATS } from '@project-00/services-core';
import { PageHeaderComponent } from '@project-00/shared-ui';
import { StatCardComponent } from '@project-00/widgets-ui';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeaderComponent, StatCardComponent],
  template: `
    <app-page-header title="Home" subtitle="Lazy-loaded from the views-home library" />
    <section class="grid">
      @for (stat of stats; track stat.label) {
        <app-stat-card [label]="stat.label" [value]="stat.value" [icon]="stat.icon" />
      }
    </section>
  `,
  styles: `
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 16px;
    }
  `,
})
export class HomeComponent {
  protected readonly stats = HOME_STATS;
}
