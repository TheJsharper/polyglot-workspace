import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Router } from '@angular/router';
import type { NotesResponse } from '@project-00/services-api';
import { UniversitySession } from '@project-00/services-session';
import { Access } from '../access.ts';
import { injectTask, parseOptionalInt, reloadOnReturn } from '../task.ts';
import { UI } from '../ui.ts';

@Component({
  selector: 'app-notes-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Notes" [loading]="task.loading()" [error]="task.error()" (retry)="load()">
      <app-choice label="Show" [options]="modes()" [(selected)]="mode" />
      @if (mode() === 'student' || mode() === 'lecture') {
        <app-field
          [label]="mode() === 'student' ? 'Student id' : 'Lecture id'"
          [(value)]="query"
          [keyboard]="mode() === 'lecture' ? 'number-pad' : 'default'"
        />
        <app-button label="Search notes" [disabled]="!query().trim()" (press)="load()" />
      }
      @for (note of notes(); track note.id) {
        <app-card
          [title]="'Lecture ' + note.lectureId + ' · ' + note.studentId"
          [subtitle]="note.content"
          [badge]="badge(note)"
          (press)="open(note.id)"
        />
      } @empty {
        @if (!task.loading() && !task.error() && loaded()) {
          <text>No notes found.</text>
        }
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class NotesScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  protected readonly notes = signal<NotesResponse[]>([]);
  protected readonly loaded = signal(false);
  protected readonly query = signal('');
  protected readonly mode = signal(
    this.access.isStudent() ? 'mine' : this.access.isProfessor() ? 'pending' : 'student',
  );
  protected readonly modes = computed(() => [
    ...(this.access.isStudent() ? [{ value: 'mine', label: 'My notes' }] : []),
    ...(this.access.isProfessor() ? [{ value: 'pending', label: 'Pending review' }] : []),
    ...(this.access.isStaff()
      ? [
          { value: 'student', label: 'By student' },
          { value: 'lecture', label: 'By lecture' },
        ]
      : []),
  ]);

  constructor() {
    effect(() => {
      const mode = this.mode();
      untracked(() => {
        this.notes.set([]);
        this.loaded.set(false);
        if (mode === 'mine' || mode === 'pending') {
          void this.load();
        }
      });
    });
    reloadOnReturn(() => void this.load());
  }

  protected badge(note: NotesResponse): string {
    if (note.grade != null) {
      return `${note.grade}/100`;
    }
    return note.privateNote ? 'Private' : '';
  }

  protected async load(): Promise<void> {
    const api = this.session.api;
    const mode = this.mode();
    const query = this.query().trim();
    let action: (() => Promise<NotesResponse[]>) | undefined;
    if (mode === 'mine') {
      action = () => api.listNotesByStudent(this.access.username());
    } else if (mode === 'pending') {
      action = () => api.listPendingReviewNotes();
    } else if (mode === 'student' && query) {
      action = () => api.listNotesByStudent(query);
    } else if (mode === 'lecture' && query) {
      action = () => api.listNotesByLecture(Number(query));
    }
    if (!action) {
      return;
    }
    const list = await this.task.run(action);
    if (list) {
      this.notes.set(list);
      this.loaded.set(true);
    }
  }

  protected open(id: number | undefined): void {
    void this.router.navigate(['/university', 'notes', String(id)]);
  }
}

@Component({
  selector: 'app-note-detail-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page title="Note" [loading]="task.loading()" [error]="task.error()" [notice]="task.notice()" (retry)="load()">
      @if (note(); as n) {
        <app-section [title]="'Lecture ' + n.lectureId">
          <text>By {{ n.studentId }} · {{ n.privateNote ? 'Private' : 'Shared' }}</text>
          <text>{{ n.timestamp }}</text>
          <text>{{ n.content }}</text>
        </app-section>
        <app-card title="Open lecture" [subtitle]="'Lecture ' + n.lectureId" (press)="openLecture(n.lectureId)" />
        @if (isAuthor()) {
          <app-row>
            <app-button label="Edit note" variant="secondary" (press)="edit()" />
            <app-button label="Delete note" variant="danger" (press)="confirming.set(true)" />
          </app-row>
        }
        @if (confirming()) {
          <app-confirm message="Delete this note?" (confirm)="remove()" (cancel)="confirming.set(false)" />
        }
        <app-section title="Review">
          @if (n.grade != null) {
            <text>Grade {{ n.grade }}/100 by {{ n.reviewerId }}</text>
            @if (n.reviewComment) {
              <text>{{ n.reviewComment }}</text>
            }
          } @else {
            <text>Not reviewed yet.</text>
          }
          @if (canReview()) {
            <app-field label="Grade (0-100)" [(value)]="grade" keyboard="number-pad" />
            <app-field label="Comment" [(value)]="comment" [multiline]="true" />
            <app-button label="Submit review" [disabled]="task.loading() || !gradeValid()" (press)="review()" />
          }
        </app-section>
      }
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class NoteDetailScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input.required<string>();
  protected readonly note = signal<NotesResponse | null>(null);
  protected readonly canReview = signal(false);
  protected readonly confirming = signal(false);
  protected readonly grade = signal('');
  protected readonly comment = signal('');
  protected readonly isAuthor = computed(() => !!this.note() && this.note()?.studentId === this.access.username());
  protected readonly gradeValid = computed(() => {
    const grade = parseOptionalInt(this.grade());
    return grade !== undefined && grade >= 0 && grade <= 100;
  });

  constructor() {
    effect(() => {
      this.id();
      untracked(() => void this.load());
    });
    reloadOnReturn(() => void this.load());
  }

  protected async load(): Promise<void> {
    this.canReview.set(false);
    const data = await this.task.run(async () => {
      const note = await this.session.api.getNote(Number(this.id()));
      if (!this.access.isProfessor() || note.lectureId == null) {
        return { note, canReview: false };
      }
      const lecture = await this.session.api.getLecture(note.lectureId);
      const course = await this.session.api.getCourse(lecture.courseCode ?? '');
      return { note, canReview: !!course.teacherIds?.includes(this.access.username()) };
    });
    if (data) {
      this.note.set(data.note);
      this.canReview.set(data.canReview);
    }
  }

  protected async review(): Promise<void> {
    const grade = parseOptionalInt(this.grade());
    if (grade === undefined) {
      return;
    }
    const reviewed = await this.task.run(
      () =>
        this.session.api.reviewNote(Number(this.id()), {
          employeeId: this.access.username(),
          grade,
          comment: this.comment().trim() || null,
        }),
      'Review saved.',
    );
    if (reviewed) {
      this.note.set(reviewed);
      this.grade.set('');
      this.comment.set('');
    }
  }

  protected edit(): void {
    void this.router.navigate(['/university', 'notes', this.id(), 'edit']);
  }

  protected openLecture(id: number | undefined): void {
    void this.router.navigate(['/university', 'lectures', String(id)]);
  }

  protected async remove(): Promise<void> {
    const done = await this.task.run(async () => {
      await this.session.api.deleteNote(Number(this.id()));
      return true;
    });
    this.confirming.set(false);
    if (done) {
      await this.router.navigate(['/university', 'notes'], { replaceUrl: true });
    }
  }
}

@Component({
  selector: 'app-note-form-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI],
  template: `
    <app-page [title]="id() ? 'Edit note' : 'Write note'" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (!id()) {
        <app-field label="Lecture id" [(value)]="lectureId" keyboard="number-pad" />
      }
      <app-field label="Content" [(value)]="content" [multiline]="true" />
      <app-choice label="Visibility" [options]="visibilities" [(selected)]="visibility" />
      <app-button label="Save note" [disabled]="task.loading() || !valid()" (press)="save()" />
    </app-page>
  `,
  styles: ':host { flex: 1; }',
})
export class NoteFormScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  private readonly access = inject(Access);
  protected readonly task = injectTask();
  readonly id = input<string>();
  /** Preselects the lecture when opened from a lecture screen. */
  readonly lectureIdParam = input<string>(undefined, { alias: 'lectureId' });
  protected readonly lectureId = signal('');
  protected readonly content = signal('');
  protected readonly visibility = signal('shared');
  protected readonly visibilities = [
    { value: 'shared', label: 'Shared' },
    { value: 'private', label: 'Private' },
  ];
  protected readonly valid = computed(
    () => !!this.content().trim() && (!!this.id() || parseOptionalInt(this.lectureId()) !== undefined),
  );

  constructor() {
    effect(() => {
      const id = this.id();
      const preset = this.lectureIdParam();
      untracked(() => {
        if (preset) {
          this.lectureId.set(preset);
        }
        if (id) {
          void this.prefill(Number(id));
        }
      });
    });
  }

  private async prefill(id: number): Promise<void> {
    const note = await this.task.run(() => this.session.api.getNote(id));
    if (note) {
      this.content.set(note.content ?? '');
      this.visibility.set(note.privateNote ? 'private' : 'shared');
    }
  }

  protected async save(): Promise<void> {
    const id = this.id();
    const content = this.content().trim();
    const privateNote = this.visibility() === 'private';
    const saved = await this.task.run(() =>
      id
        ? this.session.api.editNote(Number(id), { content, privateNote })
        : this.session.api.writeNote({
            studentId: this.access.username(),
            lectureId: Number(this.lectureId()),
            content,
            privateNote,
          }),
    );
    if (saved) {
      await this.router.navigate(['/university', 'notes', String(saved.id ?? id)], { replaceUrl: true });
    }
  }
}