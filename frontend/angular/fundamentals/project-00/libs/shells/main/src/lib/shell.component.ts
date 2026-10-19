import { ChangeDetectionStrategy, Component, effect, inject, input } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { MainLayoutComponent } from '@project-00/layouts-main-layout';
import { LayoutActions, selectMenuItems, selectSidenavOpened, selectTheme } from '@project-00/services-state';
import { UniversitySession } from '@project-00/services-session';
import { selectUsername } from '@project-00/services-university-state';

@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MainLayoutComponent, RouterOutlet],
  template: `
    <app-main-layout
      [appTitle]="appTitle()"
      [menuItems]="menuItems()"
      [sidenavOpened]="sidenavOpened()"
      [theme]="theme()"
      [username]="username() || null"
      (menuToggle)="toggleSidenav()"
      (themeToggle)="toggleTheme()"
      (authAction)="authAction()"
      (sidenavOpenedChange)="setSidenav($event)"
    >
      <router-outlet />
    </app-main-layout>
  `,
})
export class ShellComponent {
  private readonly store = inject(Store);
  private readonly router = inject(Router);
  protected readonly session = inject(UniversitySession);

  constructor() {
    effect(() => {
      if (!this.session.authenticated() && this.router.url.startsWith('/university')) {
        void this.router.navigateByUrl('/login');
      }
    });
  }

  readonly appTitle = input('University');

  protected readonly menuItems = this.store.selectSignal(selectMenuItems);
  protected readonly username = this.store.selectSignal(selectUsername);
  protected readonly sidenavOpened = this.store.selectSignal(selectSidenavOpened);
  protected readonly theme = this.store.selectSignal(selectTheme);

  protected toggleSidenav(): void {
    this.store.dispatch(LayoutActions.toggleSidenav());
  }

  protected toggleTheme(): void {
    this.store.dispatch(LayoutActions.toggleTheme());
  }

  protected setSidenav(opened: boolean): void {
    if (opened !== this.sidenavOpened()) {
      this.store.dispatch(LayoutActions.setSidenav({ opened }));
    }
  }

  protected authAction(): void {
    if (this.session.authenticated()) {
      this.session.logout();
    }
    void this.router.navigateByUrl('/login');
  }
}
