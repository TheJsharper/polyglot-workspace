import { TestBed } from '@angular/core/testing';
import { UniversitySession } from '@project-00/services-session';
import type { DemoKeyResponse } from '@project-00/services-api';
import { DEMO_KEY_POLL_MS, DemoKeysComponent } from './demo-keys.component';

const key = (id: number, redeemed: boolean): DemoKeyResponse => ({
  id,
  label: `recruiter-${id}`,
  publicKey: 'pk',
  token: `token-${id}`,
  createdAt: '2026-10-21T10:00:00Z',
  notBefore: '2026-10-21T10:00:00Z',
  expiresAt: '2999-10-31T10:00:00Z',
  redeemed,
  redeemedAt: redeemed ? '2026-10-21T11:00:00Z' : null,
  redeemCount: redeemed ? 1 : 0,
  createdBy: 'manager',
});

async function setup(initial: DemoKeyResponse[] = [key(1, false), key(2, true)]) {
  const api = {
    listDemoKeys: vi.fn().mockResolvedValue(initial),
    createDemoKeys: vi.fn().mockResolvedValue({ id: 'job-1', status: 'PENDING', keys: [], createdAt: '2026-10-21T10:00:00Z' }),
    getDemoKeyJob: vi
      .fn()
      .mockResolvedValueOnce({ id: 'job-1', status: 'RUNNING', keys: [], createdAt: '2026-10-21T10:00:00Z' })
      .mockResolvedValue({ id: 'job-1', status: 'DONE', keys: [key(3, false)], createdAt: '2026-10-21T10:00:00Z' }),
    deleteDemoKey: vi.fn().mockResolvedValue(undefined),
  };
  TestBed.configureTestingModule({
    providers: [
      { provide: DEMO_KEY_POLL_MS, useValue: 0 },
      { provide: UniversitySession, useValue: { api, run: (fn: () => unknown) => fn(), logout: vi.fn() } },
    ],
  });
  const fixture = TestBed.createComponent(DemoKeysComponent);
  await fixture.whenStable();
  return { fixture, api, root: fixture.nativeElement as HTMLElement };
}

function type(root: HTMLElement, name: string, value: string): void {
  const input = root.querySelector<HTMLInputElement>(`input[name=${name}]`)!;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}

describe('DemoKeysComponent', () => {
  it('lists keys and offers delete only for unused ones', async () => {
    const { root } = await setup();

    const rows = root.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0].textContent).toContain('Unused');
    expect(rows[0].textContent).toContain('Delete');
    expect(rows[1].textContent).toContain('Redeemed');
    expect(rows[1].textContent).not.toContain('Delete');
  });

  it('creates keys with the entered count', async () => {
    const { fixture, api, root } = await setup();
    type(root, 'count', '4');
    type(root, 'label', ' Acme ');
    await fixture.whenStable();

    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();

    expect(api.createDemoKeys).toHaveBeenCalledWith({ count: 4, days: 10, label: 'Acme' });
    await vi.waitFor(() => expect(api.getDemoKeyJob).toHaveBeenCalledTimes(2));
    await vi.waitFor(() => expect(api.listDemoKeys).toHaveBeenCalledTimes(2));
    await fixture.whenStable();
    expect(root.querySelector('[role=status]')?.textContent).toContain('Created 1 demo key');
  });

  it('shows a failed job and the service-unavailable error', async () => {
    const { fixture, api, root } = await setup();
    api.getDemoKeyJob.mockReset().mockResolvedValue({ id: 'job-1', status: 'FAILED', keys: [], error: 'Key generation failed', createdAt: 'x' });

    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(root.querySelector('[role=alert]')?.textContent).toContain('Key generation failed'));
    await fixture.whenStable();

    api.createDemoKeys.mockRejectedValue(new Error('Demo key service unavailable'));
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await vi.waitFor(() => expect(root.querySelector('[role=alert]')?.textContent).toContain('unavailable'));
  });

  it.each(['0', '11'])('blocks a count of %s', async (value) => {
    const { fixture, api, root } = await setup();
    type(root, 'count', value);
    await fixture.whenStable();

    expect(root.querySelector<HTMLButtonElement>('button[type=submit]')!.disabled).toBe(true);
    expect(root.querySelector('.hint')).not.toBeNull();
    root.querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await fixture.whenStable();
    expect(api.createDemoKeys).not.toHaveBeenCalled();
  });

  it('deletes an unused key', async () => {
    const { fixture, api, root } = await setup();

    root.querySelector<HTMLButtonElement>('button.danger')!.click();
    await fixture.whenStable();

    expect(api.deleteDemoKey).toHaveBeenCalledWith(1);
  });

  it('shows the failure when the server refuses', async () => {
    const { fixture, api, root } = await setup();
    api.deleteDemoKey.mockRejectedValue(new Error('A redeemed demo key cannot be deleted'));

    root.querySelector<HTMLButtonElement>('button.danger')!.click();
    await fixture.whenStable();

    expect(root.querySelector('[role=alert]')?.textContent).toContain('cannot be deleted');
  });
});
