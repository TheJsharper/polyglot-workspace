import { ChangeDetectionStrategy, Component } from '@angular/core';
import { PageHeaderComponent } from '@project-00/shared-ui';
import { StatCardComponent } from '@project-00/widgets-ui';

@Component({
  selector: 'app-home',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeaderComponent, StatCardComponent],
  template: `
    <app-page-header title="Home" subtitle="Lazy-loaded from the views-home library" />
    <section class="grid">
      <app-stat-card label="Libraries" value="9" icon="folder" />
      <app-stat-card label="Remotes" value="1" icon="hub" />
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
export class HomeComponent {}
