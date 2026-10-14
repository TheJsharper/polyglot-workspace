import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MainLayoutComponent } from './main-layout.component';

describe('MainLayoutComponent', () => {
  async function setup() {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.componentRef.setInput('appTitle', 'Web App');
    fixture.componentRef.setInput('menuItems', [
      { id: 'home', label: 'Home', icon: 'home', route: '/' },
      { id: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    ]);
    await fixture.whenStable();
    return fixture;
  }

  it('renders the header title and menu items', async () => {
    const fixture = await setup();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.title')?.textContent).toBe('Web App');
    const links = Array.from(el.querySelectorAll('a[mat-list-item]'));
    expect(links.map((a) => a.textContent?.trim())).toEqual(['homeHome', 'dashboardDashboard']);
    expect(links[1].getAttribute('href')).toBe('/dashboard');
  });

  it('emits menu and theme toggles', async () => {
    const fixture = await setup();
    const menu = vi.fn();
    const theme = vi.fn();
    fixture.componentInstance.menuToggle.subscribe(menu);
    fixture.componentInstance.themeToggle.subscribe(theme);
    const el = fixture.nativeElement as HTMLElement;
    el.querySelector<HTMLButtonElement>('[data-testid="menu-toggle"]')?.click();
    el.querySelector<HTMLButtonElement>('[data-testid="theme-toggle"]')?.click();
    expect(menu).toHaveBeenCalledTimes(1);
    expect(theme).toHaveBeenCalledTimes(1);
  });
});
