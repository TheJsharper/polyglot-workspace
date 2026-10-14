import { TestBed } from '@angular/core/testing';
import { PageHeaderComponent } from './page-header.component';

describe('PageHeaderComponent', () => {
  it('renders title and optional subtitle', async () => {
    const fixture = TestBed.createComponent(PageHeaderComponent);
    fixture.componentRef.setInput('title', 'Home');
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('h1')?.textContent).toBe('Home');
    expect(el.querySelector('p')).toBeNull();

    fixture.componentRef.setInput('subtitle', 'Welcome');
    await fixture.whenStable();
    expect(el.querySelector('p')?.textContent).toBe('Welcome');
  });
});
