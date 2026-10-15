import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-university-home-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="University" [retryable]="false">
      <app-section title="Signed in">
        <text class="who">{{ access.username() }} · {{ access.role() }}</text>
      </app-section>
      @if (access.isStudent()) {
        <app-card title="My profile" subtitle="Your student record and courses" (press)="go('students', access.username())" />
      } @else {
        <app-card title="Students" subtitle="Records, enrollments and notes" (press)="go('students')" />
      }
      <app-card title="Professors" subtitle="Staff, courses and lectures" (press)="go('professors')" />
      <app-card title="Courses" subtitle="Catalog, teachers and enrollment" (press)="go('courses')" />
      <app-card title="Lectures" subtitle="Schedule and attendance" (press)="go('lectures')" />
      <app-card title="Notes" subtitle="Lecture notes and reviews" (press)="go('notes')" />
      <app-button label="Sign out" variant="secondary" (press)="signOut()" />
    </app-page>
  `,
  styles: `
    :host {
      flex: 1;
    }
    .who {
      color: #596579;
      font-size: 15px;
    }
  `,
})
export class UniversityHomeScreenComponent {
  private readonly router = inject(Router);
  private readonly session = inject(UniversitySession);
  protected readonly access = inject(Access);

  protected go(...path: string[]): void {
    void this.router.navigate(['/university', ...path]);
  }

  protected signOut(): void {
    this.session.logout();
  }
}