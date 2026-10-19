import { signal, Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';
import type { MeResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { provideUniversityAuth, provideUniversityState } from '@project-00/services-university-state';

export type ApiMock = Record<string, ReturnType<typeof vi.fn>>;

/** Lets promise-based effects finish and re-renders the OnPush page. */
export async function settle(fixture: ComponentFixture<unknown>): Promise<void> {
  for (let i = 0; i < 4; i++) {
    await new Promise((resolve) => setTimeout(resolve));
    fixture.detectChanges();
    await fixture.whenStable();
  }
}

/** Renders a remote page against a real NgRx store and a mocked `UniversitySession`. */
export async function renderPage<T>(page: Type<T>, user: MeResponse, api: ApiMock): Promise<ComponentFixture<T>> {
  TestBed.configureTestingModule({
    providers: [
      provideStore(),
      provideEffects(),
      provideUniversityAuth(),
      provideUniversityState(),
      {
        provide: UniversitySession,
        useValue: { api, user: signal(user), run: (action: () => Promise<unknown>) => action(), logout: vi.fn() },
      },
    ],
  });
  const fixture = TestBed.createComponent(page);
  await settle(fixture as ComponentFixture<unknown>);
  return fixture;
}

export function type(root: HTMLElement, selector: string, value: string): void {
  const input = root.querySelector<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(selector);
  if (!input) throw new Error(`No element matches ${selector}`);
  input.value = value;
  input.dispatchEvent(new Event(input instanceof HTMLSelectElement ? 'change' : 'input'));
}
