import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PageHeaderComponent } from '@project-00/shared-ui';
import { DASHBOARD_STATS } from '@project-00/services-core';
import { StatCardComponent } from '@project-00/widgets-ui';

@Component({
  selector: 'app-dashboard',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeaderComponent, StatCardComponent],
  template: `
    <app-page-header title="Dashboard" subtitle="Served by the dashboard_remote federated app" />
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
export class DashboardComponent {
  protected readonly stats = DASHBOARD_STATS;
}
