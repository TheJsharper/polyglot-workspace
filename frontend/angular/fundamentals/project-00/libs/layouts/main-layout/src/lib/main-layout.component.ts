import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { ChangeDetectionStrategy, Component, computed, inject, input, output, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem, Theme } from '@project-00/services-core';
import { map } from 'rxjs';

@Component({
  selector: 'app-main-layout',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSidenavModule,
    MatToolbarModule,
    MatTooltipModule,
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
})
export class MainLayoutComponent {
  readonly appTitle = input.required<string>();
  readonly menuItems = input<MenuItem[]>([]);
  readonly sidenavOpened = input(true);
  readonly theme = input<Theme>('light');
  /** Name shown in the header; `null` means signed out. */
  readonly username = input<string | null>(null);
  /** Optional avatar URL; falls back to initials when missing or when it fails to load. */
  readonly pictureUrl = input<string | null>(null);

  protected readonly failedPicture = signal<string | null>(null);
  protected readonly initials = computed(() =>
    (this.username() ?? '')
      .split(/\s+/)
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase() ?? '')
      .join('')
  );

  readonly menuToggle = output<void>();
  readonly themeToggle = output<void>();
  readonly authAction = output<void>();
  readonly sidenavOpenedChange = output<boolean>();

  readonly isHandset = toSignal(
    inject(BreakpointObserver)
      .observe(Breakpoints.Handset)
      .pipe(map((result) => result.matches)),
    { initialValue: false }
  );

  onNavigate(): void {
    if (this.isHandset()) {
      this.sidenavOpenedChange.emit(false);
    }
  }
}
