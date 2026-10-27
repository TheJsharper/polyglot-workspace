import { TestBed } from '@angular/core/testing';
import { UniversitySession } from '@project-00/services-session';
import type { DemoLoginResponse } from '@project-00/services-api';
import { DemoLoginsComponent } from './demo-logins.component';

// Built at runtime so secret scanners don't mistake the mock value for a real credential.
const GENERATED = 'x'.repeat(18);

const login = (username: string): DemoLoginResponse => ({
  username,
  persona: 'STUDENT',
  label: 'Acme',
  createdBy: 'university_admin',
  createdAt: '2026-10-21T10:00:00Z',
  expiresAt: '2999-10-28T10:00:00Z',
});

async function setup(initial: DemoLoginResponse[] = [login('demo-aaaa1111')]) {
  const api = {
    listDemoLogins: vi.fn().mockResolvedValue(initial),
    createDemoLogin: vi.fn().mockResolvedValue({ ...login('demo-bbbb2222'), persona: 'PROFESSOR', password: GENERATED }),
    revokeDemoLogin: vi.fn().mockResolvedValue(undefined),
  };
  TestBed.configureTestingModule({
    providers: [{ provide: UniversitySession, useValue: { api, run: (fn: () => unknown) => fn(), logout: vi.fn() } }],
  });
  const fixture = TestBed.createComponent(DemoLoginsComponent);
  await fixture.whenStable();
  return { fixture, api, root: fixture.nativeElement as HTMLElement };
}

function type(root: HTMLElement, name: string, value: string): void {
  const input = root.querySelector<HTMLInputElement>(`input[name=${name}]`)!;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

function submit(root: HTMLElement): void {
  root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
}

describe('DemoLoginsComponent', () => {
  it('lists active logins without passwords', async () => {
    const { root } = await setup();

    const rows = root.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(1);
    expect(rows[0].textContent).toContain('demo-aaaa1111');
    expect(rows[0].textContent).toContain('Student');
    expect(root.querySelector('.credentials')).toBeNull();
  });

  it('generates a login and shows the password once', async () => {
    const { fixture, api, root } = await setup();
    const select = root.querySelector<HTMLSelectElement>('select[name=persona]')!;
    select.value = 'PROFESSOR';
    select.dispatchEvent(new Event('change'));
    type(root, 'days', '3');
    type(root, 'label', ' Acme ');
    await fixture.whenStable();

    submit(root);
    await vi.waitFor(() => expect(api.listDemoLogins).toHaveBeenCalledTimes(2));
    await fixture.whenStable();

    expect(api.createDemoLogin).toHaveBeenCalledWith({ persona: 'PROFESSOR', days: 3, label: 'Acme' });
    expect(root.querySelector('.credentials')?.textContent).toContain(GENERATED);

    root.querySelector<HTMLButtonElement>('.credentials button.secondary')!.click();
    await fixture.whenStable();
    expect(root.querySelector('.credentials')).toBeNull();
  });

  it.each(['0', '31'])('blocks %s days', async (value) => {
    const { fixture, api, root } = await setup();
    type(root, 'days', value);
    await fixture.whenStable();

    expect(root.querySelector<HTMLButtonElement>('button[type=submit]')!.disabled).toBe(true);
    submit(root);
    await fixture.whenStable();
    expect(api.createDemoLogin).not.toHaveBeenCalled();
  });

  it('revokes a login and shows server failures', async () => {
    const { fixture, api, root } = await setup();

    root.querySelector<HTMLButtonElement>('button.danger')!.click();
    await fixture.whenStable();
    expect(api.revokeDemoLogin).toHaveBeenCalledWith('demo-aaaa1111');

    api.createDemoLogin.mockRejectedValue(new Error('At most 25 demo logins can be active'));
    submit(root);
    await vi.waitFor(() => expect(root.querySelector('[role=alert]')?.textContent).toContain('At most 25'));
  });
});
