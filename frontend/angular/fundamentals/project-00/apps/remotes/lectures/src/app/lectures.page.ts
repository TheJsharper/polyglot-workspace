import { ChangeDetectionStrategy, Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import type { LectureResponse } from '@project-00/services-api';
import {
  courseKey,
  lectureKey,
  selectIsAdmin,
  selectIsProfessor,
  selectIsStudent,
  selectResourceView,
  selectSchedulableCourses,
  selectUsername,
  studentKey,
  UniversityActions,
  universityFeature,
  UniversityOperation,
} from '@project-00/services-university-state';
import { UniversityFrameComponent } from '@project-00/views-university';

@Component({
  selector: 'app-lectures-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, UniversityFrameComponent],
  template: `
    <app-university-frame resource="lectures">
      <section class="workspace-grid">
        <div class="panel">
          <div class="panel-heading"><h2>Lectures <span class="count">{{ filtered().length }}</span></h2>
            @if (schedulableCourses().length) { <button type="button" (click)="beginCreate()">Schedule lecture</button> }
          </div>
          @if (!lectures().length && !view().loading) { <p class="empty">No lectures are available for your courses.</p> }
          <ul class="record-list">
            @for (lecture of filtered(); track lectureKey(lecture)) {
              <li><button type="button" class="record" [class.selected]="selectedKey() === lectureKey(lecture)" (click)="select(lecture)">
                <strong>{{ lecture.topic || 'Lecture' }} · {{ lecture.courseCode || 'Unknown course' }}</strong>
                <span>{{ lecture.date || 'Date pending' }} {{ lecture.time || '' }} · {{ lecture.room || 'Room pending' }}</span>
              </button></li>
            }
          </ul>
          @if (view().createMode) {
            <form class="form" (ngSubmit)="schedule()">
              <h3>Schedule lecture</h3>
              <label>Course <select name="lectureCourse" [(ngModel)]="newCourseCode" required><option value="">Choose course</option>
                @for (course of schedulableCourses(); track courseKey(course)) { <option [value]="course.courseCode">{{ course.courseCode }} · {{ course.title }}</option> }
              </select></label>
              @if (isAdmin()) {
                <label>Professor <select name="lectureProfessor" [(ngModel)]="newProfessor" required><option value="">Choose professor</option>
                  @for (employeeId of newCourseTeachers(); track employeeId) { <option [value]="employeeId">{{ employeeId }}</option> }
                </select></label>
              }
              <label>Date <input name="lectureDate" [(ngModel)]="newDate" type="date" required /></label>
              <label>Time <input name="lectureTime" [(ngModel)]="newTime" type="time" required /></label>
              <label>Topic <input name="lectureTopic" [(ngModel)]="newTopic" required /></label>
              <label>Room <input name="lectureRoom" [(ngModel)]="newRoom" required /></label>
              <div class="actions"><button type="submit" [disabled]="view().loading">Schedule</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
            </form>
          }
        </div>
        <article class="panel detail">
          @if (current(); as lecture) {
            <h2>{{ lecture.topic || 'Lecture details' }}</h2>
            <dl><dt>Course</dt><dd>{{ lecture.courseCode || '—' }}</dd><dt>Professor</dt><dd>{{ lecture.employeeId || '—' }}</dd><dt>When</dt><dd>{{ lecture.date || '—' }} {{ lecture.time || '' }}</dd><dt>Room</dt><dd>{{ lecture.room || '—' }}</dd></dl>
            <h3>Attendance</h3>
            <ul class="plain-list">@for (studentId of lecture.attendeeIds || []; track studentId) { <li>{{ studentId }}
              @if (canManage(lecture)) { <button type="button" class="text-button" (click)="removeAttendance(lecture, studentId)">Remove</button> }
            </li> } @empty { <li>No attendance recorded</li> }</ul>
            @if (isStudent() && isEnrolled(lecture)) {
              @if (attends(lecture)) { <button type="button" class="quiet" [disabled]="view().loading" (click)="leave(lecture)">Leave lecture</button> }
              @else { <button type="button" [disabled]="view().loading" (click)="attend(lecture)">Attend me</button> }
            }
            @if (canManage(lecture)) {
              <div class="inline-form"><label for="attendance-choice">Record attendance</label><select id="attendance-choice" [(ngModel)]="attendanceChoice"><option value="">Choose student</option>
                @for (student of students(); track studentKey(student)) { <option [value]="student.studentId">{{ student.name }} ({{ student.studentId }})</option> }
              </select><button type="button" [disabled]="!attendanceChoice() || view().loading" (click)="recordAttendance(lecture)">Add</button></div>
              <form class="form separated" (ngSubmit)="reschedule(lecture)">
                <h3>Reschedule lecture</h3>
                <label>Date <input name="editLectureDate" [(ngModel)]="editDate" type="date" required /></label>
                <label>Time <input name="editLectureTime" [(ngModel)]="editTime" type="time" required /></label>
                <label>Room <input name="editLectureRoom" [(ngModel)]="editRoom" required /></label>
                <button type="submit" [disabled]="view().loading">Save schedule</button>
              </form>
            }
            @if (canManage(lecture)) {
              <div class="inline-form separated"><label for="lecture-professor-choice">Assign professor</label><select id="lecture-professor-choice" [(ngModel)]="professorChoice"><option value="">Choose professor</option>
                @for (employeeId of teachersOf(lecture); track employeeId) { <option [value]="employeeId">{{ employeeId }}</option> }
              </select><button type="button" [disabled]="!professorChoice() || view().loading" (click)="assignProfessor(lecture)">Assign</button></div>
            }
            @if (canManage(lecture)) { <button type="button" class="danger separated" (click)="cancelLecture(lecture)">Cancel lecture</button> }
          } @else { <p class="empty">Select a lecture to view schedule and attendance.</p> }
        </article>
      </section>
    </app-university-frame>
  `,
})
export class LecturesPage {
  private readonly store = inject(Store);
  protected readonly courseKey = courseKey;
  protected readonly lectureKey = lectureKey;
  protected readonly studentKey = studentKey;
  protected readonly view = this.store.selectSignal(selectResourceView('lectures'));
  protected readonly lectures = this.store.selectSignal(universityFeature.selectLectures);
  protected readonly courses = this.store.selectSignal(universityFeature.selectCourses);
  protected readonly students = this.store.selectSignal(universityFeature.selectStudents);
  protected readonly filtered = this.store.selectSignal(universityFeature.selectFilteredLectures);
  protected readonly current = this.store.selectSignal(universityFeature.selectCurrentLecture);
  private readonly selection = this.store.selectSignal(universityFeature.selectSelectedLecture);
  protected readonly selectedKey = computed(() => this.selection().key);
  protected readonly schedulableCourses = this.store.selectSignal(selectSchedulableCourses);
  protected readonly isAdmin = this.store.selectSignal(selectIsAdmin);
  protected readonly isProfessor = this.store.selectSignal(selectIsProfessor);
  protected readonly isStudent = this.store.selectSignal(selectIsStudent);
  private readonly username = this.store.selectSignal(selectUsername);

  protected readonly newCourseCode = signal('');
  protected readonly newProfessor = signal('');
  protected readonly newCourseTeachers = computed(() =>
    this.courses().find((course) => course.courseCode === this.newCourseCode())?.teacherIds || []);
  protected readonly newDate = signal('');
  protected readonly newTime = signal('');
  protected readonly newTopic = signal('');
  protected readonly newRoom = signal('');
  protected readonly editDate = signal('');
  protected readonly editTime = signal('');
  protected readonly editRoom = signal('');
  protected readonly professorChoice = signal('');
  protected readonly attendanceChoice = signal('');

  constructor() {
    effect(() => {
      const lecture = this.current();
      untracked(() => {
        this.editDate.set(lecture?.date || '');
        this.editTime.set((lecture?.time || '').slice(0, 5));
        this.editRoom.set(lecture?.room || '');
        this.professorChoice.set(lecture?.employeeId || '');
      });
    });
  }

  protected canManage(lecture: LectureResponse): boolean {
    if (this.isAdmin()) return true;
    const username = this.username();
    return this.isProfessor() && this.courses().some((course) =>
      course.courseCode === lecture.courseCode && course.teacherIds?.includes(username));
  }

  protected isEnrolled(lecture: LectureResponse): boolean {
    const username = this.username();
    return this.courses().some((course) =>
      course.courseCode === lecture.courseCode && course.enrolledStudentIds?.includes(username));
  }

  protected attends(lecture: LectureResponse): boolean {
    return (lecture.attendeeIds || []).includes(this.username());
  }

  protected attend(item: LectureResponse): void {
    if (item.id == null) return;
    this.execute('You are attending this lecture.', { type: 'recordLectureAttendance', lectureId: item.id, studentId: this.username() });
  }

  protected leave(item: LectureResponse): void {
    if (item.id == null) return;
    this.execute('You left this lecture.', { type: 'removeLectureAttendance', lectureId: item.id, studentId: this.username() });
  }

  protected teachersOf(lecture: LectureResponse): string[] {
    return this.courses().find((course) => course.courseCode === lecture.courseCode)?.teacherIds || [];
  }

  protected select(item: LectureResponse): void {
    this.store.dispatch(UniversityActions.select({ resource: 'lectures', key: lectureKey(item), item }));
  }

  protected beginCreate(): void {
    this.newCourseCode.set('');
    this.newProfessor.set('');
    this.newDate.set('');
    this.newTime.set('');
    this.newTopic.set('');
    this.newRoom.set('');
    this.store.dispatch(UniversityActions.beginCreate({ resource: 'lectures' }));
  }

  protected cancelCreate(): void {
    this.store.dispatch(UniversityActions.cancelCreate({ resource: 'lectures' }));
  }

  protected schedule(): void {
    this.execute('Lecture scheduled.', {
      type: 'scheduleLecture',
      request: {
        courseCode: this.newCourseCode(),
        employeeId: this.isAdmin() ? this.newProfessor() : this.username(),
        date: this.newDate(),
        time: this.newTime(),
        topic: this.newTopic().trim(),
        room: this.newRoom().trim(),
      },
    }, true);
  }

  protected reschedule(item: LectureResponse): void {
    if (item.id == null) return;
    this.execute('Lecture schedule updated.', {
      type: 'rescheduleLecture',
      lectureId: item.id,
      request: { date: this.editDate(), time: this.editTime(), room: this.editRoom().trim() },
    });
  }

  protected assignProfessor(item: LectureResponse): void {
    const employeeId = this.professorChoice();
    if (item.id == null || !employeeId) return;
    this.execute('Professor assigned to lecture.', { type: 'assignLectureProfessor', lectureId: item.id, request: { employeeId } });
  }

  protected cancelLecture(item: LectureResponse): void {
    if (item.id == null || !globalThis.confirm(`Cancel lecture “${item.topic || item.id}”?`)) return;
    this.execute('Lecture cancelled.', { type: 'cancelLecture', lectureId: item.id });
  }

  protected recordAttendance(item: LectureResponse): void {
    const studentId = this.attendanceChoice();
    if (item.id == null || !studentId) return;
    this.execute(`Attendance recorded for ${studentId}.`, { type: 'recordLectureAttendance', lectureId: item.id, studentId });
    this.attendanceChoice.set('');
  }

  protected removeAttendance(item: LectureResponse, studentId: string): void {
    if (item.id == null) return;
    this.execute(`Attendance removed for ${studentId}.`, { type: 'removeLectureAttendance', lectureId: item.id, studentId });
  }

  private execute(success: string, operation: UniversityOperation, closeCreate = false): void {
    this.store.dispatch(UniversityActions.execute({ resource: 'lectures', success, operation, closeCreate }));
  }
}
