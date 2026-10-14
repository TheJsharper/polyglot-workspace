import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-dashboard-remote-entry',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet],
  template: '<main class="standalone"><router-outlet /></main>',
  styles: '.standalone { padding: 24px; }',
})
export class RemoteEntry {}
