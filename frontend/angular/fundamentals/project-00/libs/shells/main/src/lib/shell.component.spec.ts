import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { selectSidenavOpened, selectTheme } from '@project-00/services-state';
import { provideShell } from './provide-shell';
import { ShellComponent } from './shell.component';

vi.mock('dashboard_remote/Routes', () => ({ remoteRoutes: [] }));

describe('ShellComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideShell()] });
  });

  it('renders the layout with the shell menu from the store', async () => {
    const fixture = TestBed.createComponent(ShellComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const labels = Array.from(el.querySelectorAll('[matListItemTitle]')).map((n) => n.textContent);
    expect(labels).toEqual(['Home', 'Dashboard', 'University']);
    expect(el.querySelector('router-outlet')).not.toBeNull();
  });

  it('dispatches layout actions from header buttons', async () => {
    const fixture = TestBed.createComponent(ShellComponent);
    await fixture.whenStable();
    const store = TestBed.inject(Store);
    const el = fixture.nativeElement as HTMLElement;

    el.querySelector<HTMLButtonElement>('[data-testid="theme-toggle"]')?.click();
    expect(store.selectSignal(selectTheme)()).toBe('dark');

    el.querySelector<HTMLButtonElement>('[data-testid="menu-toggle"]')?.click();
    expect(store.selectSignal(selectSidenavOpened)()).toBe(false);
  });
});
