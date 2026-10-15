import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import type { CourseResponse, LectureResponse, ProfessorResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { injectTask, parseOptionalInt, reloadOnReturn } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-courses-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Courses" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      @if (access.isAdmin()) {
        <app-button label="New course" (press)="go('new')" />
      }
      @for (course of courses(); track course.id) {
        <app-card
          [title]="course.title ?? ''"
          [subtitle]="course.courseCode + ' · ' + (course.credits ?? 0) + ' credits'"
          [badge]="enrolled(course) ? 'Enrolled' : ''"
          (press)="go(course.courseCode ?? '')"
        />
      } @empty {
        @if (!task.loading() && !task.error()) {
          <text>No courses found.</text>
        }
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class CoursesScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  protected readonly courses = signal<CourseResponse[]>([]);

  constructor() {
    effect(() => untracked(() => void this.load()));
    reloadOnReturn(() => void this.load());
  }

  protected enrolled(course: CourseResponse): boolean {
    return !!course.enrolledStudentIds?.includes(this.access.username());
  }

  protected async load(): Promise<void> {
    const list = await this.task.run(() => this.session.api.listCourses());
    if (list) {
      this.courses.set(list);
    }
  }

  protected go(...path: string[]): void {
    void this.router.navigate(['/university', 'courses', ...path]);
  }
}

@Component({
  selector: 'app-course-detail-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Course" [loading]="task.loading()" [error]="task.error()" [notice]="task.notice()" (retry)="load()">
      @if (course(); as c) {
        <app-section [title]="c.title ?? ''">
          <text>Code: {{ c.courseCode }}</text>
          <text>Credits: {{ c.credits ?? 'not set' }}</text>
        </app-section>
        <app-row>
          @if (access.isStudent()) {
            @if (isEnrolled()) {
              <app-button label="Unenroll me" variant="secondary" (press)="unenroll()" />
            } @else {
              <app-button label="Enroll me" (press)="enroll()" />
            }
          }
          @if (access.isAdmin()) {
            <app-button label="Edit course" variant="secondary" (press)="edit()" />
            <app-button label="Delete course" variant="danger" (press)="confirming.set(true)" />
          }
          @if (teachesHere()) {
            <app-button label="Schedule lecture" (press)="schedule()" />
          }
        </app-row>
        @if (confirming()) {
          <app-confirm
            [message]="'Delete ' + c.title + ' and all of its lectures?'"
            (confirm)="remove()"
            (cancel)="confirming.set(false)"
          />
        }

        <app-section title="Teachers">
          @for (teacherId of c.teacherIds ?? []; track teacherId) {
            <app-card [title]="teacherName(teacherId)" [subtitle]="teacherId" (press)="open('professors', teacherId)" />
            @if (access.isAdmin()) {
              <app-button [label]="'Remove teacher ' + teacherId" variant="secondary" (press)="unassigning.set(teacherId)" />
            }
          } @empty {
            <text>No teachers assigned.</text>
          }
          @if (unassigning(); as teacherId) {
            <app-confirm
              [message]="'Remove ' + teacherId + ' from this course?'"
              confirmLabel="Remove"
              (confirm)="unassign(teacherId)"
              (cancel)="unassigning.set('')"
            />
          }
          @if (access.isAdmin()) {
            <app-choice label="Assign a professor" [options]="assignable()" [(selected)]="assignee" empty="Every professor already teaches this course." />
            <app-button label="Assign professor" [disabled]="!assignee()" (press)="assign()" />
          }
        </app-section>

        <app-section title="Students">
          @for (studentId of c.enrolledStudentIds ?? []; track studentId) {
            <app-card
              [title]="studentId"
              [subtitle]="studentId === access.username() ? 'You' : ''"
              (press)="open('students', studentId)"
            />
          } @empty {
            <text>No students enrolled.</text>
          }
        </app-section>

        <app-section title="Lectures">
          @for (lecture of lectures(); track lecture.id) {
            <app-card
              [title]="lecture.topic ?? ''"
              [subtitle]="lecture.date + ' ' + lecture.time + ' · ' + lecture.room"
              (press)="open('lectures', '' + lecture.id)"
            />
          } @empty {
            <text>No lectures scheduled.</text>
          }
        </app-section>
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class CourseDetailScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input.required<string>();
  protected readonly course = signal<CourseResponse | null>(null);
  protected readonly lectures = signal<LectureResponse[]>([]);
  protected readonly professors = signal<ProfessorResponse[]>([]);
  protected readonly confirming = signal(false);
  protected readonly unassigning = signal('');
  protected readonly assignee = signal('');

  protected readonly isEnrolled = computed(() => !!this.course()?.enrolledStudentIds?.includes(this.access.username()));
  protected readonly teachesHere = computed(
    () => this.access.isProfessor() && !!this.course()?.teacherIds?.includes(this.access.username()),
  );
  protected readonly assignable = computed(() => {
    const taught = this.course()?.teacherIds ?? [];
    return this.professors()
      .filter((p) => !taught.includes(p.employeeId ?? ''))
      .map((p) => ({ value: p.employeeId ?? '', label: `${p.name} (${p.employeeId})` }));
  });

  constructor() {
    effect(() => {
      this.id();
      untracked(() => void this.load());
    });
    reloadOnReturn(() => void this.load());
  }

  protected teacherName(employeeId: string): string {
    return this.professors().find((p) => p.employeeId === employeeId)?.name ?? employeeId;
  }

  protected async load(): Promise<void> {
    const id = this.id();
    const data = await this.task.run(async () => {
      const api = this.session.api;
      const [course, lectures, professors] = await Promise.all([
        api.getCourse(id),
        api.listLecturesByCourse(id),
        api.listProfessors(),
      ]);
      return { course, lectures, professors };
    });
    if (data) {
      this.course.set(data.course);
      this.lectures.set(data.lectures);
      this.professors.set(data.professors);
    }
  }

  /** Runs a course mutation that returns the updated course. */
  private async mutate(action: () => Promise<CourseResponse>, notice: string): Promise<void> {
    const course = await this.task.run(action, notice);
    if (course) {
      this.course.set(course);
    }
  }

  protected enroll(): Promise<void> {
    return this.mutate(() => this.session.api.enrollStudentInCourse(this.id(), this.access.username()), 'Enrolled.');
  }

  protected unenroll(): Promise<void> {
    return this.mutate(
      () => this.session.api.unenrollStudentFromCourse(this.id(), this.access.username()),
      'Unenrolled.',
    );
  }

  protected async assign(): Promise<void> {
    const employeeId = this.assignee();
    await this.mutate(() => this.session.api.assignCourseTeacher(this.id(), employeeId), `Assigned ${employeeId}.`);
    this.assignee.set('');
  }

  protected async unassign(employeeId: string): Promise<void> {
    this.unassigning.set('');
    await this.mutate(() => this.session.api.unassignCourseTeacher(this.id(), employeeId), `Removed ${employeeId}.`);
  }

  protected edit(): void {
    void this.router.navigate(['/university', 'courses', this.id(), 'edit']);
  }

  protected schedule(): void {
    void this.router.navigate(['/university', 'lectures', 'new'], { queryParams: { courseCode: this.id() } });
  }

  protected open(section: string, id: string): void {
    void this.router.navigate(['/university', section, id]);
  }

  protected async remove(): Promise<void> {
    const done = await this.task.run(async () => {
      await this.session.api.deleteCourse(this.id());
      return true;
    });
    this.confirming.set(false);
    if (done) {
      await this.router.navigate(['/university', 'courses'], { replaceUrl: true });
    }
  }
}

@Component({
  selector: 'app-course-form-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page [title]="id() ? 'Edit course' : 'New course'" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (!id()) {
        <app-field label="Course code" [(value)]="courseCode" />
      }
      <app-field label="Title" [(value)]="title" />
      <app-field label="Credits" [(value)]="credits" keyboard="number-pad" />
      <app-button label="Save course" [disabled]="task.loading() || !valid()" (press)="save()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class CourseFormScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  readonly id = input<string>();
  protected readonly courseCode = signal('');
  protected readonly title = signal('');
  protected readonly credits = signal('');
  protected readonly valid = computed(() => !!this.title().trim() && (!!this.id() || !!this.courseCode().trim()));

  constructor() {
    effect(() => {
      const id = this.id();
      untracked(() => void (id && this.prefill(id)));
    });
  }

  private async prefill(id: string): Promise<void> {
    const course = await this.task.run(() => this.session.api.getCourse(id));
    if (course) {
      this.title.set(course.title ?? '');
      this.credits.set(course.credits?.toString() ?? '');
    }
  }

  protected async save(): Promise<void> {
    const id = this.id();
    const title = this.title().trim();
    const credits = parseOptionalInt(this.credits());
    const saved = await this.task.run(() =>
      id
        ? this.session.api.updateCourse(id, { title, credits })
        : this.session.api.createCourse({ courseCode: this.courseCode().trim(), title, credits }),
    );
    if (saved) {
      await this.router.navigate(['/university', 'courses', saved.courseCode ?? id ?? ''], { replaceUrl: true });
    }
  }
}