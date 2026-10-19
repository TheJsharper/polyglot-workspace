import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import type { RegisterRequest } from '@project-00/services-api';

@Component({
  selector: 'app-login',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth">
      <h1>Log in</h1>
      <form (ngSubmit)="submit()">
        <label>Username or email <input name="username" [(ngModel)]="username" required autocomplete="username" /></label>
        <label>Password <input name="password" [(ngModel)]="password" required type="password" autocomplete="current-password" /></label>
        <button type="submit" [disabled]="busy()">Log in</button>
      </form>
      @if (error()) { <p role="alert">{{ error() }}</p> }
      <p>No account? <a routerLink="/register">Register</a></p>
    </section>
  `,
  styles: [`.auth { max-width: 30rem; margin: 2rem auto; padding: 1.5rem; }
    form, label { display: grid; gap: .6rem; } form { gap: 1rem; }
    input { padding: .7rem; } [role=alert] { color: #a52424; }`],
})
export class LoginComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected username = '';
  protected password = '';

  protected async submit(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    try {
      await this.session.login({ username: this.username, password: this.password });
      this.password = '';
      const requested = this.router.parseUrl(this.router.url).queryParams['returnUrl'];
      const destination = typeof requested === 'string' && requested.startsWith('/university')
        ? requested : '/university';
      await this.router.navigateByUrl(destination);
    } catch (error) {
      this.error.set(error instanceof Error ? error.message : String(error));
    } finally {
      this.busy.set(false);
    }
  }
}

@Component({
  selector: 'app-register',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink],
  template: `
    <section class="auth">
      <h1>Register</h1>
      <form (ngSubmit)="submit()">
        <label>Role
          <select name="role" [(ngModel)]="role">
            <option value="STUDENT">Student</option>
            <option value="PROFESSOR">Professor</option>
            @if (session.user()?.role === 'ADMIN') { <option value="ADMIN">Admin</option> }
          </select>
        </label>
        <label>Username / ID (max 254 characters, used to log in) <input name="id" [(ngModel)]="id" required maxlength="254" /></label>
        @if (role !== 'ADMIN') {
          <label>Full name <input name="name" [(ngModel)]="name" required /></label>
        }
        @if (role === 'STUDENT') {
          <label>Enrollment year <input name="year" [(ngModel)]="enrollmentYear" type="number" min="1900" /></label>
        }
        @if (role === 'PROFESSOR') {
          <label>Department <input name="department" [(ngModel)]="department" required /></label>
        }
        @if (role !== 'ADMIN') {
          <label>Email (optional, can be used to log in) <input name="email" [(ngModel)]="email" type="email" maxlength="254" autocomplete="email" /></label>
        }
        <label>Password <input name="password" [(ngModel)]="password" type="password" required minlength="8" maxlength="72" autocomplete="new-password" /></label>
        <button type="submit" [disabled]="busy()">Register</button>
      </form>
      @if (error()) { <p role="alert">{{ error() }}</p> }
      @if (success()) { <p role="status">{{ success() }}</p> }
      <a routerLink="/login">Log in</a>
    </section>
  `,
  styles: [`.auth { max-width: 30rem; margin: 2rem auto; padding: 1.5rem; }
    form, label { display: grid; gap: .6rem; } form { gap: 1rem; }
    input, select { padding: .7rem; } [role=alert] { color: #a52424; }`],
})
export class RegisterComponent {
  protected readonly session = inject(UniversitySession);
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly success = signal('');
  protected role: RegisterRequest['role'] = 'STUDENT';
  protected id = '';
  protected name = '';
  protected department = '';
  protected email = '';
  protected enrollmentYear: number | undefined;
  protected password = '';

  protected async submit(): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.success.set('');
    try {
      const result = await this.session.register({
        role: this.role,
        id: this.id,
        name: this.role === 'ADMIN' ? undefined : this.name,
        enrollmentYear: this.role === 'STUDENT' ? this.enrollmentYear : undefined,
        department: this.role === 'PROFESSOR' ? this.department : undefined,
        email: this.role !== 'ADMIN' && this.email.trim() ? this.email.trim() : undefined,
        password: this.password,
      });
      this.password = '';
      this.success.set(`${result.username} registered. ${this.session.authenticated() ? '' : `Log in with username "${result.username}" and your password.`}`);
    } catch (error) {
      this.error.set(error instanceof Error ? error.message : String(error));
    } finally {
      this.busy.set(false);
    }
  }
}
