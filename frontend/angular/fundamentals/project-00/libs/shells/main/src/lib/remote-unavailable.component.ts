import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/** Rendered in place of a remote whose entry could not be loaded (e.g. its server is down). */
@Component({
  selector: 'app-remote-unavailable',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <section class="unavailable" role="alert">
      <h1>{{ remote() }} is unavailable</h1>
      <p>This section could not be loaded. Check that its micro-frontend is running, then reload the page.</p>
    </section>
  `,
  styles: [`
    .unavailable { max-width: 40rem; margin: 2rem auto; padding: 1.5rem; border-radius: .75rem; background: #fff0f0; color: #8b1d1d; }
    h1 { margin-top: 0; font-size: 1.4rem; text-transform: capitalize; }
  `],
})
export class RemoteUnavailableComponent {
  readonly remote = input('This section');
}
