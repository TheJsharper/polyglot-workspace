import { ChangeDetectionStrategy, Component, Injectable, computed, effect, inject } from '@angular/core';
import { provideIcons } from '@ng-icons/core';
import { lucideMoon, lucideSun, lucideSunMoon } from '@ng-icons/lucide';
import { Pressable } from '@ng-native/components';
import { ColorScheme } from '@ng-native/device';
import { Storage } from '@ng-native/expo/async-storage';
import { NgIcon } from '@ng-native/icons';

export type ThemeChoice = 'system' | 'light' | 'dark';

const ORDER: readonly ThemeChoice[] = ['system', 'light', 'dark'];
const LABELS: Record<ThemeChoice, string> = { system: 'System', light: 'Light', dark: 'Dark' };

/** The user's appearance choice, saved across launches and applied to the whole app through `ColorScheme`. */
@Injectable({ providedIn: 'root' })
export class ThemePreference {
  private readonly stored = inject(Storage).signal<ThemeChoice>('theme-choice', 'system');
  /** A value written by another app version falls back to following the system. */
  readonly choice = computed<ThemeChoice>(() => (ORDER.includes(this.stored()) ? this.stored() : 'system'));
  readonly label = computed(() => LABELS[this.choice()]);

  constructor() {
    const scheme = inject(ColorScheme);
    effect(() => {
      const choice = this.choice();
      scheme.set(choice === 'system' ? null : choice);
    });
  }

  cycle(): void {
    this.stored.set(ORDER[(ORDER.indexOf(this.choice()) + 1) % ORDER.length]);
  }
}

/** Header button cycling System → Light → Dark. */
@Component({
  selector: 'app-theme-toggle',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon, Pressable],
  viewProviders: [provideIcons({ lucideMoon, lucideSun, lucideSunMoon })],
  template: `
    <pressable
      accessibilityRole="button"
      class="toggle"
      [accessibilityLabel]="'Theme: ' + theme.label() + '. Change theme'"
      [hitSlop]="6"
      (press)="theme.cycle()"
    >
      <ng-icon [name]="icon()" [size]="22" color="var(--primary)" [strokeWidth]="2" />
    </pressable>
  `,
  styles: `
    .toggle { width: 40px; height: 40px; justify-content: center; align-items: center; }
  `,
})
export class ThemeToggle {
  protected readonly theme = inject(ThemePreference);
  protected readonly icon = computed(() =>
    ({ system: 'lucideSunMoon', light: 'lucideSun', dark: 'lucideMoon' })[this.theme.choice()],
  );
}
