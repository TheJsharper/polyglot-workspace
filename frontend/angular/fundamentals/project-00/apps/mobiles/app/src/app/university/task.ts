import { DestroyRef, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { ApiError } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    const problem = error.problem;
    const fields = problem['errors'];
    const detail = problem.detail || problem.title;
    if (fields && typeof fields === 'object') {
      return [detail, ...Object.values(fields as Record<string, unknown>).map(String)].filter(Boolean).join(': ');
    }
    return detail || `Request failed (${error.status})`;
  }
  if (error instanceof Error && /network|fetch|connect/i.test(error.message)) {
    return 'Cannot reach the University API. Check EXPO_PUBLIC_API_URL and that the server is running.';
  }
  return error instanceof Error ? error.message : 'Something went wrong.';
}

/** Tracks one screen's busy flag and last error; every API call goes through the session so a 401 logs out. */
export class Task {
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);
  private pending = 0;

  constructor(private readonly session: UniversitySession) {}

  async run<T>(action: () => Promise<T>, notice?: string): Promise<T | undefined> {
    this.pending++;
    this.loading.set(true);
    this.error.set(null);
    this.notice.set(null);
    try {
      const result = await this.session.run(action);
      if (notice) {
        this.notice.set(notice);
      }
      return result;
    } catch (error) {
      this.error.set(errorMessage(error));
      return undefined;
    } finally {
      this.pending--;
      this.loading.set(this.pending > 0);
    }
  }
}

export function injectTask(): Task {
  return new Task(inject(UniversitySession));
}

/** Calls `load` again when the router comes back to this screen after it was covered by another one. */
export function reloadOnReturn(load: () => void): void {
  const router = inject(Router);
  const route = inject(ActivatedRoute);
  const subscription = router.events.subscribe((event) => {
    if (!(event instanceof NavigationEnd)) {
      return;
    }
    const mine = '/' + route.snapshot.pathFromRoot.flatMap((r) => r.url.map((s) => s.path)).join('/');
    if (event.urlAfterRedirects.split('?')[0] === mine) {
      load();
    }
  });
  inject(DestroyRef).onDestroy(() => subscription.unsubscribe());
}

export function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export function parseOptionalInt(text: string): number | undefined {
  const value = text.trim();
  return value && /^-?\d+$/.test(value) ? Number(value) : undefined;
}