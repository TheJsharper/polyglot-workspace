import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import type { CourseResponse, LectureResponse, NotesResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { injectTask, reloadOnReturn, today } from '../task.ts';
import { UI } from '../ui.ts';

const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const TIME_PATTERN = /^\d{2}:\d{2}(:\d{2})?$/;

@Component({
  selector: 'app-lectures-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Lectures" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      @if (access.isProfessor()) {
        <app-choice label="Show" [options]="views" [(selected)]="view" />
      }
      @if (view() === 'course') {
        <app-choice label="Course" [options]="courseOptions()" [(selected)]="course" empty="No courses yet." />
      }
      @for (lecture of lectures(); track lecture.id) {
        <app-card
          [title]="lecture.topic ?? ''"
          [subtitle]="lecture.courseCode + ' · ' + lecture.date + ' ' + lecture.time + ' · ' + lecture.room"
          [badge]="lecture.attendeeIds?.includes(access.username()) ? 'Attended' : ''"
          (press)="open(lecture.id)"
        />
      } @empty {
        @if (!task.loading() && !task.error() && (course() || view() === 'mine')) {
          <text>No lectures found.</text>
        }
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class LecturesScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly courseCode = input<string>();
  protected readonly views = [
    { value: 'course', label: 'By course' },
    { value: 'mine', label: 'My lectures' },
  ];
  protected readonly view = signal('course');
  protected readonly course = signal('');
  protected readonly courses = signal<CourseResponse[]>([]);
  protected readonly lectures = signal<LectureResponse[]>([]);
  protected readonly courseOptions = computed(() =>
    this.courses().map((c) => ({ value: c.courseCode ?? '', label: c.courseCode ?? '' })),
  );

  constructor() {
    effect(() => {
      const preset = this.courseCode();
      untracked(() => preset && this.course.set(preset));
    });
    effect(() => {
      this.view();
      this.course();
      untracked(() => void this.load());
    });
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const mode = this.view();
    const code = this.course();
    const data = await this.task.run(async () => {
      const api = this.session.api;
      const courses = await api.listCourses();
      if (mode === 'mine') {
        return { courses, lectures: await api.listLecturesByProfessor(this.access.username()) };
      }
      return { courses, lectures: code ? await api.listLecturesByCourse(code) : [] };
    });
    if (data) {
      this.courses.set(data.courses);
      this.lectures.set(data.lectures);
    }
  }

  protected open(id: number | undefined): void {
    void this.router.navigate(['/university', 'lectures', String(id)]);
  }
}

@Component({
  selector: 'app-lecture-detail-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Lecture" [loading]="task.loading()" [error]="task.error()" [notice]="task.notice()" (retry)="load()">
      @if (lecture(); as l) {
        <app-section [title]="l.topic ?? ''">
          <text>{{ l.date }} at {{ l.time }} in {{ l.room }}</text>
        </app-section>
        <app-card [title]="'Course ' + l.courseCode" subtitle="Open course" (press)="open('courses', l.courseCode ?? '')" />
        <app-card [title]="'Professor ' + l.employeeId" subtitle="Open professor" (press)="open('professors', l.employeeId ?? '')" />

        <app-row>
          @if (canManage()) {
            <app-button label="Reschedule" variant="secondary" (press)="reschedule()" />
            <app-button label="Cancel lecture" variant="danger" (press)="confirming.set(true)" />
          }
          @if (attended()) {
            <app-button label="Write a note" (press)="writeNote()" />
          }
        </app-row>
        @if (confirming()) {
          <app-confirm
            message="Cancel this lecture? This fails if notes were written for it."
            confirmLabel="Cancel lecture"
            (confirm)="cancel()"
            (cancel)="confirming.set(false)"
          />
        }

        @if (access.isAdmin() || canManage()) {
          <app-section title="Delivering professor">
            <app-choice label="Hand over to" [options]="teacherOptions()" [(selected)]="newProfessor" empty="Only the current professor teaches this course." />
            <app-button label="Assign professor" [disabled]="!newProfessor()" (press)="assignProfessor()" />
          </app-section>
        }

        <app-section title="Attendance">
          @for (studentId of l.attendeeIds ?? []; track studentId) {
            <app-card [title]="studentId" (press)="open('students', studentId)" />
            @if (canManage()) {
              <app-button [label]="'Remove attendance ' + studentId" variant="secondary" (press)="removeAttendance(studentId)" />
            }
          } @empty {
            <text>No attendance recorded.</text>
          }
          @if (canManage()) {
            <app-choice label="Record attendance" [options]="absentees()" [(selected)]="attendee" empty="All enrolled students attended." />
            <app-button label="Record attendance" [disabled]="!attendee()" (press)="recordAttendance()" />
          }
        </app-section>

        <app-section title="Notes">
          @for (note of notes(); track note.id) {
            <app-card
              [title]="'By ' + note.studentId"
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
export class LectureDetailScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input.required<string>();
  protected readonly lecture = signal<LectureResponse | null>(null);
  protected readonly course = signal<CourseResponse | null>(null);
  protected readonly notes = signal<NotesResponse[]>([]);
  protected readonly confirming = signal(false);
  protected readonly newProfessor = signal('');
  protected readonly attendee = signal('');

  protected readonly canManage = computed(
    () => this.access.isProfessor() && !!this.course()?.teacherIds?.includes(this.access.username()),
  );
  protected readonly attended = computed(
    () => this.access.isStudent() && !!this.lecture()?.attendeeIds?.includes(this.access.username()),
  );
  protected readonly teacherOptions = computed(() =>
    (this.course()?.teacherIds ?? [])
      .filter((t) => t !== this.lecture()?.employeeId)
      .map((t) => ({ value: t, label: t })),
  );
  protected readonly absentees = computed(() => {
    const present = this.lecture()?.attendeeIds ?? [];
    return (this.course()?.enrolledStudentIds ?? []).filter((s) => !present.includes(s)).map((s) => ({ value: s, label: s }));
  });

  constructor() {
    effect(() => {
      this.id();
      untracked(() => void this.load());
    });
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    const lectureId = Number(this.id());
    const data = await this.task.run(async () => {
      const api = this.session.api;
      const lecture = await api.getLecture(lectureId);
      const [course, notes] = await Promise.all([
        api.getCourse(lecture.courseCode ?? ''),
        api.listNotesByLecture(lectureId),
      ]);
      return { lecture, course, notes };
    });
    if (data) {
      this.lecture.set(data.lecture);
      this.course.set(data.course);
      this.notes.set(data.notes);
    }
  }

  private async mutate(action: () => Promise<LectureResponse>, notice: string): Promise<void> {
    const lecture = await this.task.run(action, notice);
    if (lecture) {
      this.lecture.set(lecture);
    }
  }

  protected async assignProfessor(): Promise<void> {
    const employeeId = this.newProfessor();
    await this.mutate(
      () => this.session.api.assignLectureProfessor(Number(this.id()), { employeeId }),
      `Lecture handed over to ${employeeId}.`,
    );
    this.newProfessor.set('');
  }

  protected async recordAttendance(): Promise<void> {
    const studentId = this.attendee();
    await this.mutate(() => this.session.api.recordLectureAttendance(Number(this.id()), studentId), `Recorded ${studentId}.`);
    this.attendee.set('');
  }

  protected removeAttendance(studentId: string): Promise<void> {
    return this.mutate(() => this.session.api.removeLectureAttendance(Number(this.id()), studentId), `Removed ${studentId}.`);
  }

  protected reschedule(): void {
    void this.router.navigate(['/university', 'lectures', this.id(), 'edit']);
  }

  protected writeNote(): void {
    void this.router.navigate(['/university', 'notes', 'new'], { queryParams: { lectureId: this.id() } });
  }

  protected open(section: string, id: string): void {
    void this.router.navigate(['/university', section, id]);
  }

  protected async cancel(): Promise<void> {
    const done = await this.task.run(async () => {
      await this.session.api.cancelLecture(Number(this.id()));
      return true;
    });
    this.confirming.set(false);
    if (done) {
      await this.router.navigate(['/university', 'lectures'], { replaceUrl: true });
    }
  }
}

@Component({
  selector: 'app-lecture-form-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page [title]="id() ? 'Reschedule lecture' : 'Schedule lecture'" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (!id()) {
        <app-choice label="Course" [options]="courseOptions()" [(selected)]="course" empty="You teach no courses." />
        <app-choice label="Delivering professor" [options]="teacherOptions()" [(selected)]="professor" empty="Choose a course first." />
        <app-field label="Topic" [(value)]="topic" />
      }
      <app-field label="Date (yyyy-MM-dd)" [(value)]="date" />
      <app-field label="Time (HH:mm)" [(value)]="time" />
      <app-field label="Room" [(value)]="room" />
      <app-button [label]="id() ? 'Save schedule' : 'Schedule lecture'" [disabled]="task.loading() || !valid()" (press)="save()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class LectureFormScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  private readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input<string>();
  readonly courseCode = input<string>();
  protected readonly courses = signal<CourseResponse[]>([]);
  protected readonly course = signal('');
  protected readonly professor = signal('');
  protected readonly topic = signal('');
  protected readonly date = signal(today());
  protected readonly time = signal('09:00');
  protected readonly room = signal('');

  protected readonly courseOptions = computed(() =>
    this.courses()
      .filter((c) => !this.access.isProfessor() || c.teacherIds?.includes(this.access.username()))
      .map((c) => ({ value: c.courseCode ?? '', label: c.courseCode ?? '' })),
  );
  protected readonly teacherOptions = computed(() =>
    (this.courses().find((c) => c.courseCode === this.course())?.teacherIds ?? []).map((t) => ({ value: t, label: t })),
  );
  protected readonly valid = computed(
    () =>
      DATE_PATTERN.test(this.date().trim()) &&
      TIME_PATTERN.test(this.time().trim()) &&
      !!this.room().trim() &&
      (!!this.id() || (!!this.course() && !!this.professor() && !!this.topic().trim())),
  );

  constructor() {
    effect(() => {
      const id = this.id();
      const preset = this.courseCode();
      untracked(() => {
        if (id) {
          void this.prefill(Number(id));
        } else {
          if (preset) {
            this.course.set(preset);
          }
          void this.loadCourses();
        }
      });
    });
    effect(() => {
      const teachers = this.teacherOptions();
      untracked(() => {
        const mine = this.access.username();
        if (!teachers.some((t) => t.value === this.professor())) {
          this.professor.set(teachers.find((t) => t.value === mine)?.value ?? '');
        }
      });
    });
  }

  private async loadCourses(): Promise<void> {
    const courses = await this.task.run(() => this.session.api.listCourses());
    if (courses) {
      this.courses.set(courses);
    }
  }

  private async prefill(id: number): Promise<void> {
    const lecture = await this.task.run(() => this.session.api.getLecture(id));
    if (lecture) {
      this.date.set(lecture.date ?? today());
      this.time.set((lecture.time ?? '').slice(0, 5));
      this.room.set(lecture.room ?? '');
    }
  }

  protected async save(): Promise<void> {
    const id = this.id();
    const date = this.date().trim();
    const time = this.time().trim();
    const room = this.room().trim();
    const saved = await this.task.run(() =>
      id
        ? this.session.api.rescheduleLecture(Number(id), { date, time, room })
        : this.session.api.scheduleLecture({
            courseCode: this.course(),
            employeeId: this.professor(),
            date,
            time,
            topic: this.topic().trim(),
            room,
          }),
    );
    if (saved) {
      await this.router.navigate(['/university', 'lectures', String(saved.id ?? id)], { replaceUrl: true });
    }
  }
}