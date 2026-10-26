import { ChangeDetectionStrategy, Component, computed, inject, InjectionToken, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UniversitySession } from '@project-00/services-session';
import type { DemoKeyResponse } from '@project-00/services-api';

export const MIN_DEMO_KEYS = 1;
export const MAX_DEMO_KEYS = 10;
const MAX_POLLS = 60;
/** Delay between job status polls; overridable in tests. */
export const DEMO_KEY_POLL_MS = new InjectionToken<number>('DEMO_KEY_POLL_MS', { factory: () => 500 });

/** Manager workspace: list, create (1 to 10 at a time) and delete unused recruiter demo keys. */
@Component({
  selector: 'app-demo-keys',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe],
  template: `
    <section class="keys">
      <header>
        <div>
          <p class="eyebrow">Manager</p>
          <h1>Recruiter demo keys</h1>
          <p>Each key is a one-recruiter read-only demo token. Redeemed keys are kept as a record.</p>
        </div>
      </header>

      <form class="create" (ngSubmit)="create()">
        <label>Number of keys
          <input name="count" type="number" [min]="min" [max]="max" step="1" required [ngModel]="count()"
            (ngModelChange)="count.set($event)" />
        </label>
        <label>Valid for (days)
          <input name="days" type="number" min="1" max="30" step="1" required [ngModel]="days()"
            (ngModelChange)="days.set($event)" />
        </label>
        <label>Label
          <input name="label" maxlength="100" placeholder="Recruiter or company" [ngModel]="label()"
            (ngModelChange)="label.set($event)" />
        </label>
        <button type="submit" [disabled]="busy() || !countValid()">Create keys</button>
        @if (!countValid()) { <p class="hint" role="alert">Enter a whole number from {{ min }} to {{ max }}.</p> }
      </form>

      @if (error()) { <p class="failure" role="alert">{{ error() }}</p> }
      @if (message()) { <p class="success" role="status">{{ message() }}</p> }

      @if (loading()) {
        <p role="status">Loading keys…</p>
      } @else if (keys().length === 0) {
        <p>No demo keys yet.</p>
      } @else {
        <table>
          <caption class="sr-only">Demo keys</caption>
          <thead>
            <tr><th>Label</th><th>Created</th><th>Expires</th><th>Status</th><th>Actions</th></tr>
          </thead>
          <tbody>
            @for (key of keys(); track key.id) {
              <tr>
                <td>{{ key.label }}</td>
                <td>{{ key.createdAt | date: 'short' }}</td>
                <td>{{ key.expiresAt | date: 'short' }}</td>
                <td>{{ status(key) }}</td>
                <td>
                  <button type="button" (click)="copy(key)">Copy token</button>
                  @if (!key.redeemed) {
                    <button type="button" class="danger" [disabled]="busy()" (click)="remove(key)">Delete</button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styles: [`
    .keys { max-width: 64rem; margin: 2rem auto; padding: 0 1.5rem; display: grid; gap: 1.25rem; }
    header { display: flex; justify-content: space-between; align-items: start; gap: 1rem; }
    .eyebrow { margin: 0; text-transform: uppercase; font-size: .75rem; letter-spacing: .08em; }
    .create { display: flex; flex-wrap: wrap; gap: 1rem; align-items: end; }
    label { display: grid; gap: .3rem; }
    input { padding: .6rem; border: 1px solid var(--mat-sys-outline); border-radius: .5rem; background: var(--mat-sys-surface); color: inherit; font: inherit; }
    button { padding: .55rem 1rem; border: 0; border-radius: 999px; background: var(--mat-sys-primary); color: var(--mat-sys-on-primary); font: inherit; font-weight: 600; cursor: pointer; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    button.danger { background: var(--mat-sys-error); color: var(--mat-sys-on-error); }
    table { width: 100%; border-collapse: collapse; }
    th, td { text-align: left; padding: .6rem; border-bottom: 1px solid var(--mat-sys-outline-variant); }
    td button + button { margin-left: .5rem; }
    .failure, .hint { color: var(--mat-sys-error); }
    .success { color: var(--mat-sys-tertiary); }
    .sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); }
  `],
})
export class DemoKeysComponent implements OnInit {
  protected readonly session = inject(UniversitySession);
  private readonly pollMs = inject(DEMO_KEY_POLL_MS);
  protected readonly min = MIN_DEMO_KEYS;
  protected readonly max = MAX_DEMO_KEYS;
  protected readonly keys = signal<DemoKeyResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly message = signal('');
  protected readonly count = signal<number>(1);
  protected readonly days = signal<number>(10);
  protected readonly label = signal('');
  protected readonly countValid = computed(() => {
    const value = this.count();
    return Number.isInteger(value) && value >= MIN_DEMO_KEYS && value <= MAX_DEMO_KEYS;
  });

  ngOnInit(): void {
    void this.refresh();
  }

  protected status(key: DemoKeyResponse): string {
    if (key.redeemed) return `Redeemed${key.redeemCount > 1 ? ` ×${key.redeemCount}` : ''}`;
    return Date.parse(key.expiresAt) <= Date.now() ? 'Expired' : 'Unused';
  }

  protected async create(): Promise<void> {
    if (this.busy() || !this.countValid()) return;
    await this.act(async () => {
      const request = { count: this.count(), days: this.days(), label: this.label().trim() || undefined };
      this.message.set('Generating keys…');
      let job = await this.session.run(() => this.session.api.createDemoKeys(request));
      for (let attempt = 0; job.status === 'PENDING' || job.status === 'RUNNING'; attempt++) {
        if (attempt >= MAX_POLLS) throw new Error('Key generation is taking too long. Refresh the list in a moment.');
        await new Promise((resolve) => setTimeout(resolve, this.pollMs));
        const id = job.id;
        job = await this.session.run(() => this.session.api.getDemoKeyJob(id));
      }
      if (job.status === 'FAILED') throw new Error(job.error || 'Key generation failed.');
      const created = job.keys;
      this.message.set(`Created ${created.length} demo key${created.length === 1 ? '' : 's'}.`);
      await this.reload();
    });
  }

  protected async remove(key: DemoKeyResponse): Promise<void> {
    if (this.busy() || key.redeemed) return;
    await this.act(async () => {
      await this.session.run(() => this.session.api.deleteDemoKey(key.id));
      this.message.set('Demo key deleted.');
      await this.reload();
    });
  }

  protected async copy(key: DemoKeyResponse): Promise<void> {
    this.error.set('');
    try {
      await navigator.clipboard.writeText(key.token);
      this.message.set(`Token for "${key.label}" copied.`);
    } catch {
      this.error.set('Could not copy the token to the clipboard.');
    }
  }

  private async refresh(): Promise<void> {
    this.loading.set(true);
    try {
      await this.reload();
    } catch (error) {
      this.error.set(messageOf(error));
    } finally {
      this.loading.set(false);
    }
  }

  private async reload(): Promise<void> {
    this.keys.set(await this.session.run(() => this.session.api.listDemoKeys()));
  }

  private async act(action: () => Promise<void>): Promise<void> {
    this.busy.set(true);
    this.error.set('');
    this.message.set('');
    try {
      await action();
    } catch (error) {
      this.error.set(messageOf(error));
    } finally {
      this.busy.set(false);
    }
  }
}

function messageOf(error: unknown): string {
  return error instanceof Error ? error.message : String(error);
}
