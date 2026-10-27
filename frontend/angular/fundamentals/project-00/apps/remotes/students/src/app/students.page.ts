import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import type { StudentResponse } from '@project-00/services-api';
import {
  selectIsAdmin,
  selectIsStudent,
  selectResourceView,
  selectUsername,
  studentKey,
  UniversityActions,
  universityFeature,
} from '@project-00/services-university-state';
import { UniversityFrameComponent } from '@project-00/views-university';

@Component({
  selector: 'app-students-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UniversityFrameComponent],
  template: `
    <app-university-frame resource="students">
      <section class="workspace-grid">
        <div class="panel">
          <div class="panel-heading"><h2>Students <span class="count">{{ filtered().length }}</span></h2>
            @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add student</button> }
          </div>
          @if (!students().length && !view().loading) { <p class="empty">No student records are available.</p> }
          <ul class="record-list">
            @for (student of filtered(); track studentKey(student)) {
              <li><button type="button" class="record" [class.selected]="selectedKey() === studentKey(student)" (click)="select(student)">
                <strong>{{ student.name || 'Unnamed student' }}</strong><span [title]="student.studentId || ''">{{ student.studentId || 'No student ID' }}</span>
              </button></li>
            }
          </ul>
          @if (view().createMode) {
            <form class="form" (ngSubmit)="create()">
              <h3>New student</h3>
              <label>Student ID <input name="newStudentId" [(ngModel)]="newId" required /></label>
              <label>Full name <input name="newStudentName" [(ngModel)]="newName" required /></label>
              <label>Enrollment year <input name="newStudentYear" [(ngModel)]="newYear" type="number" min="1900" /></label>
              <div class="actions"><button type="submit" [disabled]="view().loading">Create student</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
            </form>
          }
        </div>
        <article class="panel detail">
          @if (current(); as student) {
            <h2>{{ student.name || 'Student details' }}</h2>
            <dl><dt>Student ID</dt><dd>{{ student.studentId || '—' }}</dd><dt>Record number</dt><dd>{{ student.id ?? '—' }}</dd></dl>
            @if (canEdit(student)) {
              <form class="form" (ngSubmit)="update(student)">
                <h3>Edit student</h3>
                <label>Full name <input name="studentEditName" [(ngModel)]="editName" required /></label>
                <label>Enrollment year <input name="studentEditYear" [(ngModel)]="editYear" type="number" min="1900" /></label>
                <div class="actions"><button type="submit" [disabled]="view().loading">Save changes</button>
                  @if (isAdmin()) { <button type="button" class="danger" (click)="remove(student)">Delete student</button> }
                </div>
              </form>
            }
          } @else { <p class="empty">Select a student to view details.</p> }
        </article>
      </section>
    </app-university-frame>
  `,
})
export class StudentsPage {
  private readonly store = inject(Store);
  protected readonly studentKey = studentKey;
  protected readonly view = this.store.selectSignal(selectResourceView('students'));
  protected readonly students = this.store.selectSignal(universityFeature.selectStudents);
  protected readonly filtered = this.store.selectSignal(universityFeature.selectFilteredStudents);
  protected readonly current = this.store.selectSignal(universityFeature.selectCurrentStudent);
  private readonly selection = this.store.selectSignal(universityFeature.selectSelectedStudent);
  protected readonly selectedKey = computed(() => this.selection().key);
  protected readonly isAdmin = this.store.selectSignal(selectIsAdmin);
  private readonly isStudent = this.store.selectSignal(selectIsStudent);
  private readonly username = this.store.selectSignal(selectUsername);

  protected readonly newId = signal('');
  protected readonly newName = signal('');
  protected readonly newYear = signal<number | null>(null);
  protected readonly editName = signal('');
  protected readonly editYear = signal<number | null>(null);

  constructor() {
    effect(() => {
      const student = this.current();
      untracked(() => {
        this.editName.set(student?.name || '');
        this.editYear.set(student?.enrollmentYear ?? null);
      });
    });
  }

  protected canEdit(item: StudentResponse): boolean {
    return this.isAdmin() || (this.isStudent() && item.studentId === this.username());
  }

  protected select(item: StudentResponse): void {
    this.store.dispatch(UniversityActions.select({ resource: 'students', key: studentKey(item), item }));
  }

  protected beginCreate(): void {
    this.newId.set('');
    this.newName.set('');
    this.newYear.set(new Date().getFullYear());
    this.store.dispatch(UniversityActions.beginCreate({ resource: 'students' }));
  }

  protected cancelCreate(): void {
    this.store.dispatch(UniversityActions.cancelCreate({ resource: 'students' }));
  }

  protected create(): void {
    this.store.dispatch(UniversityActions.execute({
      resource: 'students',
      success: 'Student record created.',
      closeCreate: true,
      operation: {
        type: 'createStudent',
        request: { studentId: this.newId().trim(), name: this.newName().trim(), enrollmentYear: this.newYear() ?? undefined },
      },
    }));
  }

  protected update(item: StudentResponse): void {
    if (!item.studentId) return;
    this.store.dispatch(UniversityActions.execute({
      resource: 'students',
      success: 'Student record updated.',
      operation: {
        type: 'updateStudent',
        studentId: item.studentId,
        request: { name: this.editName().trim(), enrollmentYear: this.editYear() ?? undefined },
      },
    }));
  }

  protected remove(item: StudentResponse): void {
    if (!item.studentId || !globalThis.confirm(`Delete student ${item.studentId}? This may also remove related records.`)) return;
    this.store.dispatch(UniversityActions.execute({
      resource: 'students',
      success: 'Student record deleted.',
      operation: { type: 'deleteStudent', studentId: item.studentId },
    }));
  }
}
