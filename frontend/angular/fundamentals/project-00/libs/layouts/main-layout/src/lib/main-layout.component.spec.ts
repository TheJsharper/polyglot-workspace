import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { MainLayoutComponent } from './main-layout.component';

describe('MainLayoutComponent', () => {
  async function setup() {
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
    const fixture = TestBed.createComponent(MainLayoutComponent);
    fixture.componentRef.setInput('appTitle', 'University');
    fixture.componentRef.setInput('menuItems', [
      { id: 'university', label: 'University', icon: 'school', route: '/university' },
    ]);
    await fixture.whenStable();
    return fixture;
  }

  it('renders the header title and menu items', async () => {
    const fixture = await setup();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.title')?.textContent).toBe('University');
    const links = Array.from(el.querySelectorAll('a[mat-list-item]'));
    expect(links.map((a) => a.textContent?.trim())).toEqual(['schoolUniversity']);
    expect(links[0].getAttribute('href')).toBe('/university');
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

  it('shows the Google avatar without a referrer and falls back to initials on error', async () => {
    const fixture = await setup();
    fixture.componentRef.setInput('username', 'Ada Lovelace');
    fixture.componentRef.setInput('pictureUrl', 'https://lh3.googleusercontent.com/a/x');
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const img = el.querySelector<HTMLImageElement>('[data-testid="user-avatar"]');
    expect(img?.getAttribute('referrerpolicy')).toBe('no-referrer');
    expect(img?.getAttribute('alt')).toBe('');
    expect(el.querySelector('.user-name')?.textContent).toBe('Ada Lovelace');

    img?.dispatchEvent(new Event('error'));
    await fixture.whenStable();
    expect(el.querySelector('[data-testid="user-avatar"]')).toBeNull();
    expect(el.querySelector('[data-testid="user-initials"]')?.textContent).toBe('AL');
  });

  it('shows the role switcher only when roles are available and emits the chosen role', async () => {
    const fixture = await setup();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[data-testid="role-switcher"]')).toBeNull();

    fixture.componentRef.setInput('roles', ['MANAGER', 'STUDENT', 'PROFESSOR', 'ADMIN']);
    fixture.componentRef.setInput('activeRole', 'MANAGER');
    await fixture.whenStable();
    const select = el.querySelector<HTMLSelectElement>('[data-testid="role-switcher"]');
    expect(select?.options.length).toBe(4);
    expect(select?.value).toBe('MANAGER');

    const changed = vi.fn();
    fixture.componentInstance.roleChange.subscribe(changed);
    select!.value = 'STUDENT';
    select!.dispatchEvent(new Event('change'));
    expect(changed).toHaveBeenCalledWith('STUDENT');
  });

  it('shows initials when there is no picture and nothing when signed out', async () => {
    const fixture = await setup();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('[data-testid="current-user"]')).toBeNull();
    fixture.componentRef.setInput('username', 'g-1a2b');
    await fixture.whenStable();
    expect(el.querySelector('[data-testid="user-avatar"]')).toBeNull();
    expect(el.querySelector('[data-testid="user-initials"]')?.textContent).toBe('G');
  });
});
