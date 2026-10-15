import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import type { CourseResponse, NotesResponse, StudentResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { injectTask, parseOptionalInt, reloadOnReturn } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-students-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Students" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      @if (access.isAdmin()) {
        <app-button label="New student" (press)="go('new')" />
      }
      <app-field label="Search" [(value)]="query" placeholder="Name or student id" />
      @for (student of filtered(); track student.id) {
        <app-card
          [title]="student.name ?? ''"
          [subtitle]="student.studentId + (student.enrollmentYear ? ' · enrolled ' + student.enrollmentYear : '')"
          (press)="go(student.studentId ?? '')"
        />
      } @empty {
        @if (!task.loading() && !task.error()) {
          <text>No students found.</text>
        }
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class StudentsScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  protected readonly students = signal<StudentResponse[]>([]);
  protected readonly query = signal('');
  protected readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    return this.students().filter((s) => !q || `${s.name} ${s.studentId}`.toLowerCase().includes(q));
  });

  constructor() {
    effect(() => untracked(() => void this.load()));
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const list = await this.task.run(async () => this.access.isStudent()
      ? [await this.session.api.getStudent(this.access.username())]
      : this.session.api.listStudents());
    if (list) {
      this.students.set(list);
    }
  }

  protected go(...path: string[]): void {
    void this.router.navigate(['/university', 'students', ...path]);
  }
}

@Component({
  selector: 'app-student-detail-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Student" [loading]="task.loading()" [error]="task.error()" [notice]="task.notice()" (retry)="load()">
      @if (student(); as s) {
        <app-section [title]="s.name ?? ''">
          <text>Student id: {{ s.studentId }}</text>
          <text>Enrollment year: {{ s.enrollmentYear ?? 'not set' }}</text>
        </app-section>
        <app-row>
          @if (access.isAdmin() || access.username() === s.studentId) {
            <app-button label="Edit student" variant="secondary" (press)="edit()" />
          }
          @if (access.isAdmin()) {
            <app-button label="Delete student" variant="danger" (press)="confirming.set(true)" />
          }
        </app-row>
        @if (confirming()) {
          <app-confirm
            [message]="'Delete ' + s.name + '? This also removes enrollments, attendance and the login account.'"
            (confirm)="remove()"
            (cancel)="confirming.set(false)"
          />
        }
        <app-section title="Courses">
          @for (course of courses(); track course.courseCode) {
            <app-card [title]="course.title ?? ''" [subtitle]="course.courseCode" (press)="open('courses', course.courseCode ?? '')" />
          } @empty {
            <text>Not enrolled in any course.</text>
          }
        </app-section>
        <app-section title="Notes">
          @for (note of notes(); track note.id) {
            <app-card
              [title]="'Lecture ' + note.lectureId"
              [subtitle]="note.content"
              [badge]="note.grade != null ? note.grade + '/100' : ''"
              (press)="open('notes', '' + note.id)"
            />
          } @empty {
            <text>No notes visible.</text>
          }
        </app-section>
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class StudentDetailScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input.required<string>();
  protected readonly student = signal<StudentResponse | null>(null);
  protected readonly courses = signal<CourseResponse[]>([]);
  protected readonly notes = signal<NotesResponse[]>([]);
  protected readonly confirming = signal(false);

  constructor() {
    effect(() => {
      this.id();
      untracked(() => void this.load());
    });
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const id = this.id();
    const data = await this.task.run(async () => {
      const api = this.session.api;
      const [student, courses, notes] = await Promise.all([
        api.getStudent(id),
        api.listCourses(),
        api.listNotesByStudent(id),
      ]);
      return { student, courses, notes };
    });
    if (data) {
      this.student.set(data.student);
      this.courses.set(data.courses.filter((c) => c.enrolledStudentIds?.includes(id)));
      this.notes.set(data.notes);
    }
  }

  protected edit(): void {
    void this.router.navigate(['/university', 'students', this.id(), 'edit']);
  }

  protected open(section: string, id: string): void {
    void this.router.navigate(['/university', section, id]);
  }

  protected async remove(): Promise<void> {
    const done = await this.task.run(async () => {
      await this.session.api.deleteStudent(this.id());
      return true;
    });
    this.confirming.set(false);
    if (done) {
      await this.router.navigate(['/university', 'students'], { replaceUrl: true });
    }
  }
}

@Component({
  selector: 'app-student-form-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page [title]="id() ? 'Edit student' : 'New student'" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (!id()) {
        <app-field label="Student id" [(value)]="studentId" />
      }
      <app-field label="Name" [(value)]="name" />
      <app-field label="Enrollment year" [(value)]="year" keyboard="number-pad" />
      <app-button label="Save student" [disabled]="task.loading() || !valid()" (press)="save()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class StudentFormScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  readonly id = input<string>();
  protected readonly studentId = signal('');
  protected readonly name = signal('');
  protected readonly year = signal('');
  protected readonly valid = computed(() => !!this.name().trim() && (!!this.id() || !!this.studentId().trim()));

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void (id && this.prefill(id)));
    });
  }

  private async prefill(id: string): Promise<void> {
    const student = await this.task.run(() => this.session.api.getStudent(id));
    if (student) {
      this.name.set(student.name ?? '');
      this.year.set(student.enrollmentYear?.toString() ?? '');
    }
  }

  protected async save(): Promise<void> {
    const id = this.id();
    const enrollmentYear = parseOptionalInt(this.year());
    const name = this.name().trim();
    const saved = await this.task.run(() =>
      id
        ? this.session.api.updateStudent(id, { name, enrollmentYear })
        : this.session.api.createStudent({ studentId: this.studentId().trim(), name, enrollmentYear }),
    );
    if (saved) {
      await this.router.navigate(['/university', 'students', saved.studentId ?? id ?? ''], { replaceUrl: true });
    }
  }
}