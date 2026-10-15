import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import type { CourseResponse, LectureResponse, ProfessorResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { injectTask, reloadOnReturn } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-professors-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Professors" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      @if (access.isAdmin()) {
        <app-button label="New professor" (press)="go('new')" />
      }
      @for (professor of professors(); track professor.id) {
        <app-card
          [title]="professor.name ?? ''"
          [subtitle]="professor.employeeId + ' · ' + professor.department"
          (press)="go(professor.employeeId ?? '')"
        />
      } @empty {
        @if (!task.loading() && !task.error()) {
          <text>No professors found.</text>
        }
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class ProfessorsScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  protected readonly professors = signal<ProfessorResponse[]>([]);

  constructor() {
    effect(() => untracked(() => void this.load()));
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const list = await this.task.run(() => this.session.api.listProfessors());
    if (list) {
      this.professors.set(list);
    }
  }

  protected go(...path: string[]): void {
    void this.router.navigate(['/university', 'professors', ...path]);
  }
}

@Component({
  selector: 'app-professor-detail-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Professor" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      @if (professor(); as p) {
        <app-section [title]="p.name ?? ''">
          <text>Employee id: {{ p.employeeId }}</text>
          <text>Department: {{ p.department }}</text>
        </app-section>
        @if (access.isAdmin()) {
          <app-row>
            <app-button label="Edit professor" variant="secondary" (press)="edit()" />
            <app-button label="Delete professor" variant="danger" (press)="confirming.set(true)" />
          </app-row>
        }
        @if (confirming()) {
          <app-confirm
            [message]="'Delete ' + p.name + '? This fails while they still deliver lectures.'"
            (confirm)="remove()"
            (cancel)="confirming.set(false)"
          />
        }
        <app-section title="Teaches">
          @for (course of courses(); track course.courseCode) {
            <app-card [title]="course.title ?? ''" [subtitle]="course.courseCode" (press)="open('courses', course.courseCode ?? '')" />
          } @empty {
            <text>Not assigned to any course.</text>
          }
        </app-section>
        <app-section title="Lectures">
          @for (lecture of lectures(); track lecture.id) {
            <app-card
              [title]="lecture.topic ?? ''"
              [subtitle]="lecture.courseCode + ' · ' + lecture.date + ' ' + lecture.time + ' · ' + lecture.room"
              (press)="open('lectures', '' + lecture.id)"
            />
          } @empty {
            <text>No lectures.</text>
          }
        </app-section>
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class ProfessorDetailScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input.required<string>();
  protected readonly professor = signal<ProfessorResponse | null>(null);
  protected readonly courses = signal<CourseResponse[]>([]);
  protected readonly lectures = signal<LectureResponse[]>([]);
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
      const [professor, courses, lectures] = await Promise.all([
        api.getProfessor(id),
        api.listCourses(),
        api.listLecturesByProfessor(id),
      ]);
      return { professor, courses, lectures };
    });
    if (data) {
      this.professor.set(data.professor);
      this.courses.set(data.courses.filter((c) => c.teacherIds?.includes(id)));
      this.lectures.set(data.lectures);
    }
  }

  protected edit(): void {
    void this.router.navigate(['/university', 'professors', this.id(), 'edit']);
  }

  protected open(section: string, id: string): void {
    void this.router.navigate(['/university', section, id]);
  }

  protected async remove(): Promise<void> {
    const done = await this.task.run(async () => {
      await this.session.api.deleteProfessor(this.id());
      return true;
    });
    this.confirming.set(false);
    if (done) {
      await this.router.navigate(['/university', 'professors'], { replaceUrl: true });
    }
  }
}

@Component({
  selector: 'app-professor-form-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page [title]="id() ? 'Edit professor' : 'New professor'" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (!id()) {
        <app-field label="Employee id" [(value)]="employeeId" />
        <app-field label="Initial password" [(value)]="password" [secure]="true" />
      }
      <app-field label="Name" [(value)]="name" />
      <app-field label="Department" [(value)]="department" />
      <app-button label="Save professor" [disabled]="task.loading() || !valid()" (press)="save()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class ProfessorFormScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  readonly id = input<string>();
  protected readonly employeeId = signal('');
  protected readonly password = signal('');
  protected readonly name = signal('');
  protected readonly department = signal('');
  protected readonly valid = computed(
    () =>
      !!this.name().trim() &&
      !!this.department().trim() &&
      (!!this.id() || (!!this.employeeId().trim() && !!this.password())),
  );

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void (id && this.prefill(id)));
    });
  }

  private async prefill(id: string): Promise<void> {
    const professor = await this.task.run(() => this.session.api.getProfessor(id));
    if (professor) {
      this.name.set(professor.name ?? '');
      this.department.set(professor.department ?? '');
    }
  }

  protected async save(): Promise<void> {
    const id = this.id();
    const name = this.name().trim();
    const department = this.department().trim();
    const saved = await this.task.run(() =>
      id
        ? this.session.api.updateProfessor(id, { name, department })
        : this.session.api.createProfessor({
            employeeId: this.employeeId().trim(),
            name,
            department,
            password: this.password(),
          }),
    );
    if (saved) {
      await this.router.navigate(['/university', 'professors', saved.employeeId ?? id ?? ''], { replaceUrl: true });
    }
  }
}