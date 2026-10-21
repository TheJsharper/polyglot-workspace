import { Component, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Action, provideState, provideStore, Store } from '@ngrx/store';
import { UniversitySession } from '@project-00/services-session';
import { provideUniversityAuth, universityFeature, UniversityActions } from '@project-00/services-university-state';
import { UniversityFrameComponent } from './university-frame.component';

@Component({
  imports: [UniversityFrameComponent],
  template: `<app-university-frame resource="courses"><p class="projected">Page content</p></app-university-frame>`,
})
class HostComponent {}

describe('UniversityFrameComponent', () => {
  const logout = vi.fn();
  let dispatched: Action[];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideStore(),
        provideState(universityFeature),
        provideUniversityAuth(),
        { provide: UniversitySession, useValue: { user: signal({ username: 'g-1a2b', role: 'ADMIN', displayName: 'Ada Lovelace' }), logout } },
      ],
    });
    const store = TestBed.inject(Store);
    dispatched = [];
    const dispatch = store.dispatch.bind(store);
    vi.spyOn(store, 'dispatch').mockImplementation(((action: Action) => {
      dispatched.push(action);
      return dispatch(action);
    }) as Store['dispatch']);
  });

  async function render() {
    const fixture = TestBed.createComponent(HostComponent);
    await fixture.whenStable();
    fixture.detectChanges();
    return { fixture, root: fixture.nativeElement as HTMLElement };
  }

  it('renders the shared heading, account block and projected page content', async () => {
    const { root } = await render();
    expect(root.querySelector('h1')?.textContent).toBe('Resources');
    expect(root.querySelector('.account span')?.textContent).toBe('Ada Lovelace · ADMIN');
    expect(root.querySelector('.projected')?.textContent).toBe('Page content');
    expect(dispatched).toContainEqual(UniversityActions.open({ resource: 'courses' }));
  });

  it('dispatches filter and refresh actions and logs out through the session', async () => {
    const { fixture, root } = await render();
    const input = root.querySelector<HTMLInputElement>('input[type="search"]');
    if (!input) throw new Error('missing filter input');
    input.value = 'cs';
    input.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(dispatched).toContainEqual(UniversityActions.setFilter({ resource: 'courses', query: 'cs' }));
    expect(input.value).toBe('cs');

    const buttons = Array.from(root.querySelectorAll<HTMLButtonElement>('button'));
    buttons.find((b) => b.textContent?.trim() === 'Refresh')?.click();
    expect(dispatched).toContainEqual(UniversityActions.load({ resource: 'courses' }));
    buttons.find((b) => b.textContent?.trim() === 'Log out')?.click();
    expect(logout).toHaveBeenCalled();
  });
});
