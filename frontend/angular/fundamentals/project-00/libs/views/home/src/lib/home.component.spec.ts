import { TestBed } from '@angular/core/testing';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('renders the page header and stat cards', async () => {
    const fixture = TestBed.createComponent(HomeComponent);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('h1')?.textContent).toBe('Home');
    expect(el.querySelectorAll('app-stat-card').length).toBe(2);
  });
});
