import { TestBed } from '@angular/core/testing';
import { StatCardComponent } from './stat-card.component';

describe('StatCardComponent', () => {
  it('renders label, value and icon', async () => {
    const fixture = TestBed.createComponent(StatCardComponent);
    fixture.componentRef.setInput('label', 'Users');
    fixture.componentRef.setInput('value', 42);
    fixture.componentRef.setInput('icon', 'group');
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.label')?.textContent).toBe('Users');
    expect(el.querySelector('.value')?.textContent).toBe('42');
    expect(el.querySelector('mat-icon')?.textContent).toBe('group');
  });
});
