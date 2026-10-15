import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UniversitySession } from '@project-00/services-session';
import type {
  CourseResponse,
  LectureResponse,
  NotesResponse,
  ProfessorResponse,
  StudentResponse,
} from '@project-00/services-api';

type Resource = 'students' | 'professors' | 'courses' | 'lectures' | 'notes';
type ApiAction = () => Promise<unknown>;

@Component({
  selector: 'app-university',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule],
  template: `
    <main class="university">
      <header class="page-heading">
        <div>
          <p class="eyebrow">University workspace</p>
          <h1>Resources</h1>
          <p>Manage university records and course activity.</p>
        </div>
        <div class="account">
          <span>{{ session.user()?.username }} · {{ session.user()?.role }}</span>
          <button type="button" class="quiet" (click)="session.logout()">Log out</button>
        </div>
      </header>

      <nav class="resource-tabs" aria-label="University resources">
        <button type="button" [attr.aria-current]="active() === 'students' ? 'page' : null" (click)="open('students')">Students</button>
        <button type="button" [attr.aria-current]="active() === 'professors' ? 'page' : null" (click)="open('professors')">Professors</button>
        <button type="button" [attr.aria-current]="active() === 'courses' ? 'page' : null" (click)="open('courses')">Courses</button>
        <button type="button" [attr.aria-current]="active() === 'lectures' ? 'page' : null" (click)="open('lectures')">Lectures</button>
        <button type="button" [attr.aria-current]="active() === 'notes' ? 'page' : null" (click)="open('notes')">Notes &amp; reviews</button>
      </nav>

      @if (message()) { <p class="notice success" role="status">{{ message() }}</p> }
      @if (error()) { <p class="notice failure" role="alert">{{ error() }}</p> }
      @if (choiceError()) { <p class="notice failure" role="alert">Some selection data could not be loaded: {{ choiceError() }}</p> }
      @if (loading()) { <p class="loading" role="status">Loading {{ activeLabel() }}…</p> }

      <section class="toolbar" aria-label="Resource filters">
        <label class="search">Filter {{ activeLabel() }}
          <input type="search" placeholder="Search by name, ID, title…" [ngModel]="filterQuery()" (ngModelChange)="filterQuery.set($event)" />
        </label>
        <button type="button" class="quiet" (click)="refresh()">Refresh</button>
      </section>

      @if (active() === 'students') {
        <section class="workspace-grid">
          <div class="panel">
            <div class="panel-heading"><h2>Students <span class="count">{{ filteredStudents().length }}</span></h2>
              @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add student</button> }
            </div>
            @if (!students().length && !loading()) { <p class="empty">No student records are available.</p> }
            <ul class="record-list">
              @for (student of filteredStudents(); track studentKey(student)) {
                <li><button type="button" class="record" [class.selected]="selectedStudentKey() === studentKey(student)" (click)="selectStudent(student)">
                  <strong>{{ student.name || 'Unnamed student' }}</strong><span>{{ student.studentId || 'No student ID' }}</span>
                </button></li>
              }
            </ul>
            @if (createMode()) {
              <form class="form" (ngSubmit)="createStudent()">
                <h3>New student</h3>
                <label>Student ID <input name="newStudentId" [(ngModel)]="studentId" required /></label>
                <label>Full name <input name="newStudentName" [(ngModel)]="studentName" required /></label>
                <label>Enrollment year <input name="newStudentYear" [(ngModel)]="studentYear" type="number" min="1900" /></label>
                <div class="actions"><button type="submit" [disabled]="loading()">Create student</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
              </form>
            }
          </div>
          <article class="panel detail">
            @if (selectedStudent(); as student) {
              <h2>{{ student.name || 'Student details' }}</h2>
              <dl><dt>Student ID</dt><dd>{{ student.studentId || '—' }}</dd><dt>Record number</dt><dd>{{ student.id ?? '—' }}</dd></dl>
              @if (canEditStudent(student)) {
                <form class="form" (ngSubmit)="updateStudent(student)">
                  <h3>Edit student</h3>
                  <label>Full name <input name="studentEditName" [(ngModel)]="studentName" required /></label>
                  <label>Enrollment year <input name="studentEditYear" [(ngModel)]="studentYear" type="number" min="1900" /></label>
                  <div class="actions"><button type="submit" [disabled]="loading()">Save changes</button>
                    @if (isAdmin()) { <button type="button" class="danger" (click)="deleteStudent(student)">Delete student</button> }
                  </div>
                </form>
              }
            } @else { <p class="empty">Select a student to view details.</p> }
          </article>
        </section>
      }

      @if (active() === 'professors') {
        <section class="workspace-grid">
          <div class="panel">
            <div class="panel-heading"><h2>Professors <span class="count">{{ filteredProfessors().length }}</span></h2>
              @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add professor</button> }
            </div>
            @if (!professors().length && !loading()) { <p class="empty">No professor records are available.</p> }
            <ul class="record-list">
              @for (professor of filteredProfessors(); track professorKey(professor)) {
                <li><button type="button" class="record" [class.selected]="selectedProfessorKey() === professorKey(professor)" (click)="selectProfessor(professor)">
                  <strong>{{ professor.name || 'Unnamed professor' }}</strong><span>{{ professor.employeeId || 'No employee ID' }} · {{ professor.department || 'No department' }}</span>
                </button></li>
              }
            </ul>
            @if (createMode()) {
              <form class="form" (ngSubmit)="createProfessor()">
                <h3>New professor</h3>
                <label>Employee ID <input name="newProfessorId" [(ngModel)]="professorId" required /></label>
                <label>Full name <input name="newProfessorName" [(ngModel)]="professorName" required /></label>
                <label>Department <input name="newProfessorDepartment" [(ngModel)]="professorDepartment" required /></label>
                <label>Temporary password <input name="newProfessorPassword" [(ngModel)]="professorPassword" type="password" required minlength="8" autocomplete="new-password" /></label>
                <div class="actions"><button type="submit" [disabled]="loading()">Create professor</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
              </form>
            }
          </div>
          <article class="panel detail">
            @if (selectedProfessor(); as professor) {
              <h2>{{ professor.name || 'Professor details' }}</h2>
              <dl><dt>Employee ID</dt><dd>{{ professor.employeeId || '—' }}</dd><dt>Department</dt><dd>{{ professor.department || '—' }}</dd><dt>Record number</dt><dd>{{ professor.id ?? '—' }}</dd></dl>
              @if (isAdmin()) {
                <form class="form" (ngSubmit)="updateProfessor(professor)">
                  <h3>Edit professor</h3>
                  <label>Full name <input name="professorEditName" [(ngModel)]="professorName" required /></label>
                  <label>Department <input name="professorEditDepartment" [(ngModel)]="professorDepartment" required /></label>
                  <div class="actions"><button type="submit" [disabled]="loading()">Save changes</button>
                    @if (isAdmin()) { <button type="button" class="danger" (click)="deleteProfessor(professor)">Delete professor</button> }
                  </div>
                </form>
              }
            } @else { <p class="empty">Select a professor to view details.</p> }
          </article>
        </section>
      }

      @if (active() === 'courses') {
        <section class="workspace-grid">
          <div class="panel">
            <div class="panel-heading"><h2>Courses <span class="count">{{ filteredCourses().length }}</span></h2>
              @if (isAdmin()) { <button type="button" (click)="beginCreate()">Add course</button> }
            </div>
            @if (!courses().length && !loading()) { <p class="empty">No courses are available.</p> }
            <ul class="record-list">
              @for (course of filteredCourses(); track courseKey(course)) {
                <li><button type="button" class="record" [class.selected]="selectedCourseKey() === courseKey(course)" (click)="selectCourse(course)">
                  <strong>{{ course.title || 'Untitled course' }}</strong><span>{{ course.courseCode || 'No code' }} · {{ course.credits ?? '—' }} credits</span>
                </button></li>
              }
            </ul>
            @if (createMode()) {
              <form class="form" (ngSubmit)="createCourse()">
                <h3>New course</h3>
                <label>Course code <input name="newCourseCode" [(ngModel)]="courseCode" required /></label>
                <label>Title <input name="newCourseTitle" [(ngModel)]="courseTitle" required /></label>
                <label>Credits <input name="newCourseCredits" [(ngModel)]="courseCredits" type="number" min="0" /></label>
                <div class="actions"><button type="submit" [disabled]="loading()">Create course</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
              </form>
            }
          </div>
          <article class="panel detail">
            @if (selectedCourse(); as course) {
              <h2>{{ course.title || 'Course details' }}</h2>
              <dl><dt>Course code</dt><dd>{{ course.courseCode || '—' }}</dd><dt>Credits</dt><dd>{{ course.credits ?? '—' }}</dd></dl>
              <h3>Assigned professors</h3>
              <ul class="plain-list">@for (employeeId of course.teacherIds || []; track employeeId) { <li>{{ employeeId }}
                @if (isAdmin()) { <button type="button" class="text-button" (click)="unassignTeacher(course, employeeId)">Unassign</button> }
              </li> } @empty { <li>None assigned</li> }</ul>
              @if (isAdmin()) {
                <div class="inline-form"><label for="teacher-choice">Assign professor</label><select id="teacher-choice" [ngModel]="teacherChoice()" (ngModelChange)="teacherChoice.set($event)"><option value="">Choose professor</option>
                  @for (professor of professors(); track professorKey(professor)) { <option [value]="professor.employeeId">{{ professor.name }} ({{ professor.employeeId }})</option> }
                </select><button type="button" [disabled]="!teacherChoice() || loading()" (click)="assignTeacher(course)">Assign</button></div>
              }
              <h3>Enrolled students</h3>
              <ul class="plain-list">@for (studentId of course.enrolledStudentIds || []; track studentId) { <li>{{ studentId }}
              </li> } @empty { <li>No students enrolled</li> }</ul>
              @if (isStudent() && session.user()?.username) {
                @if (isEnrolled(course)) { <button type="button" class="quiet" [disabled]="loading()" (click)="unenrollStudent(course, session.user()?.username || '')">Unenroll me</button> }
                @else { <button type="button" [disabled]="loading()" (click)="enrollStudent(course, session.user()?.username || '')">Enroll me</button> }
              }
              @if (isAdmin()) {
                <form class="form separated" (ngSubmit)="updateCourse(course)">
                  <h3>Edit course</h3><label>Title <input name="courseEditTitle" [(ngModel)]="courseTitle" required /></label>
                  <label>Credits <input name="courseEditCredits" [(ngModel)]="courseCredits" type="number" min="0" /></label>
                  <div class="actions"><button type="submit" [disabled]="loading()">Save changes</button><button type="button" class="danger" (click)="deleteCourse(course)">Delete course</button></div>
                </form>
              }
            } @else { <p class="empty">Select a course to view details and enrollment.</p> }
          </article>
        </section>
      }

      @if (active() === 'lectures') {
        <section class="workspace-grid">
          <div class="panel">
            <div class="panel-heading"><h2>Lectures <span class="count">{{ filteredLectures().length }}</span></h2>
              @if (isProfessor() && schedulableCourses().length) { <button type="button" (click)="beginCreate()">Schedule lecture</button> }
            </div>
            @if (!lectures().length && !loading()) { <p class="empty">No lectures are available for your courses.</p> }
            <ul class="record-list">
              @for (lecture of filteredLectures(); track lectureKey(lecture)) {
                <li><button type="button" class="record" [class.selected]="selectedLectureKey() === lectureKey(lecture)" (click)="selectLecture(lecture)">
                  <strong>{{ lecture.topic || 'Lecture' }} · {{ lecture.courseCode || 'Unknown course' }}</strong>
                  <span>{{ lecture.date || 'Date pending' }} {{ lecture.time || '' }} · {{ lecture.room || 'Room pending' }}</span>
                </button></li>
              }
            </ul>
            @if (createMode()) {
              <form class="form" (ngSubmit)="scheduleLecture()">
                <h3>Schedule lecture</h3>
                <label>Course <select name="lectureCourse" [(ngModel)]="lectureCourseCode" required><option value="">Choose course</option>
                  @for (course of schedulableCourses(); track courseKey(course)) { <option [value]="course.courseCode">{{ course.courseCode }} · {{ course.title }}</option> }
                </select></label>
                <label>Date <input name="lectureDate" [(ngModel)]="lectureDate" type="date" required /></label>
                <label>Time <input name="lectureTime" [(ngModel)]="lectureTime" type="time" required /></label>
                <label>Topic <input name="lectureTopic" [(ngModel)]="lectureTopic" required /></label>
                <label>Room <input name="lectureRoom" [(ngModel)]="lectureRoom" required /></label>
                <div class="actions"><button type="submit" [disabled]="loading()">Schedule</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
              </form>
            }
          </div>
          <article class="panel detail">
            @if (selectedLecture(); as lecture) {
              <h2>{{ lecture.topic || 'Lecture details' }}</h2>
              <dl><dt>Course</dt><dd>{{ lecture.courseCode || '—' }}</dd><dt>Professor</dt><dd>{{ lecture.employeeId || '—' }}</dd><dt>When</dt><dd>{{ lecture.date || '—' }} {{ lecture.time || '' }}</dd><dt>Room</dt><dd>{{ lecture.room || '—' }}</dd></dl>
              <h3>Attendance</h3>
              <ul class="plain-list">@for (studentId of lecture.attendeeIds || []; track studentId) { <li>{{ studentId }}
                @if (canManageLecture(lecture)) { <button type="button" class="text-button" (click)="removeAttendance(lecture, studentId)">Remove</button> }
              </li> } @empty { <li>No attendance recorded</li> }</ul>
              @if (canManageLecture(lecture)) {
                <div class="inline-form"><label for="attendance-choice">Record attendance</label><select id="attendance-choice" [ngModel]="attendanceChoice()" (ngModelChange)="attendanceChoice.set($event)"><option value="">Choose student</option>
                  @for (student of students(); track studentKey(student)) { <option [value]="student.studentId">{{ student.name }} ({{ student.studentId }})</option> }
                </select><button type="button" [disabled]="!attendanceChoice() || loading()" (click)="recordAttendance(lecture)">Add</button></div>
                <form class="form separated" (ngSubmit)="rescheduleLecture(lecture)">
                  <h3>Reschedule lecture</h3>
                  <label>Date <input name="editLectureDate" [(ngModel)]="lectureDate" type="date" required /></label>
                  <label>Time <input name="editLectureTime" [(ngModel)]="lectureTime" type="time" required /></label>
                  <label>Room <input name="editLectureRoom" [(ngModel)]="lectureRoom" required /></label>
                  <button type="submit" [disabled]="loading()">Save schedule</button>
                </form>
              }
              @if (isAdmin() || canManageLecture(lecture)) {
                <div class="inline-form separated"><label for="lecture-professor-choice">Assign professor</label><select id="lecture-professor-choice" [ngModel]="lectureProfessorId" (ngModelChange)="lectureProfessorId = $event"><option value="">Choose professor</option>
                  @for (employeeId of lectureTeachers(lecture); track employeeId) { <option [value]="employeeId">{{ employeeId }}</option> }
                </select><button type="button" [disabled]="!lectureProfessorId || loading()" (click)="assignLectureProfessor(lecture)">Assign</button></div>
              }
              @if (canManageLecture(lecture)) { <button type="button" class="danger separated" (click)="cancelLecture(lecture)">Cancel lecture</button> }
            } @else { <p class="empty">Select a lecture to view schedule and attendance.</p> }
          </article>
        </section>
      }

      @if (active() === 'notes') {
        <section class="workspace-grid">
          <div class="panel">
            <div class="panel-heading"><h2>{{ notesHeading() }} <span class="count">{{ filteredNotes().length }}</span></h2>
              @if (isStudent()) { <button type="button" (click)="beginCreate()">Write notes</button> }
            </div>
            @if ((isAdmin() || isProfessor()) && !isStudent()) {
              <form class="form note-lookup" (ngSubmit)="findNotes()">
                <h3>{{ isAdmin() ? 'Find notes' : 'Review queue or find notes' }}</h3>
                <label>Lookup
                  <select name="notesLookupMode" [ngModel]="noteLookupMode()" (ngModelChange)="setNoteLookupMode($event)">
                    @if (isProfessor()) { <option value="pending">Pending review</option> }
                    <option value="student">By student ID</option><option value="lecture">By lecture ID</option>
                  </select>
                </label>
                @if (noteLookupMode() === 'student') { <label>Student ID <input name="lookupStudentId" [(ngModel)]="lookupStudentId" required /></label> }
                @if (noteLookupMode() === 'lecture') { <label>Lecture ID <input name="lookupLectureId" [(ngModel)]="lookupLectureId" type="number" min="1" required /></label> }
                <button type="submit" [disabled]="loading()">{{ noteLookupMode() === 'pending' ? 'Load review queue' : 'Find notes' }}</button>
              </form>
            }
            @if (!notes().length && !loading()) { <p class="empty">{{ notesEmptyMessage() }}</p> }
            <ul class="record-list">
              @for (note of filteredNotes(); track note.id) {
                <li><button type="button" class="record" [class.selected]="selectedNoteId() === note.id" (click)="selectNote(note)">
                  <strong>Lecture {{ note.lectureId ?? '—' }} · {{ note.studentId || 'Unknown student' }}</strong>
                  <span>{{ note.timestamp || 'No timestamp' }} · {{ hasGrade(note) ? 'Grade ' + note.grade : 'Pending review' }}</span>
                </button></li>
              }
            </ul>
            @if (createMode() && isStudent()) {
              <form class="form" (ngSubmit)="writeNote()">
                <h3>Write notes</h3>
                <label>Lecture <select name="noteLecture" [(ngModel)]="noteLectureId" required><option [ngValue]="null">Choose a lecture</option>
                  @for (lecture of attendedLectures(); track lectureKey(lecture)) { <option [ngValue]="lecture.id">{{ lecture.courseCode }} · {{ lecture.topic }} · {{ lecture.date }}</option> }
                </select></label>
                @if (!attendedLectures().length) { <p class="empty">Notes can only be written for lectures where your attendance was recorded.</p> }
                <label>Notes <textarea name="noteContent" [(ngModel)]="noteContent" rows="6" required></textarea></label>
                <label class="check"><input name="privateNote" [(ngModel)]="notePrivate" type="checkbox" /> Keep these notes private</label>
                <div class="actions"><button type="submit" [disabled]="loading()">Save notes</button><button type="button" class="quiet" (click)="cancelCreate()">Cancel</button></div>
              </form>
            }
          </div>
          <article class="panel detail">
            @if (selectedNote(); as note) {
              <h2>Note details</h2>
              <dl><dt>Student</dt><dd>{{ note.studentId || '—' }}</dd><dt>Lecture</dt><dd>{{ note.lectureId ?? '—' }}</dd><dt>Written</dt><dd>{{ note.timestamp || '—' }}</dd><dt>Privacy</dt><dd>{{ note.privateNote ? 'Private' : 'Shared' }}</dd><dt>Grade</dt><dd>{{ note.grade ?? 'Pending' }}</dd><dt>Review comment</dt><dd>{{ note.reviewComment || '—' }}</dd></dl>
              @if (isStudent() && note.studentId === session.user()?.username) {
                  <form class="form" (ngSubmit)="editNote(note)">
                    <h3>Edit notes</h3><label>Notes <textarea name="editNoteContent" [(ngModel)]="noteContent" rows="7" required></textarea></label>
                    <label class="check"><input name="editPrivateNote" [(ngModel)]="notePrivate" type="checkbox" /> Keep these notes private</label>
                    <div class="actions"><button type="submit" [disabled]="loading()">Save changes</button><button type="button" class="danger" (click)="deleteNote(note)">Delete notes</button></div>
                  </form>
              }
              @if (isProfessor() || isAdmin()) {
                <p class="note-content">{{ note.content }}</p>
                @if (isProfessor() && canReviewNote(note)) {
                  <form class="form" (ngSubmit)="reviewNote(note)">
                    <h3>Review notes</h3><label>Grade <input name="reviewGrade" [(ngModel)]="reviewGrade" type="number" min="0" max="100" required /></label>
                    <label>Comment <textarea name="reviewComment" [(ngModel)]="reviewComment" rows="3"></textarea></label>
                    <button type="submit" [disabled]="loading()">Submit review</button>
                  </form>
                }
              }
            } @else { <p class="empty">Select a note to view its details.</p> }
          </article>
        </section>
      }
    </main>
  `,
  styles: [`
    :host { display: block; color: #172033; }
    .university { max-width: 76rem; margin: 0 auto; padding: clamp(1rem, 3vw, 2rem); }
    .page-heading, .panel-heading, .toolbar, .account, .actions { display: flex; align-items: center; justify-content: space-between; gap: .8rem; }
    .page-heading { margin-bottom: 1.4rem; }
    h1, h2, h3, p { margin-top: 0; }
    h1 { margin-bottom: .25rem; font-size: clamp(1.7rem, 4vw, 2.3rem); }
    h2 { font-size: 1.25rem; } h3 { margin: 1.25rem 0 .7rem; font-size: 1rem; }
    .eyebrow { margin-bottom: .35rem; color: #4d6284; font-size: .8rem; font-weight: 700; letter-spacing: .08em; text-transform: uppercase; }
    .account { flex-wrap: wrap; justify-content: flex-end; font-size: .9rem; }
    .resource-tabs { display: flex; gap: .35rem; overflow-x: auto; border-bottom: 1px solid #dce2eb; margin-bottom: 1rem; }
    button { border: 0; border-radius: .45rem; padding: .65rem .9rem; background: #174ea6; color: white; font: inherit; font-weight: 650; cursor: pointer; }
    button:hover:not(:disabled) { background: #103d85; }
    button:disabled { cursor: not-allowed; opacity: .55; }
    .resource-tabs button { border-radius: .45rem .45rem 0 0; background: transparent; color: #35445c; white-space: nowrap; }
    .resource-tabs button[aria-current="page"] { background: #e8f0fe; color: #123d80; }
    .quiet { border: 1px solid #cbd5e1; background: white; color: #233149; }
    .quiet:hover:not(:disabled) { background: #f1f5f9; }
    .danger { background: #a52a2a; } .danger:hover:not(:disabled) { background: #821d1d; }
    .toolbar { justify-content: flex-start; margin: 1rem 0; }
    label { display: grid; gap: .35rem; color: #29364c; font-size: .9rem; font-weight: 600; }
    input, select, textarea { width: 100%; box-sizing: border-box; border: 1px solid #b8c3d3; border-radius: .4rem; padding: .62rem .7rem; background: white; color: inherit; font: inherit; }
    input:focus, select:focus, textarea:focus, button:focus-visible { outline: 3px solid #8bb7ff; outline-offset: 2px; }
    .search { min-width: min(26rem, 70vw); }
    .workspace-grid { display: grid; grid-template-columns: minmax(17rem, .8fr) minmax(20rem, 1.2fr); align-items: start; gap: 1rem; }
    .panel { min-width: 0; padding: 1.15rem; border: 1px solid #dce2eb; border-radius: .75rem; background: white; box-shadow: 0 2px 8px #1720330a; }
    .panel-heading { align-items: flex-start; margin-bottom: .8rem; } .panel-heading h2 { margin: .25rem 0; }
    .count { display: inline-grid; min-width: 1.5rem; height: 1.5rem; place-items: center; margin-left: .25rem; border-radius: 99px; background: #edf2f8; color: #40516c; font-size: .76rem; vertical-align: .1rem; }
    .record-list, .plain-list { list-style: none; margin: 0; padding: 0; }
    .record-list { display: grid; gap: .4rem; max-height: 35rem; overflow: auto; }
    .record { display: grid; width: 100%; gap: .25rem; border: 1px solid #e0e6ef; background: white; color: #1c293d; text-align: left; }
    .record:hover, .record.selected { border-color: #7aa5e7; background: #f1f6ff; color: #183f79; }
    .record span { color: #64748b; font-size: .84rem; font-weight: 400; }
    .detail > h2 { margin-bottom: 1rem; }
    dl { display: grid; grid-template-columns: minmax(7rem, .55fr) 1fr; gap: .55rem .8rem; margin: 0 0 1rem; }
    dt { color: #64748b; } dd { margin: 0; font-weight: 550; overflow-wrap: anywhere; }
    .plain-list li { display: flex; align-items: center; justify-content: space-between; gap: .6rem; padding: .45rem 0; border-bottom: 1px solid #edf0f4; }
    .text-button { padding: .3rem .5rem; background: transparent; color: #174ea6; }
    .text-button:hover { background: #e8f0fe; }
    .form { display: grid; gap: .75rem; margin-top: 1.2rem; padding-top: 1rem; border-top: 1px solid #e6ebf2; }
    .actions { justify-content: flex-start; flex-wrap: wrap; }
    .inline-form { display: grid; grid-template-columns: 1fr 1fr auto; align-items: end; gap: .55rem; margin: .8rem 0 1rem; }
    .inline-form select { min-width: 7rem; } .separated { margin-top: 1.2rem; padding-top: 1rem; border-top: 1px solid #e6ebf2; }
    .check { display: flex; align-items: center; gap: .5rem; } .check input { width: auto; }
    .notice { border-radius: .45rem; padding: .75rem 1rem; } .success { background: #e8f5ec; color: #185b31; } .failure { background: #fff0f0; color: #8b1d1d; }
    .loading { color: #42516a; } .empty { color: #64748b; } .note-content { white-space: pre-wrap; overflow-wrap: anywhere; }
    @media (max-width: 760px) {
      .page-heading { align-items: flex-start; flex-direction: column; }
      .account { justify-content: flex-start; }
      .workspace-grid { grid-template-columns: minmax(0, 1fr); }
      .inline-form { grid-template-columns: 1fr; }
      .panel-heading { flex-wrap: wrap; }
    }
  `],
})
export class UniversityComponent {
  protected readonly session = inject(UniversitySession);
  protected readonly active = signal<Resource>('students');
  protected readonly activeLabel = computed(() => ({
    students: 'students', professors: 'professors', courses: 'courses', lectures: 'lectures', notes: 'notes',
  })[this.active()]);
  protected readonly loading = signal(false);
  protected readonly error = signal('');
  protected readonly choiceError = signal('');
  protected readonly message = signal('');
  protected readonly filterQuery = signal('');
  protected readonly createMode = signal(false);

  protected readonly students = signal<StudentResponse[]>([]);
  protected readonly professors = signal<ProfessorResponse[]>([]);
  protected readonly courses = signal<CourseResponse[]>([]);
  protected readonly lectures = signal<LectureResponse[]>([]);
  protected readonly notes = signal<NotesResponse[]>([]);
  protected readonly selectedStudentKey = signal('');
  protected readonly selectedProfessorKey = signal('');
  protected readonly selectedCourseKey = signal('');
  protected readonly selectedLectureKey = signal<number | null>(null);
  protected readonly selectedStudentDetail = signal<StudentResponse | null>(null);
  protected readonly selectedProfessorDetail = signal<ProfessorResponse | null>(null);
  protected readonly selectedCourseDetail = signal<CourseResponse | null>(null);
  protected readonly selectedLectureDetail = signal<LectureResponse | null>(null);
  protected readonly selectedNoteId = signal<number | null>(null);
  protected readonly selectedNoteDetail = signal<NotesResponse | null>(null);
  protected readonly selectedNoteCourseCode = signal('');
  protected readonly teacherChoice = signal('');
  protected readonly attendanceChoice = signal('');
  protected readonly noteLookupMode = signal<'pending' | 'student' | 'lecture'>('pending');
  protected lookupStudentId = '';
  protected lookupLectureId: number | null = null;

  protected studentId = '';
  protected studentName = '';
  protected studentYear: number | null = null;
  protected professorId = '';
  protected professorName = '';
  protected professorDepartment = '';
  protected professorPassword = '';
  protected courseCode = '';
  protected courseTitle = '';
  protected courseCredits: number | null = null;
  protected lectureCourseCode = '';
  protected lectureProfessorId = '';
  protected lectureDate = '';
  protected lectureTime = '';
  protected lectureTopic = '';
  protected lectureRoom = '';
  protected noteLectureId: number | null = null;
  protected noteContent = '';
  protected notePrivate = false;
  protected reviewGrade: number | null = null;
  protected reviewComment = '';

  protected readonly filteredStudents = computed(() => {
    const query = this.filterQuery().trim().toLowerCase();
    return this.students().filter((item) => `${item.studentId} ${item.name} ${item.enrollmentYear}`.toLowerCase().includes(query));
  });
  protected readonly filteredProfessors = computed(() => {
    const query = this.filterQuery().trim().toLowerCase();
    return this.professors().filter((item) => `${item.employeeId} ${item.name} ${item.department}`.toLowerCase().includes(query));
  });
  protected readonly filteredCourses = computed(() => {
    const query = this.filterQuery().trim().toLowerCase();
    return this.courses().filter((item) => `${item.courseCode} ${item.title} ${item.credits}`.toLowerCase().includes(query));
  });
  protected readonly filteredLectures = computed(() => {
    const query = this.filterQuery().trim().toLowerCase();
    return this.lectures().filter((item) => `${item.courseCode} ${item.employeeId} ${item.topic} ${item.room} ${item.date}`.toLowerCase().includes(query));
  });
  protected readonly filteredNotes = computed(() => {
    const query = this.filterQuery().trim().toLowerCase();
    return this.notes().filter((item) => `${item.studentId} ${item.lectureId} ${item.content} ${item.grade}`.toLowerCase().includes(query));
  });
  protected readonly selectedStudent = computed(() => {
    const detail = this.selectedStudentDetail();
    return detail && this.studentKey(detail) === this.selectedStudentKey()
      ? detail : this.students().find((item) => this.studentKey(item) === this.selectedStudentKey()) ?? null;
  });
  protected readonly selectedProfessor = computed(() => {
    const detail = this.selectedProfessorDetail();
    return detail && this.professorKey(detail) === this.selectedProfessorKey()
      ? detail : this.professors().find((item) => this.professorKey(item) === this.selectedProfessorKey()) ?? null;
  });
  protected readonly selectedCourse = computed(() => {
    const detail = this.selectedCourseDetail();
    return detail && this.courseKey(detail) === this.selectedCourseKey()
      ? detail : this.courses().find((item) => this.courseKey(item) === this.selectedCourseKey()) ?? null;
  });
  protected readonly selectedLecture = computed(() => {
    const detail = this.selectedLectureDetail();
    return detail && this.lectureKey(detail) === this.selectedLectureKey()
      ? detail : this.lectures().find((item) => this.lectureKey(item) === this.selectedLectureKey()) ?? null;
  });
  protected readonly selectedNote = computed(() => {
    const detail = this.selectedNoteDetail();
    return detail?.id === this.selectedNoteId()
      ? detail
      : this.notes().find((item) => item.id === this.selectedNoteId()) ?? null;
  });
  protected readonly notesHeading = computed(() => this.isStudent()
    ? 'My notes' : this.isAdmin() ? 'Note records' : 'Pending review');
  protected readonly notesEmptyMessage = computed(() => {
    if (this.isStudent()) return 'You have not written any notes yet.';
    if (this.isAdmin()) return 'Choose a student or lecture lookup to find notes.';
    return 'No notes are waiting for review. You can also search by student or lecture.';
  });
  protected readonly attendedLectures = computed(() => {
    const studentId = this.session.user()?.username || '';
    return this.lectures().filter((lecture) => lecture.id != null && (lecture.attendeeIds || []).includes(studentId));
  });
  protected readonly schedulableCourses = computed(() => {
    if (!this.isProfessor()) return [];
    const employeeId = this.session.user()?.username;
    return this.courses().filter((course) => course.teacherIds?.includes(employeeId || ''));
  });

  constructor() {
    void this.load('students');
  }

  protected isAdmin(): boolean { return this.session.user()?.role === 'ADMIN'; }
  protected isProfessor(): boolean { return this.session.user()?.role === 'PROFESSOR'; }
  protected isStudent(): boolean { return this.session.user()?.role === 'STUDENT'; }
  protected studentKey(item: StudentResponse): string { return item.studentId || (item.id == null ? '' : String(item.id)); }
  protected professorKey(item: ProfessorResponse): string { return item.employeeId || (item.id == null ? '' : String(item.id)); }
  protected courseKey(item: CourseResponse): string { return item.courseCode || (item.id == null ? '' : String(item.id)); }
  protected lectureKey(item: LectureResponse): number | null { return item.id ?? null; }
  protected canEditStudent(item: StudentResponse): boolean {
    return this.isAdmin() || (this.isStudent() && item.studentId === this.session.user()?.username);
  }
  protected canManageLecture(lecture: LectureResponse): boolean {
    return this.isProfessor() && this.courses().some((course) =>
      course.courseCode === lecture.courseCode && course.teacherIds?.includes(this.session.user()?.username || ''));
  }
  protected lectureTeachers(lecture: LectureResponse): string[] {
    return this.courses().find((course) => course.courseCode === lecture.courseCode)?.teacherIds || [];
  }
  protected canReviewNote(note: NotesResponse): boolean {
    return this.isProfessor() && this.selectedNoteCourseCode() !== '' &&
      this.courses().some((course) => course.courseCode === this.selectedNoteCourseCode()
        && course.teacherIds?.includes(this.session.user()?.username || '')) && note.id === this.selectedNoteId();
  }
  protected isEnrolled(course: CourseResponse): boolean {
    return !!this.session.user()?.username && (course.enrolledStudentIds || []).includes(this.session.user()?.username || '');
  }
  protected hasGrade(note: NotesResponse): boolean { return note.grade !== null && note.grade !== undefined; }

  protected open(resource: Resource): void {
    this.active.set(resource);
    this.filterQuery.set('');
    this.message.set('');
    this.error.set('');
    this.createMode.set(false);
    if (resource === 'notes') this.noteLookupMode.set(this.isAdmin() ? 'student' : 'pending');
    void this.load(resource);
  }

  protected refresh(): void { void this.load(this.active()); }
  protected beginCreate(): void {
    this.resetForm();
    if (this.active() === 'students') this.studentYear = new Date().getFullYear();
    if (this.active() === 'courses') this.courseCredits = 0;
    this.createMode.set(true);
  }
  protected cancelCreate(): void { this.createMode.set(false); this.resetForm(); }

  private async load(resource: Resource): Promise<void> {
    this.loading.set(true);
    this.error.set('');
    try {
      await this.session.run(async () => {
        switch (resource) {
          case 'students': {
            const list = this.isStudent() && this.session.user()?.username
              ? [await this.session.api.getStudent(this.session.user()?.username || '')]
              : await this.session.api.listStudents();
            this.students.set(list);
            const selected = list.find((item) => this.studentKey(item) === this.selectedStudentKey()) ?? null;
            this.selectedStudentDetail.set(selected);
            if (!selected) this.selectedStudentKey.set('');
            break;
          }
          case 'professors': {
            const list = await this.session.api.listProfessors();
            this.professors.set(list);
            const selected = list.find((item) => this.professorKey(item) === this.selectedProfessorKey()) ?? null;
            this.selectedProfessorDetail.set(selected);
            if (!selected) this.selectedProfessorKey.set('');
            break;
          }
          case 'courses':
            this.courses.set(await this.session.api.listCourses());
            await this.loadChoices();
            this.selectedCourseDetail.set(this.courses().find((item) => this.courseKey(item) === this.selectedCourseKey()) ?? null);
            if (!this.selectedCourseDetail()) this.selectedCourseKey.set('');
            break;
          case 'lectures':
            await this.loadLectures();
            break;
          case 'notes':
            if (this.isStudent()) {
              const studentId = this.session.user()?.username || '';
              this.notes.set(studentId ? await this.session.api.listNotesByStudent(studentId) : []);
              try { await this.loadLectures(); }
              catch (reason) { this.choiceError.set(`Lectures: ${this.errorMessage(reason)}`); }
            } else if (this.isProfessor()) {
              this.courses.set(await this.session.api.listCourses());
              this.notes.set(await this.session.api.listPendingReviewNotes());
            } else this.notes.set([]);
            this.selectedNoteDetail.set(null);
            this.selectedNoteCourseCode.set('');
            if (!this.notes().some((item) => item.id === this.selectedNoteId())) this.selectedNoteId.set(null);
            break;
        }
      });
    } catch (reason) {
      this.error.set(this.errorMessage(reason));
    } finally {
      this.loading.set(false);
    }
  }

  private async loadChoices(): Promise<void> {
    this.choiceError.set('');
    if (this.isAdmin() || this.isProfessor()) {
      try { this.professors.set(await this.session.api.listProfessors()); }
      catch (reason) { this.choiceError.set(`Professors: ${this.errorMessage(reason)}`); }
    }
    if (this.isAdmin()) {
      try { this.students.set(await this.session.api.listStudents()); }
      catch (reason) { this.choiceError.set(`${this.choiceError() ? `${this.choiceError()}; ` : ''}Students: ${this.errorMessage(reason)}`); }
    }
  }

  private async loadLectures(): Promise<void> {
    const userId = this.session.user()?.username || '';
    const allCourses = this.courses().length ? this.courses() : await this.session.api.listCourses();
    this.courses.set(allCourses);
    await this.loadChoices();
    let list: LectureResponse[] = [];
    if (this.isProfessor() && userId) {
      const taught = allCourses.filter((course) => course.teacherIds?.includes(userId) && course.courseCode);
      const results = await Promise.all([
        this.session.api.listLecturesByProfessor(userId),
        ...taught.map((course) => this.session.api.listLecturesByCourse(course.courseCode as string)),
      ]);
      list = results.flat();
    } else {
      const courses = this.isStudent()
        ? allCourses.filter((course) => (course.enrolledStudentIds || []).includes(userId))
        : allCourses;
      const results = await Promise.all(courses.filter((course) => course.courseCode)
        .map((course) => this.session.api.listLecturesByCourse(course.courseCode as string)));
      list = results.flat();
    }
    const byId = new Map<number, LectureResponse>();
    for (const item of list) if (item.id != null) byId.set(item.id, item);
    this.lectures.set([...byId.values()].sort((a, b) => `${a.date} ${a.time}`.localeCompare(`${b.date} ${b.time}`)));
    this.selectedLectureDetail.set(this.lectures().find((item) => this.lectureKey(item) === this.selectedLectureKey()) ?? null);
    if (!this.selectedLectureDetail()) this.selectedLectureKey.set(null);
    if (this.isAdmin()) {
      try { this.students.set(await this.session.api.listStudents()); }
      catch (reason) { this.choiceError.set(`Students: ${this.errorMessage(reason)}`); }
    } else if (this.isProfessor()) {
      try { this.students.set(await this.session.api.listStudents()); }
      catch (reason) { this.choiceError.set(`Students: ${this.errorMessage(reason)}`); }
    }
  }

  protected selectStudent(item: StudentResponse): void {
    this.selectedStudentKey.set(this.studentKey(item));
    this.selectedStudentDetail.set(item);
    this.studentName = item.name || '';
    this.studentYear = item.enrollmentYear ?? null;
    this.message.set('');
    if (item.studentId) void this.loadSelectedStudent(item.studentId);
  }
  protected selectProfessor(item: ProfessorResponse): void {
    this.selectedProfessorKey.set(this.professorKey(item));
    this.selectedProfessorDetail.set(item);
    this.professorName = item.name || '';
    this.professorDepartment = item.department || '';
    this.message.set('');
    if (item.employeeId) void this.loadSelectedProfessor(item.employeeId);
  }
  protected selectCourse(item: CourseResponse): void {
    this.selectedCourseKey.set(this.courseKey(item));
    this.selectedCourseDetail.set(item);
    this.courseTitle = item.title || '';
    this.courseCredits = item.credits ?? null;
    this.message.set('');
    if (item.courseCode) void this.loadSelectedCourse(item.courseCode);
  }
  protected selectLecture(item: LectureResponse): void {
    this.selectedLectureKey.set(this.lectureKey(item));
    this.selectedLectureDetail.set(item);
    this.lectureDate = item.date || '';
    this.lectureTime = (item.time || '').slice(0, 5);
    this.lectureRoom = item.room || '';
    this.lectureProfessorId = item.employeeId || '';
    this.message.set('');
    if (item.id != null) void this.loadSelectedLecture(item.id);
  }
  protected selectNote(item: NotesResponse): void {
    this.selectedNoteId.set(item.id ?? null);
    this.selectedNoteCourseCode.set('');
    this.selectedNoteDetail.set(item);
    this.noteContent = item.content || '';
    this.notePrivate = item.privateNote ?? false;
    this.reviewGrade = item.grade ?? null;
    this.reviewComment = item.reviewComment || '';
    this.message.set('');
    if (item.id != null) void this.loadNoteDetail(item.id);
  }

  protected setNoteLookupMode(value: string): void {
    if (value === 'pending' || value === 'student' || value === 'lecture') {
      this.noteLookupMode.set(value);
    }
  }

  protected async findNotes(): Promise<void> {
    const mode = this.noteLookupMode();
    if (mode === 'pending' && !this.isProfessor()) {
      this.error.set('The pending-review endpoint is only available to professors.');
      return;
    }
    if (mode === 'student' && !this.lookupStudentId.trim()) return;
    if (mode === 'lecture' && (!this.lookupLectureId || this.lookupLectureId < 1)) return;
    this.loading.set(true);
    this.error.set('');
    this.message.set('');
    this.selectedNoteId.set(null);
    this.selectedNoteDetail.set(null);
    try {
      const results = await this.session.run(() => {
        if (mode === 'pending') return this.session.api.listPendingReviewNotes();
        if (mode === 'student') return this.session.api.listNotesByStudent(this.lookupStudentId.trim());
        return this.session.api.listNotesByLecture(this.lookupLectureId as number);
      });
      this.notes.set(results);
      this.message.set(`${results.length} note${results.length === 1 ? '' : 's'} found.`);
    } catch (reason) {
      this.notes.set([]);
      this.error.set(this.errorMessage(reason));
    } finally {
      this.loading.set(false);
    }
  }

  private async loadNoteDetail(noteId: number): Promise<void> {
    this.error.set('');
    try {
      const detail = await this.session.run(async () => {
        const note = await this.session.api.getNote(noteId);
        const lecture = this.isProfessor() && note.lectureId != null
          ? await this.session.api.getLecture(note.lectureId) : null;
        return { note, courseCode: lecture?.courseCode || '' };
      });
      if (this.selectedNoteId() === noteId) {
        this.selectedNoteDetail.set(detail.note);
        this.selectedNoteCourseCode.set(detail.courseCode);
        this.noteContent = detail.note.content || '';
        this.notePrivate = detail.note.privateNote ?? false;
        this.reviewGrade = detail.note.grade ?? null;
        this.reviewComment = detail.note.reviewComment || '';
      }
    } catch (reason) {
      this.error.set(this.errorMessage(reason));
    }
  }

  private async loadSelectedStudent(studentId: string): Promise<void> {
    await this.loadDetail(() => this.selectedStudentKey() === studentId,
      () => this.session.api.getStudent(studentId), (item) => {
        this.selectedStudentDetail.set(item);
        this.studentName = item.name || '';
        this.studentYear = item.enrollmentYear ?? null;
      });
  }

  private async loadSelectedProfessor(employeeId: string): Promise<void> {
    await this.loadDetail(() => this.selectedProfessorKey() === employeeId,
      () => this.session.api.getProfessor(employeeId), (item) => {
        this.selectedProfessorDetail.set(item);
        this.professorName = item.name || '';
        this.professorDepartment = item.department || '';
      });
  }

  private async loadSelectedCourse(courseCode: string): Promise<void> {
    await this.loadDetail(() => this.selectedCourseKey() === courseCode,
      () => this.session.api.getCourse(courseCode), (item) => {
        this.selectedCourseDetail.set(item);
        this.courseTitle = item.title || '';
        this.courseCredits = item.credits ?? null;
      });
  }

  private async loadSelectedLecture(lectureId: number): Promise<void> {
    await this.loadDetail(() => this.selectedLectureKey() === lectureId,
      () => this.session.api.getLecture(lectureId), (item) => {
        this.selectedLectureDetail.set(item);
        this.lectureDate = item.date || '';
        this.lectureTime = (item.time || '').slice(0, 5);
        this.lectureRoom = item.room || '';
        this.lectureProfessorId = item.employeeId || '';
      });
  }

  private async loadDetail<T>(
    isCurrent: () => boolean,
    fetch: () => Promise<T>,
    save: (value: T) => void,
  ): Promise<void> {
    this.error.set('');
    try {
      const detail = await this.session.run(fetch);
      if (isCurrent()) save(detail);
    } catch (reason) {
      if (isCurrent()) this.error.set(this.errorMessage(reason));
    }
  }

  protected async createStudent(): Promise<void> {
    const created = await this.execute('Student record created.', () => this.session.api.createStudent({
      studentId: this.studentId.trim(), name: this.studentName.trim(), enrollmentYear: this.studentYear ?? undefined,
    }), 'students');
    if (created) { this.createMode.set(false); this.resetForm(); }
  }
  protected async updateStudent(item: StudentResponse): Promise<void> {
    if (!item.studentId) return;
    await this.execute('Student record updated.', () => this.session.api.updateStudent(item.studentId as string, {
      name: this.studentName.trim(), enrollmentYear: this.studentYear ?? undefined,
    }), 'students');
  }
  protected async deleteStudent(item: StudentResponse): Promise<void> {
    if (!item.studentId || !this.confirmDelete(`Delete student ${item.studentId}? This may also remove related records.`)) return;
    await this.execute('Student record deleted.', () => this.session.api.deleteStudent(item.studentId as string), 'students');
  }
  protected async createProfessor(): Promise<void> {
    const created = await this.execute('Professor record created.', () => this.session.api.createProfessor({
      employeeId: this.professorId.trim(), name: this.professorName.trim(),
      department: this.professorDepartment.trim(), password: this.professorPassword,
    }), 'professors');
    if (created) { this.createMode.set(false); this.resetForm(); }
  }
  protected async updateProfessor(item: ProfessorResponse): Promise<void> {
    if (!item.employeeId) return;
    await this.execute('Professor record updated.', () => this.session.api.updateProfessor(item.employeeId as string, {
      name: this.professorName.trim(), department: this.professorDepartment.trim(),
    }), 'professors');
  }
  protected async deleteProfessor(item: ProfessorResponse): Promise<void> {
    if (!item.employeeId || !this.confirmDelete(`Delete professor ${item.employeeId}? This may affect assigned courses and lectures.`)) return;
    await this.execute('Professor record deleted.', () => this.session.api.deleteProfessor(item.employeeId as string), 'professors');
  }
  protected async createCourse(): Promise<void> {
    const created = await this.execute('Course created.', () => this.session.api.createCourse({
      courseCode: this.courseCode.trim(), title: this.courseTitle.trim(), credits: this.courseCredits ?? undefined,
    }), 'courses');
    if (created) { this.createMode.set(false); this.resetForm(); }
  }
  protected async updateCourse(item: CourseResponse): Promise<void> {
    if (!item.courseCode) return;
    await this.execute('Course updated.', () => this.session.api.updateCourse(item.courseCode as string, {
      title: this.courseTitle.trim(), credits: this.courseCredits ?? undefined,
    }), 'courses');
  }
  protected async deleteCourse(item: CourseResponse): Promise<void> {
    if (!item.courseCode || !this.confirmDelete(`Delete course ${item.courseCode} and its associated resources?`)) return;
    await this.execute('Course deleted.', () => this.session.api.deleteCourse(item.courseCode as string), 'courses');
  }
  protected async assignTeacher(course: CourseResponse): Promise<void> {
    if (!course.courseCode || !this.teacherChoice()) return;
    await this.execute(`Professor ${this.teacherChoice()} assigned to ${course.courseCode}.`,
      () => this.session.api.assignCourseTeacher(course.courseCode as string, this.teacherChoice()), 'courses');
    this.teacherChoice.set('');
  }
  protected async unassignTeacher(course: CourseResponse, employeeId: string): Promise<void> {
    if (!course.courseCode) return;
    await this.execute(`Professor ${employeeId} unassigned from ${course.courseCode}.`,
      () => this.session.api.unassignCourseTeacher(course.courseCode as string, employeeId), 'courses');
  }
  protected async enrollStudent(course: CourseResponse, studentId: string): Promise<void> {
    if (!course.courseCode || !studentId) return;
    await this.execute(`Student ${studentId} enrolled in ${course.courseCode}.`,
      () => this.session.api.enrollStudentInCourse(course.courseCode as string, studentId), 'courses');
  }
  protected async unenrollStudent(course: CourseResponse, studentId: string): Promise<void> {
    if (!course.courseCode || !studentId) return;
    await this.execute(`Student ${studentId} unenrolled from ${course.courseCode}.`,
      () => this.session.api.unenrollStudentFromCourse(course.courseCode as string, studentId), 'courses');
  }
  protected async scheduleLecture(): Promise<void> {
    const employeeId = this.isProfessor() ? this.session.user()?.username || '' : this.lectureProfessorId;
    const scheduled = await this.execute('Lecture scheduled.', () => this.session.api.scheduleLecture({
      courseCode: this.lectureCourseCode, employeeId, date: this.lectureDate,
      time: this.lectureTime, topic: this.lectureTopic.trim(), room: this.lectureRoom.trim(),
    }), 'lectures');
    if (scheduled) { this.createMode.set(false); this.resetForm(); }
  }
  protected async rescheduleLecture(item: LectureResponse): Promise<void> {
    if (item.id == null) return;
    await this.execute('Lecture schedule updated.', () => this.session.api.rescheduleLecture(item.id as number, {
      date: this.lectureDate, time: this.lectureTime, room: this.lectureRoom.trim(),
    }), 'lectures');
  }
  protected async assignLectureProfessor(item: LectureResponse): Promise<void> {
    if (item.id == null || !this.lectureProfessorId) return;
    await this.execute('Professor assigned to lecture.', () => this.session.api.assignLectureProfessor(item.id as number, {
      employeeId: this.lectureProfessorId,
    }), 'lectures');
  }
  protected async cancelLecture(item: LectureResponse): Promise<void> {
    if (item.id == null || !this.confirmDelete(`Cancel lecture “${item.topic || item.id}”?`)) return;
    await this.execute('Lecture cancelled.', () => this.session.api.cancelLecture(item.id as number), 'lectures');
  }
  protected async recordAttendance(item: LectureResponse): Promise<void> {
    if (item.id == null || !this.attendanceChoice()) return;
    await this.execute(`Attendance recorded for ${this.attendanceChoice()}.`,
      () => this.session.api.recordLectureAttendance(item.id as number, this.attendanceChoice()), 'lectures');
    this.attendanceChoice.set('');
  }
  protected async removeAttendance(item: LectureResponse, studentId: string): Promise<void> {
    if (item.id == null) return;
    await this.execute(`Attendance removed for ${studentId}.`,
      () => this.session.api.removeLectureAttendance(item.id as number, studentId), 'lectures');
  }
  protected async writeNote(): Promise<void> {
    const studentId = this.session.user()?.username || '';
    if (!studentId || this.noteLectureId == null) return;
    const saved = await this.execute('Notes saved.', () => this.session.api.writeNote({
      studentId, lectureId: this.noteLectureId as number, content: this.noteContent.trim(), privateNote: this.notePrivate,
    }), 'notes');
    if (saved) { this.createMode.set(false); this.resetForm(); }
  }
  protected async editNote(item: NotesResponse): Promise<void> {
    if (item.id == null) return;
    await this.execute('Notes updated.', () => this.session.api.editNote(item.id as number, {
      content: this.noteContent.trim(), privateNote: this.notePrivate,
    }), 'notes');
  }
  protected async deleteNote(item: NotesResponse): Promise<void> {
    if (item.id == null || !this.confirmDelete('Delete these notes permanently?')) return;
    await this.execute('Notes deleted.', () => this.session.api.deleteNote(item.id as number), 'notes');
  }
  protected async reviewNote(item: NotesResponse): Promise<void> {
    const employeeId = this.session.user()?.username || '';
    if (!employeeId || item.id == null || this.reviewGrade == null) return;
    await this.execute('Note review submitted.', () => this.session.api.reviewNote(item.id as number, {
      employeeId, grade: this.reviewGrade as number, comment: this.reviewComment.trim() || null,
    }), 'notes');
  }

  private async execute(success: string, action: ApiAction, resource: Resource): Promise<boolean> {
    if (this.loading()) return false;
    this.loading.set(true);
    this.error.set('');
    this.message.set('');
    try {
      await this.session.run(action);
      this.message.set(success);
      await this.load(resource);
      return true;
    } catch (reason) {
      this.error.set(this.errorMessage(reason));
      return false;
    } finally {
      this.loading.set(false);
    }
  }

  private confirmDelete(message: string): boolean {
    return globalThis.confirm(message);
  }

  private errorMessage(error: unknown): string {
    return error instanceof Error ? error.message : String(error);
  }

  private resetForm(): void {
    this.studentId = ''; this.studentName = ''; this.studentYear = null;
    this.professorId = ''; this.professorName = ''; this.professorDepartment = ''; this.professorPassword = '';
    this.courseCode = ''; this.courseTitle = ''; this.courseCredits = null;
    this.lectureCourseCode = ''; this.lectureProfessorId = ''; this.lectureDate = ''; this.lectureTime = '';
    this.lectureTopic = ''; this.lectureRoom = ''; this.noteLectureId = null; this.noteContent = '';
    this.notePrivate = false; this.reviewGrade = null; this.reviewComment = '';
  }
}
