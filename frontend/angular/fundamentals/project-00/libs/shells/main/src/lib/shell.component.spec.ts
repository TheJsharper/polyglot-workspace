import { TestBed } from '@angular/core/testing';
import { Store } from '@ngrx/store';
import { selectSidenavOpened, selectTheme } from '@project-00/services-state';
import { UniversitySession } from '@project-00/services-session';
import { SHELL_MENU, menuForRole } from './shell.menu';
import { provideShell } from './provide-shell';
import { ShellComponent } from './shell.component';

describe('ShellComponent', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideShell()] });
  });

  it('renders the layout with the shell menu from the store', async () => {
    TestBed.overrideProvider(UniversitySession, { useValue: { user: () => ({ username: 'a', role: 'ADMIN' }), authenticated: () => true } });
    const fixture = TestBed.createComponent(ShellComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const labels = Array.from(el.querySelectorAll('[matListItemTitle]')).map((n) => n.textContent);
    expect(labels).toEqual(['Students', 'Professors', 'Courses', 'Lectures', 'Notes & reviews']);
    expect(el.querySelector('router-outlet')).not.toBeNull();
  });

  it('filters the menu by role', () => {
    const ids = (role: string) => menuForRole(SHELL_MENU, role).map((i) => i.id);
    expect(ids('STUDENT')).toEqual(['courses', 'lectures', 'notes']);
    expect(ids('PROFESSOR')).toEqual(['students', 'courses', 'lectures', 'notes']);
    expect(menuForRole(SHELL_MENU, 'PROFESSOR')[0].label).toBe('My Students');
    expect(ids('ADMIN')).toHaveLength(5);
    expect(ids('')).toEqual([]);
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
