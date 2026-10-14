import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PageHeaderComponent } from '@project-00/shared-ui';
import { StatCardComponent } from '@project-00/widgets-ui';

interface DashboardStat {
  label: string;
  value: string;
  icon: string;
}

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
  protected readonly stats: DashboardStat[] = [
    { label: 'Active users', value: '1,284', icon: 'group' },
    { label: 'Revenue', value: '$48.2k', icon: 'payments' },
    { label: 'Orders', value: '326', icon: 'shopping_cart' },
    { label: 'Uptime', value: '99.98%', icon: 'monitor_heart' },
  ];
}
