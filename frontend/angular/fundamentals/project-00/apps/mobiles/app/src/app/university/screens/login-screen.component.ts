import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { injectTask } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-login-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Sign in" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      <text class="lead">Sign in with your student id or employee id.</text>
      <app-field label="Username" [(value)]="username" />
      <app-field label="Password" [(value)]="password" [secure]="true" />
      <app-button label="Sign in" [disabled]="task.loading() || !canSubmit()" (press)="submit()" />
      <app-button label="Create an account" variant="secondary" (press)="register()" />
    </app-page>
  `,
  styles: `
    :host {
      flex: 1;
    }
    .lead {
      color: #596579;
      font-size: 15px;
    }
  `,
})
export class LoginScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  readonly returnUrl = input<string>();
  protected readonly username = signal('');
  protected readonly password = signal('');

  protected canSubmit(): boolean {
    return !!this.username().trim() && !!this.password();
  }

  protected async submit(): Promise<void> {
    const user = await this.task.run(() =>
      this.session.login({ username: this.username().trim(), password: this.password() }),
    );
    if (user) {
      this.password.set('');
      await this.router.navigateByUrl(this.returnUrl() || '/university', { replaceUrl: true });
    }
  }

  protected register(): void {
    void this.router.navigate(['/register']);
  }
}