import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Store } from '@ngrx/store';
import { MainLayoutComponent } from '@project-00/layouts-main-layout';
import { LayoutActions, selectMenuItems, selectSidenavOpened, selectTheme } from '@project-00/services-state';

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
      (menuToggle)="toggleSidenav()"
      (themeToggle)="toggleTheme()"
      (sidenavOpenedChange)="setSidenav($event)"
    >
      <router-outlet />
    </app-main-layout>
  `,
})
export class ShellComponent {
  private readonly store = inject(Store);

  readonly appTitle = input('Web App');

  protected readonly menuItems = this.store.selectSignal(selectMenuItems);
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
}
