import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { LoginComponent, RegisterComponent } from './auth.component';

@Component({ template: '' })
class EmptyPage {}

describe('University authentication', () => {
  it('logs in and redirects to the university', async () => {
    const login = vi.fn().mockResolvedValue({ username: 'S-1', role: 'STUDENT' });
    TestBed.configureTestingModule({
      providers: [
        provideRouter([{ path: 'university', component: EmptyPage }]),
        { provide: UniversitySession, useValue: { login } },
      ],
    });
    const fixture = TestBed.createComponent(LoginComponent);
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    const username = root.querySelector<HTMLInputElement>('input[name=username]')!;
    username.value = 'S-1';
    username.dispatchEvent(new Event('input'));
    const password = root.querySelector<HTMLInputElement>('input[name=password]')!;
    password.value = 'test-password';
    password.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();

    expect(login).toHaveBeenCalledWith({ username: 'S-1', password: 'test-password' });
  });

  it('shows a registration error instead of reporting success', async () => {
    const register = vi.fn().mockRejectedValue(new Error('Account already exists'));
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: UniversitySession, useValue: { register, user: () => null, authenticated: () => false } },
      ],
    });
    const fixture = TestBed.createComponent(RegisterComponent);
    await fixture.whenStable();
    const root = fixture.nativeElement as HTMLElement;
    for (const [name, value] of Object.entries({ id: 'S-1', name: 'Student', password: 'password-123' })) {
      const input = root.querySelector<HTMLInputElement>(`input[name=${name}]`)!;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    await fixture.whenStable();
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();

    expect(register).toHaveBeenCalledOnce();
    expect(root.querySelector('[role=alert]')?.textContent).toContain('Account already exists');
    expect(root.querySelector('[role=status]')).toBeNull();
  });
});
