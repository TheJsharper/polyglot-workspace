import { computed, inject, Injectable } from '@angular/core';
import { UniversitySession } from '@project-00/services-session';

/** Role-derived permissions. The username is the student id or employee id. */
@Injectable({ providedIn: 'root' })
export class Access {
  private readonly session = inject(UniversitySession);

  readonly username = computed(() => this.session.user()?.username ?? '');
  readonly role = computed(() => this.session.user()?.role);
  readonly isAdmin = computed(() => this.role() === 'ADMIN');
  readonly isProfessor = computed(() => this.role() === 'PROFESSOR');
  readonly isStudent = computed(() => this.role() === 'STUDENT');
  readonly isStaff = computed(() => this.isAdmin() || this.isProfessor());
}