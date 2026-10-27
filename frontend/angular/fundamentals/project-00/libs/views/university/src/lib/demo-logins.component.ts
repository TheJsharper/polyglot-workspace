import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UniversitySession } from '@project-00/services-session';
import type { CreatedDemoLoginResponse, DemoLoginPersona, DemoLoginResponse } from '@project-00/services-api';

export const MIN_DEMO_LOGIN_DAYS = 1;
export const MAX_DEMO_LOGIN_DAYS = 30;

const PERSONAS: { value: DemoLoginPersona; label: string }[] = [
  { value: 'STUDENT', label: 'Student' },
  { value: 'PROFESSOR', label: 'Professor' },
  { value: 'ADMIN', label: 'Admin' },
];

/**
 * Admin/manager workspace: generate read-only recruiter logins (random username and password), list and revoke
 * them. The password is shown once, right after generation.
 */
@Component({
  selector: 'app-demo-logins',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe],
  template: `
    <section class="logins">
      <header>
        <p class="eyebrow">Recruiters</p>
        <h1>Demo logins</h1>
        <p>
          Generate a username and password for a recruiter. They sign in on the normal login page, see only demo
          data and cannot change anything. The login expires automatically.
        </p>
      </header>

      <form class="create" (ngSubmit)="create()">
        <label>Explore as
          <select name="persona" [ngModel]="persona()" (ngModelChange)="persona.set($event)">
            @for (option of personas; track option.value) {
              <option [value]="option.value">{{ option.label }}</option>
            }
          </select>
        </label>
        <label>Valid for (days)
          <input name="days" type="number" [min]="minDays" [max]="maxDays" step="1" required [ngModel]="days()"
            (ngModelChange)="days.set($event)" />
        </label>
        <label>Label
          <input name="label" maxlength="100" placeholder="Recruiter or company" [ngModel]="label()"
            (ngModelChange)="label.set($event)" />
        </label>
        <button type="submit" [disabled]="busy() || !daysValid()">Generate login</button>
        @if (!daysValid()) {
          <p class="hint" role="alert">Enter a whole number of days from {{ minDays }} to {{ maxDays }}.</p>
        }
      </form>

      @if (error()) { <p class="failure" role="alert">{{ error() }}</p> }
      @if (message()) { <p class="success" role="status">{{ message() }}</p> }

      @if (created(); as login) {
        <article class="credentials" aria-live="polite">
          <h2>Send these to the recruiter</h2>
          <p class="warning">The password is shown only now. Copy it before leaving this page.</p>
          <dl>
            <dt>Username</dt><dd><code>{{ login.username }}</code></dd>
            <dt>Password</dt><dd><code>{{ login.password }}</code></dd>
            <dt>Expires</dt><dd>{{ login.expiresAt | date: 'medium' }}</dd>
          </dl>
          <div class="actions">
            <button type="button" (click)="copy(login)">Copy username and password</button>
            <button type="button" class="secondary" (click)="created.set(null)">Hide</button>
          </div>
        </article>
      }

      @if (loading()) {
        <p role="status">Loading demo logins…</p>
      } @else if (logins().length === 0) {
        <p>No active demo logins.</p>
      } @else {
        <table>
          <caption class="sr-only">Active demo logins</caption>
          <thead>
            <tr><th>Username</th><th>Persona</th><th>Label</th><th>Created</th><th>Expires</th><th>Actions</th></tr>
          </thead>
          <tbody>
            @for (login of logins(); track login.username) {
              <tr>
                <td><code>{{ login.username }}</code></td>
                <td>{{ personaLabel(login.persona) }}</td>
                <td>{{ login.label || '—' }}</td>
                <td>{{ login.createdAt | date: 'short' }}</td>
                <td>{{ login.expiresAt | date: 'short' }}</td>
                <td>
                  <button type="button" class="danger" [disabled]="busy()" (click)="revoke(login)">Revoke</button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styles: [`
    .logins { max-width: 64rem; margin: 2rem auto; padding: 0 1.5rem; display: grid; gap: 1.25rem; }
    .eyebrow { margin: 0; text-transform: uppercase; font-size: .75rem; letter-spacing: .08em; }
    .create { display: flex; flex-wrap: wrap; gap: 1rem; align-items: end; }
    label { display: grid; gap: .3rem; }
    input, select { padding: .6rem; border: 1px solid var(--mat-sys-outline); border-radius: .5rem; background: var(--mat-sys-surface); color: inherit; font: inherit; }
    button { padding: .55rem 1rem; border: 0; border-radius: 999px; background: var(--mat-sys-primary); color: var(--mat-sys-on-primary); font: inherit; font-weight: 600; cursor: pointer; }
    button:disabled { opacity: .55; cursor: not-allowed; }
    button.danger { background: var(--mat-sys-error); color: var(--mat-sys-on-error); }
    button.secondary { background: var(--mat-sys-secondary-container); color: var(--mat-sys-on-secondary-container); }
    .credentials { padding: 1rem 1.25rem; border: 1px solid var(--mat-sys-outline-variant); border-radius: .75rem; background: var(--mat-sys-surface-container); }
    .credentials h2 { margin-top: 0; }
    .credentials dl { display: grid; grid-template-columns: max-content 1fr; gap: .4rem 1rem; }
    .credentials dd { margin: 0; }
    .actions { display: flex; gap: .5rem; flex-wrap: wrap; }
    .warning { color: var(--mat-sys-error); }
    table { width: 100%; border-collapse: collapse; }
    th, td { text-align: left; padding: .6rem; border-bottom: 1px solid var(--mat-sys-outline-variant); }
    .failure, .hint { color: var(--mat-sys-error); }
    .success { color: var(--mat-sys-tertiary); }
    .sr-only { position: absolute; width: 1px; height: 1px; overflow: hidden; clip-path: inset(50%); }
  `],
})
export class DemoLoginsComponent implements OnInit {
  protected readonly session = inject(UniversitySession);
  protected readonly personas = PERSONAS;
  protected readonly minDays = MIN_DEMO_LOGIN_DAYS;
  protected readonly maxDays = MAX_DEMO_LOGIN_DAYS;
  protected readonly logins = signal<DemoLoginResponse[]>([]);
  protected readonly created = signal<CreatedDemoLoginResponse | null>(null);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly message = signal('');
  protected readonly persona = signal<DemoLoginPersona>('STUDENT');
  protected readonly days = signal<number>(7);
  protected readonly label = signal('');
  protected readonly daysValid = computed(() => {
    const value = this.days();
    return Number.isInteger(value) && value >= MIN_DEMO_LOGIN_DAYS && value <= MAX_DEMO_LOGIN_DAYS;
  });

  ngOnInit(): void {
    void this.refresh();
  }

  protected personaLabel(persona: DemoLoginPersona): string {
    return PERSONAS.find((option) => option.value === persona)?.label ?? persona;
  }

  protected async create(): Promise<void> {
    if (this.busy() || !this.daysValid()) return;
    await this.act(async () => {
      const request = { persona: this.persona(), days: this.days(), label: this.label().trim() || undefined };
      const login = await this.session.run(() => this.session.api.createDemoLogin(request));
      this.created.set(login);
      this.message.set(`Demo login ${login.username} generated.`);
      await this.reload();
    });
  }

  protected async revoke(login: DemoLoginResponse): Promise<void> {
    if (this.busy()) return;
    await this.act(async () => {
      await this.session.run(() => this.session.api.revokeDemoLogin(login.username));
      if (this.created()?.username === login.username) this.created.set(null);
      this.message.set(`Demo login ${login.username} revoked.`);
      await this.reload();
    });
  }

  protected async copy(login: CreatedDemoLoginResponse): Promise<void> {
    this.error.set('');
    try {
      await navigator.clipboard.writeText(`Username: ${login.username}\nPassword: ${login.password}`);
      this.message.set('Username and password copied.');
    } catch {
      this.error.set('Could not copy to the clipboard. Select the text and copy it manually.');
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
    this.logins.set(await this.session.run(() => this.session.api.listDemoLogins()));
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
