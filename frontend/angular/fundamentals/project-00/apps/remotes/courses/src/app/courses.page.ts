import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import type { CourseResponse } from '@project-00/services-api';
import {
  courseKey,
  professorKey,
  selectIsAdmin,
  selectIsStudent,
  selectResourceView,
  selectUsername,
  UniversityActions,
  universityFeature,
  UniversityOperation,
} from '@project-00/services-university-state';
import { UniversityFrameComponent } from '@project-00/views-university';

@Component({
  selector: 'app-courses-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UniversityFrameComponent],
  template: `
    <app-university-frame resource="courses">
      <section class="workspace-grid">
        <div class="panel">
          <div class="panel-heading"><h2>Courses <span class="count">{{ filtered().length }}</span></h2>
            @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add course</button> }
          </div>
          @if (!courses().length && !view().loading) { <p class="empty">No courses are available.</p> }
          <ul class="record-list">
            @for (course of filtered(); track courseKey(course)) {
              <li><button type="button" class="record" [class.selected]="selectedKey() === courseKey(course)" (click)="select(course)">
                <strong>{{ course.title || 'Untitled course' }}</strong><span>{{ course.courseCode || 'No code' }} · {{ course.credits ?? '—' }} credits</span>
              </button></li>
            }
          </ul>
          @if (view().createMode) {
            <form class="form" (ngSubmit)="create()">
              <h3>New course</h3>
              <label>Course code <input name="newCourseCode" [(ngModel)]="newCode" required /></label>
              <label>Title <input name="newCourseTitle" [(ngModel)]="newTitle" required /></label>
              <label>Credits <input name="newCourseCredits" [(ngModel)]="newCredits" type="number" min="0" /></label>
              <div class="actions"><button type="submit" [disabled]="view().loading">Create course</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
            </form>
          }
        </div>
        <article class="panel detail">
          @if (current(); as course) {
            <h2>{{ course.title || 'Course details' }}</h2>
            <dl><dt>Course code</dt><dd>{{ course.courseCode || '—' }}</dd><dt>Credits</dt><dd>{{ course.credits ?? '—' }}</dd></dl>
            <h3>Assigned professors</h3>
            <ul class="plain-list">@for (employeeId of course.teacherIds || []; track employeeId) { <li>{{ employeeId }}
              @if (isAdmin()) { <button type="button" class="text-button" (click)="unassignTeacher(course, employeeId)">Unassign</button> }
            </li> } @empty { <li>None assigned</li> }</ul>
            @if (isAdmin()) {
              <div class="inline-form"><label for="teacher-choice">Assign professor</label><select id="teacher-choice" [(ngModel)]="teacherChoice"><option value="">Choose professor</option>
                @for (professor of professors(); track professorKey(professor)) { <option [value]="professor.employeeId">{{ professor.name }} ({{ professor.employeeId }})</option> }
              </select><button type="button" [disabled]="!teacherChoice() || view().loading" (click)="assignTeacher(course)">Assign</button></div>
            }
            <h3>Enrolled students</h3>
            <ul class="plain-list">@for (studentId of course.enrolledStudentIds || []; track studentId) { <li>{{ studentId }}
            </li> } @empty { <li>No students enrolled</li> }</ul>
            @if (isStudent() && username()) {
              @if (isEnrolled(course)) { <button type="button" class="quiet" [disabled]="view().loading" (click)="unenroll(course)">Unenroll me</button> }
              @else { <button type="button" [disabled]="view().loading" (click)="enroll(course)">Enroll me</button> }
            }
            @if (canEdit(course)) {
              <form class="form separated" (ngSubmit)="update(course)">
                <h3>Edit course</h3><label>Title <input name="courseEditTitle" [(ngModel)]="editTitle" required /></label>
                <label>Credits <input name="courseEditCredits" [(ngModel)]="editCredits" type="number" min="0" /></label>
                <div class="actions"><button type="submit" [disabled]="view().loading">Save changes</button>@if (isAdmin()) { <button type="button" class="danger" (click)="remove(course)">Delete course</button> }</div>
              </form>
            }
          } @else { <p class="empty">Select a course to view details and enrollment.</p> }
        </article>
      </section>
    </app-university-frame>
  `,
})
export class CoursesPage {
  private readonly store = inject(Store);
  protected readonly courseKey = courseKey;
  protected readonly professorKey = professorKey;
  protected readonly view = this.store.selectSignal(selectResourceView('courses'));
  protected readonly courses = this.store.selectSignal(universityFeature.selectCourses);
  protected readonly professors = this.store.selectSignal(universityFeature.selectProfessors);
  protected readonly filtered = this.store.selectSignal(universityFeature.selectFilteredCourses);
  protected readonly current = this.store.selectSignal(universityFeature.selectCurrentCourse);
  private readonly selection = this.store.selectSignal(universityFeature.selectSelectedCourse);
  protected readonly selectedKey = computed(() => this.selection().key);
  protected readonly isAdmin = this.store.selectSignal(selectIsAdmin);
  protected readonly isStudent = this.store.selectSignal(selectIsStudent);
  protected readonly username = this.store.selectSignal(selectUsername);

  protected readonly newCode = signal('');
  protected readonly newTitle = signal('');
  protected readonly newCredits = signal<number | null>(null);
  protected readonly editTitle = signal('');
  protected readonly editCredits = signal<number | null>(null);
  protected readonly teacherChoice = signal('');

  constructor() {
    effect(() => {
      const course = this.current();
      untracked(() => {
        this.editTitle.set(course?.title || '');
        this.editCredits.set(course?.credits ?? null);
      });
    });
  }

  protected canEdit(course: CourseResponse): boolean {
    return this.isAdmin() || (course.teacherIds || []).includes(this.username());
  }

  protected isEnrolled(course: CourseResponse): boolean {
    return !!this.username() && (course.enrolledStudentIds || []).includes(this.username());
  }

  protected select(item: CourseResponse): void {
    this.store.dispatch(UniversityActions.select({ resource: 'courses', key: courseKey(item), item }));
  }

  protected beginCreate(): void {
    this.newCode.set('');
    this.newTitle.set('');
    this.newCredits.set(0);
    this.store.dispatch(UniversityActions.beginCreate({ resource: 'courses' }));
  }

  protected cancelCreate(): void {
    this.store.dispatch(UniversityActions.cancelCreate({ resource: 'courses' }));
  }

  protected create(): void {
    this.execute('Course created.', {
      type: 'createCourse',
      request: { courseCode: this.newCode().trim(), title: this.newTitle().trim(), credits: this.newCredits() ?? undefined },
    }, true);
  }

  protected update(item: CourseResponse): void {
    if (!item.courseCode) return;
    this.execute('Course updated.', {
      type: 'updateCourse',
      courseCode: item.courseCode,
      request: { title: this.editTitle().trim(), credits: this.editCredits() ?? undefined },
    });
  }

  protected remove(item: CourseResponse): void {
    if (!item.courseCode || !globalThis.confirm(`Delete course ${item.courseCode} and its associated resources?`)) return;
    this.execute('Course deleted.', { type: 'deleteCourse', courseCode: item.courseCode });
  }

  protected assignTeacher(course: CourseResponse): void {
    const employeeId = this.teacherChoice();
    if (!course.courseCode || !employeeId) return;
    this.execute(`Professor ${employeeId} assigned to ${course.courseCode}.`,
      { type: 'assignCourseTeacher', courseCode: course.courseCode, employeeId });
    this.teacherChoice.set('');
  }

  protected unassignTeacher(course: CourseResponse, employeeId: string): void {
    if (!course.courseCode) return;
    this.execute(`Professor ${employeeId} unassigned from ${course.courseCode}.`,
      { type: 'unassignCourseTeacher', courseCode: course.courseCode, employeeId });
  }

  protected enroll(course: CourseResponse): void {
    const studentId = this.username();
    if (!course.courseCode || !studentId) return;
    this.execute(`Student ${studentId} enrolled in ${course.courseCode}.`,
      { type: 'enrollStudentInCourse', courseCode: course.courseCode, studentId });
  }

  protected unenroll(course: CourseResponse): void {
    const studentId = this.username();
    if (!course.courseCode || !studentId) return;
    this.execute(`Student ${studentId} unenrolled from ${course.courseCode}.`,
      { type: 'unenrollStudentFromCourse', courseCode: course.courseCode, studentId });
  }

  private execute(success: string, operation: UniversityOperation, closeCreate = false): void {
    this.store.dispatch(UniversityActions.execute({ resource: 'courses', success, operation, closeCreate }));
  }
}
