import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import type { CreatedDemoLoginResponse, DemoLoginPersona, DemoLoginResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { injectTask, reloadOnReturn } from '../task.ts';
import { UI } from '../ui.ts';

const PERSONAS: { value: DemoLoginPersona; label: string }[] = [
  { value: 'STUDENT', label: 'Student' },
  { value: 'PROFESSOR', label: 'Professor' },
  { value: 'ADMIN', label: 'Admin' },
];

@Component({
  selector: 'app-demo-logins-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Demo logins" [loading]="task.loading()" [error]="task.error()" [notice]="task.notice()" (retry)="load()">
      <text class="description">
        Generate temporary, read-only recruiter accounts. Passwords are shown only once, when an account is created.
      </text>
      <app-choice label="Explore as" [options]="personas" [(selected)]="persona" />
      <app-field label="Valid for (days)" [(value)]="days" keyboard="number-pad" />
      <app-field label="Label" [(value)]="label" placeholder="Recruiter or company" />
      @if (!daysValid()) {
        <text class="validation" accessibilityRole="alert">Enter a whole number from 1 to 30.</text>
      }
      <app-button label="Generate login" [disabled]="task.loading() || !daysValid()" (press)="create()" />

      @if (created(); as login) {
        <app-section title="Share these credentials">
          <text class="warning">The password is only available now. Save it before leaving this screen.</text>
          <app-field label="Username" [value]="login.username" [readonly]="true" />
          <text class="credential-label">Password</text>
          <text class="credential" selectable>{{ login.password }}</text>
          <text>Expires: {{ formatDate(login.expiresAt) }}</text>
          <app-button label="Hide credentials" variant="secondary" (press)="created.set(null)" />
        </app-section>
      }

      <app-section title="Active demo logins">
        @for (login of logins(); track login.username) {
          <app-card
            [title]="login.username"
            [subtitle]="personaLabel(login.persona) + (login.label ? ' · ' + login.label : '')"
            [badge]="'Expires ' + formatDate(login.expiresAt)"
          />
          <app-button
            [label]="'Revoke ' + login.username"
            variant="danger"
            [disabled]="task.loading()"
            (press)="confirming.set(login.username)"
          />
          @if (confirming() === login.username) {
            <app-confirm
              [message]="'Revoke demo login ' + login.username + '? The account will no longer be able to sign in.'"
              confirmLabel="Revoke"
              (confirm)="revoke(login)"
              (cancel)="confirming.set('')"
            />
          }
        } @empty {
          @if (!task.loading() && !task.error()) {
            <text>No active demo logins.</text>
          }
        }
      </app-section>
    </app-page>
  `,
  styles: `
    :host { flex: 1; }
    .description { color: var(--muted); font-size: 15px; }
    .validation, .warning { color: var(--error-fg); font-size: 14px; }
    .credential-label { color: var(--muted); font-size: 13px; font-weight: 700; }
    .credential { padding: 12px; border-radius: 10px; background-color: var(--surface); color: var(--text); font-size: 16px; }
  `,
})
export class DemoLoginsScreenComponent {
  private readonly session = inject(UniversitySession);
  protected readonly task = injectTask();
  protected readonly personas = PERSONAS;
  protected readonly logins = signal<DemoLoginResponse[]>([]);
  protected readonly created = signal<CreatedDemoLoginResponse | null>(null);
  protected readonly confirming = signal('');
  protected readonly persona = signal<DemoLoginPersona>('STUDENT');
  protected readonly days = signal('7');
  protected readonly label = signal('');
  protected readonly daysValid = computed(() => {
    const value = Number(this.days().trim());
    return Number.isInteger(value) && value >= 1 && value <= 30;
  });

  constructor() {
    effect(() => untracked(() => void this.load()));
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const logins = await this.task.run(() => this.session.api.listDemoLogins());
    if (logins) {
      this.logins.set(logins);
    }
  }

  protected async create(): Promise<void> {
    if (!this.daysValid() || this.task.loading()) {
      return;
    }
    const login = await this.task.run(() =>
      this.session.api.createDemoLogin({
        persona: this.persona(),
        days: Number(this.days()),
        label: this.label().trim() || undefined,
      }),
    );
    if (login) {
      this.created.set(login);
      await this.load();
      this.task.notice.set(`Demo login ${login.username} generated.`);
    }
  }

  protected async revoke(login: DemoLoginResponse): Promise<void> {
    const revoked = await this.task.run(async () => {
      await this.session.api.revokeDemoLogin(login.username);
      return true;
    });
    this.confirming.set('');
    if (revoked) {
      if (this.created()?.username === login.username) {
        this.created.set(null);
      }
      await this.load();
      this.task.notice.set(`Demo login ${login.username} revoked.`);
    }
  }

  protected personaLabel(persona: DemoLoginPersona): string {
    return PERSONAS.find((option) => option.value === persona)?.label ?? persona;
  }

  protected formatDate(value: string): string {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
  }
}
