import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Store } from '@ngrx/store';
import { firstValueFrom } from 'rxjs';
import { LayoutActions, selectTheme } from './layout/layout.feature';
import { MENU_CONFIG } from './navigation/menu-config.token';
import { selectMenuItems } from './navigation/navigation.feature';
import { provideAppState } from './provide-app-state';

describe('provideAppState', () => {
  const menuItems = [{ id: 'home', label: 'Home', icon: 'home', route: '/' }];

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.classList.remove('dark-theme');
  });

  function setup() {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideAppState(), { provide: MENU_CONFIG, useValue: menuItems }],
    });
    return TestBed.inject(Store);
  }

  it('loads the configured menu on init', async () => {
    const store = setup();
    expect(await firstValueFrom(store.select(selectMenuItems))).toEqual(menuItems);
  });

  it('restores a persisted theme', async () => {
    localStorage.setItem('app.theme', 'dark');
    const store = setup();
    expect(await firstValueFrom(store.select(selectTheme))).toBe('dark');
    expect(document.documentElement.classList.contains('dark-theme')).toBe(true);
  });

  it('applies and persists theme changes', () => {
    const store = setup();
    store.dispatch(LayoutActions.toggleTheme());
    expect(document.documentElement.classList.contains('dark-theme')).toBe(true);
    expect(localStorage.getItem('app.theme')).toBe('dark');
  });
});
