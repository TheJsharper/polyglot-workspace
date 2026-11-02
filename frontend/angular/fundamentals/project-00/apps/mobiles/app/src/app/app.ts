import { Component, inject } from '@angular/core';
import { SafeAreaProvider } from '@ng-native/components';
import { StatusBar } from '@ng-native/device';
import { NativeStackOutlet } from '@ng-native/router';
import { ThemePreference } from './theme-preference.ts';

@Component({
  imports: [NativeStackOutlet, SafeAreaProvider],
  selector: 'app-root',
  template: '<safe-area-provider><native-stack-outlet /></safe-area-provider>',
  // Theme tokens (mirrored in theme.ts for native headers) cascade into every screen and follow the system scheme.
  styles: `
    :host {
      flex: 1;
      background-color: var(--bg);
      --bg: #f4f5f8;
      --surface: #ffffff;
      --raised: #e8ecf4;
      --border: #d8dde6;
      --text: #172033;
      --muted: #596579;
      --primary: #2f5bea;
      --on-primary: #ffffff;
      --danger: #c62828;
      --on-danger: #ffffff;
      --error-bg: #fdecea;
      --error-fg: #b71c1c;
      --success-bg: #e6f4ea;
      --success-fg: #1b5e20;
      --backdrop: rgba(16, 24, 40, 0.48);
    }
    @media (prefers-color-scheme: dark) {
      :host {
        --bg: #121826;
        --surface: #1a2233;
        --raised: #232d42;
        --border: #2f3a52;
        --text: #e6e9f2;
        --muted: #a0abc2;
        --primary: #8fa8ff;
        --on-primary: #0e1630;
        --danger: #ff8f8f;
        --on-danger: #2a0f12;
        --error-bg: #3a1d24;
        --error-fg: #ffb4ab;
        --success-bg: #16301f;
        --success-fg: #8fd9a8;
        --backdrop: rgba(4, 7, 14, 0.64);
      }
    }
  `,
})
export class App {
  constructor() {
    inject(StatusBar).set({ style: 'auto' });
    // Applies the saved System/Light/Dark choice at startup.
    inject(ThemePreference);
  }
}
