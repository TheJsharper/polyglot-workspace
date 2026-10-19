import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import type { NotesResponse } from '@project-00/services-api';
import {
  lectureKey,
  NoteLookupMode,
  noteKey,
  selectAttendedLectures,
  selectIsAdmin,
  selectIsProfessor,
  selectIsStudent,
  selectNotesEmptyMessage,
  selectNotesHeading,
  selectResourceView,
  selectUsername,
  UniversityActions,
  universityFeature,
  UniversityOperation,
} from '@project-00/services-university-state';
import { UniversityFrameComponent } from '@project-00/views-university';

@Component({
  selector: 'app-notes-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UniversityFrameComponent],
  template: `
    <app-university-frame resource="notes">
      <section class="workspace-grid">
        <div class="panel">
          <div class="panel-heading"><h2>{{ heading() }} <span class="count">{{ filtered().length }}</span></h2>
            @if (isStudent()) { <button type="button" (click)="beginCreate()">Write notes</button> }
          </div>
          @if ((isAdmin() || isProfessor()) && !isStudent()) {
            <form class="form note-lookup" (ngSubmit)="findNotes()">
              <h3>{{ isAdmin() ? 'Find notes' : 'Review queue or find notes' }}</h3>
              <label>Lookup
                <select name="notesLookupMode" [ngModel]="lookupMode()" (ngModelChange)="setLookupMode($event)">
                  @if (isProfessor()) { <option value="pending">Pending review</option> }
                  <option value="student">By student ID</option><option value="lecture">By lecture ID</option>
                </select>
              </label>
              @if (lookupMode() === 'student') { <label>Student ID <input name="lookupStudentId" [(ngModel)]="lookupStudentId" required /></label> }
              @if (lookupMode() === 'lecture') { <label>Lecture ID <input name="lookupLectureId" [(ngModel)]="lookupLectureId" type="number" min="1" required /></label> }
              <button type="submit" [disabled]="view().loading">{{ lookupMode() === 'pending' ? 'Load review queue' : 'Find notes' }}</button>
            </form>
          }
          @if (!notes().length && !view().loading) { <p class="empty">{{ emptyMessage() }}</p> }
          <ul class="record-list">
            @for (note of filtered(); track noteKey(note)) {
              <li><button type="button" class="record" [class.selected]="selectedKey() === noteKey(note)" (click)="select(note)">
                <strong>Lecture {{ note.lectureId ?? '—' }} · {{ note.studentId || 'Unknown student' }}</strong>
                <span>{{ note.timestamp || 'No timestamp' }} · {{ hasGrade(note) ? 'Grade ' + note.grade : 'Pending review' }}</span>
              </button></li>
            }
          </ul>
          @if (view().createMode && isStudent()) {
            <form class="form" (ngSubmit)="write()">
              <h3>Write notes</h3>
              <label>Lecture <select name="noteLecture" [(ngModel)]="newLectureId" required><option [ngValue]="null">Choose a lecture</option>
                @for (lecture of attendedLectures(); track lectureKey(lecture)) { <option [ngValue]="lecture.id">{{ lecture.courseCode }} · {{ lecture.topic }} · {{ lecture.date }}</option> }
              </select></label>
              @if (!attendedLectures().length) { <p class="empty">Notes can only be written for lectures where your attendance was recorded.</p> }
              <label>Notes <textarea name="noteContent" [(ngModel)]="newContent" rows="6" required></textarea></label>
              <label class="check"><input name="privateNote" [(ngModel)]="newPrivate" type="checkbox" /> Keep these notes private</label>
              <div class="actions"><button type="submit" [disabled]="view().loading">Save notes</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
            </form>
          }
        </div>
        <article class="panel detail">
          @if (current(); as note) {
            <h2>Note details</h2>
            <dl><dt>Student</dt><dd>{{ note.studentId || '—' }}</dd><dt>Lecture</dt><dd>{{ note.lectureId ?? '—' }}</dd><dt>Written</dt><dd>{{ note.timestamp || '—' }}</dd><dt>Privacy</dt><dd>{{ note.privateNote ? 'Private' : 'Shared' }}</dd><dt>Grade</dt><dd>{{ note.grade ?? 'Pending' }}</dd><dt>Review comment</dt><dd>{{ note.reviewComment || '—' }}</dd></dl>
            @if (isStudent() && note.studentId === username()) {
              <form class="form" (ngSubmit)="edit(note)">
                <h3>Edit notes</h3><label>Notes <textarea name="editNoteContent" [(ngModel)]="editContent" rows="7" required></textarea></label>
                <label class="check"><input name="editPrivateNote" [(ngModel)]="editPrivate" type="checkbox" /> Keep these notes private</label>
                <div class="actions"><button type="submit" [disabled]="view().loading">Save changes</button><button type="button" class="danger" (click)="remove(note)">Delete notes</button></div>
              </form>
            }
            @if (isProfessor() || isAdmin()) {
              <p class="note-content">{{ note.content }}</p>
              @if (canReview(note)) {
                <form class="form" (ngSubmit)="review(note)">
                  <h3>Review notes</h3><label>Grade <input name="reviewGrade" [(ngModel)]="reviewGrade" type="number" min="0" max="100" required /></label>
                  <label>Comment <textarea name="reviewComment" [(ngModel)]="reviewComment" rows="3"></textarea></label>
                  <button type="submit" [disabled]="view().loading">Submit review</button>
                </form>
              }
            }
          } @else { <p class="empty">Select a note to view its details.</p> }
        </article>
      </section>
    </app-university-frame>
  `,
})
export class NotesPage {
  private readonly store = inject(Store);
  protected readonly noteKey = noteKey;
  protected readonly lectureKey = lectureKey;
  protected readonly view = this.store.selectSignal(selectResourceView('notes'));
  protected readonly notes = this.store.selectSignal(universityFeature.selectNotes);
  private readonly courses = this.store.selectSignal(universityFeature.selectCourses);
  protected readonly filtered = this.store.selectSignal(universityFeature.selectFilteredNotes);
  protected readonly current = this.store.selectSignal(universityFeature.selectCurrentNote);
  private readonly selection = this.store.selectSignal(universityFeature.selectSelectedNote);
  protected readonly selectedKey = computed(() => this.selection().key);
  protected readonly lookupMode = this.store.selectSignal(universityFeature.selectNoteLookupMode);
  protected readonly attendedLectures = this.store.selectSignal(selectAttendedLectures);
  protected readonly heading = this.store.selectSignal(selectNotesHeading);
  protected readonly emptyMessage = this.store.selectSignal(selectNotesEmptyMessage);
  protected readonly isAdmin = this.store.selectSignal(selectIsAdmin);
  protected readonly isProfessor = this.store.selectSignal(selectIsProfessor);
  protected readonly isStudent = this.store.selectSignal(selectIsStudent);
  protected readonly username = this.store.selectSignal(selectUsername);

  protected readonly lookupStudentId = signal('');
  protected readonly lookupLectureId = signal<number | null>(null);
  protected readonly newLectureId = signal<number | null>(null);
  protected readonly newContent = signal('');
  protected readonly newPrivate = signal(false);
  protected readonly editContent = signal('');
  protected readonly editPrivate = signal(false);
  protected readonly reviewGrade = signal<number | null>(null);
  protected readonly reviewComment = signal('');

  constructor() {
    effect(() => {
      const note = this.current();
      untracked(() => {
        this.editContent.set(note?.content || '');
        this.editPrivate.set(note?.privateNote ?? false);
        this.reviewGrade.set(note?.grade ?? null);
        this.reviewComment.set(note?.reviewComment || '');
      });
    });
  }

  protected hasGrade(note: NotesResponse): boolean { return note.grade !== null && note.grade !== undefined; }

  /** Professors may review only notes for lectures of courses they teach (course code comes from the detail lookup). */
  protected canReview(note: NotesResponse): boolean {
    const { key, courseCode } = this.selection();
    const username = this.username();
    return this.isProfessor() && courseCode !== '' && key === noteKey(note) &&
      this.courses().some((course) => course.courseCode === courseCode && course.teacherIds?.includes(username));
  }

  protected setLookupMode(value: string): void {
    if (value === 'pending' || value === 'student' || value === 'lecture') {
      this.store.dispatch(UniversityActions.setNoteLookupMode({ mode: value as NoteLookupMode }));
    }
  }

  protected findNotes(): void {
    const mode = this.lookupMode();
    const studentId = this.lookupStudentId().trim();
    const lectureId = this.lookupLectureId();
    if (mode === 'student' && !studentId) return;
    if (mode === 'lecture' && (!lectureId || lectureId < 1)) return;
    this.store.dispatch(UniversityActions.findNotes({ mode, studentId, lectureId }));
  }

  protected select(item: NotesResponse): void {
    this.store.dispatch(UniversityActions.select({ resource: 'notes', key: noteKey(item), item }));
  }

  protected beginCreate(): void {
    this.newLectureId.set(null);
    this.newContent.set('');
    this.newPrivate.set(false);
    this.store.dispatch(UniversityActions.beginCreate({ resource: 'notes' }));
  }

  protected cancelCreate(): void {
    this.store.dispatch(UniversityActions.cancelCreate({ resource: 'notes' }));
  }

  protected write(): void {
    const studentId = this.username();
    const lectureId = this.newLectureId();
    if (!studentId || lectureId == null) return;
    this.execute('Notes saved.', {
      type: 'writeNote',
      request: { studentId, lectureId, content: this.newContent().trim(), privateNote: this.newPrivate() },
    }, true);
  }

  protected edit(item: NotesResponse): void {
    if (item.id == null) return;
    this.execute('Notes updated.', {
      type: 'editNote',
      notesId: item.id,
      request: { content: this.editContent().trim(), privateNote: this.editPrivate() },
    });
  }

  protected remove(item: NotesResponse): void {
    if (item.id == null || !globalThis.confirm('Delete these notes permanently?')) return;
    this.execute('Notes deleted.', { type: 'deleteNote', notesId: item.id });
  }

  protected review(item: NotesResponse): void {
    const employeeId = this.username();
    const grade = this.reviewGrade();
    if (!employeeId || item.id == null || grade == null) return;
    this.execute('Note review submitted.', {
      type: 'reviewNote',
      notesId: item.id,
      request: { employeeId, grade, comment: this.reviewComment().trim() || null },
    });
  }

  private execute(success: string, operation: UniversityOperation, closeCreate = false): void {
    this.store.dispatch(UniversityActions.execute({ resource: 'notes', success, operation, closeCreate }));
  }
}
