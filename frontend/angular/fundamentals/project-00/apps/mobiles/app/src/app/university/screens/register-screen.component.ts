import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import type { RegisterRequest, Role } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { injectTask, parseOptionalInt } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-register-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Create account" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      <app-choice label="Account type" [options]="roles" [(selected)]="role" />
      <app-field [label]="role() === 'PROFESSOR' ? 'Employee id' : 'Student id'" [(value)]="id" />
      <app-field label="Full name" [(value)]="name" />
      @if (role() === 'STUDENT') {
        <app-field label="Enrollment year" [(value)]="year" keyboard="number-pad" />
      } @else {
        <app-field label="Department" [(value)]="department" />
      }
      <app-field label="Password" [(value)]="password" [secure]="true" />
      <app-button label="Register" [disabled]="task.loading() || !valid()" (press)="submit()" />
      <app-button label="Back to sign in" variant="secondary" (press)="back()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class RegisterScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  protected readonly roles = [
    { value: 'STUDENT', label: 'Student' },
    { value: 'PROFESSOR', label: 'Professor' },
  ];
  protected readonly role = signal('STUDENT');
  protected readonly id = signal('');
  protected readonly name = signal('');
  protected readonly year = signal('');
  protected readonly department = signal('');
  protected readonly password = signal('');

  protected readonly valid = computed(
    () =>
      !!this.id().trim() &&
      !!this.name().trim() &&
      !!this.password() &&
      (this.role() === 'STUDENT' || !!this.department().trim()),
  );

  protected async submit(): Promise<void> {
    const id = this.id().trim();
    const request: RegisterRequest = {
      role: this.role() as Role,
      id,
      name: this.name().trim(),
      password: this.password(),
      ...(this.role() === 'STUDENT'
        ? { enrollmentYear: parseOptionalInt(this.year()) }
        : { department: this.department().trim() }),
    };
    const created = await this.task.run(async () => {
      await this.session.register(request);
      return this.session.login({ username: id, password: request.password });
    });
    if (created) {
      await this.router.navigateByUrl('/university', { replaceUrl: true });
    }
  }

  protected back(): void {
    void this.router.navigate(['/login']);
  }
}