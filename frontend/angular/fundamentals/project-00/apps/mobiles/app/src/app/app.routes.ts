import { withComponentInputBinding } from '@angular/router';
import type { Routes } from '@angular/router';
import { provideNativeRouter, withHeaderDefaults } from '@ng-native/router';
import { headerDefaults } from './theme.ts';
import { authGuard, universityDefaultGuard, adminRouteGuard } from './university/auth.ts';
import { DemoLoginsScreenComponent } from './university/screens/demo-logins.component.ts';
import { LoginScreenComponent } from './university/screens/login-screen.component.ts';
import { RegisterScreenComponent } from './university/screens/register-screen.component.ts';
import { CourseDetailScreenComponent, CourseFormScreenComponent, CoursesScreenComponent } from './university/screens/courses.component.ts';
import { LectureDetailScreenComponent, LectureFormScreenComponent, LecturesScreenComponent } from './university/screens/lectures.component.ts';
import { NoteDetailScreenComponent, NoteFormScreenComponent, NotesScreenComponent } from './university/screens/notes.component.ts';
import { ProfessorDetailScreenComponent, ProfessorFormScreenComponent, ProfessorsScreenComponent } from './university/screens/professors.component.ts';
import { StudentDetailScreenComponent, StudentFormScreenComponent, StudentsScreenComponent } from './university/screens/students.component.ts';
import { UniversityHomeScreenComponent } from './university/screens/university-home-screen.component.ts';
import { UniversityShellComponent } from './university/university-shell.component.ts';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'university' },
  { path: 'login', component: LoginScreenComponent },
  { path: 'register', component: RegisterScreenComponent },
  {
    path: 'university',
    component: UniversityShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', canActivate: [universityDefaultGuard], component: UniversityHomeScreenComponent },
      { path: 'demo-logins', canActivate: [adminRouteGuard], component: DemoLoginsScreenComponent },
      { path: 'students', component: StudentsScreenComponent },
      { path: 'students/new', component: StudentFormScreenComponent },
      { path: 'students/:id', component: StudentDetailScreenComponent },
      { path: 'students/:id/edit', component: StudentFormScreenComponent },
      { path: 'professors', component: ProfessorsScreenComponent },
      { path: 'professors/new', component: ProfessorFormScreenComponent },
      { path: 'professors/:id', component: ProfessorDetailScreenComponent },
      { path: 'professors/:id/edit', component: ProfessorFormScreenComponent },
      { path: 'courses', component: CoursesScreenComponent },
      { path: 'courses/new', component: CourseFormScreenComponent },
      { path: 'courses/:id', component: CourseDetailScreenComponent },
      { path: 'courses/:id/edit', component: CourseFormScreenComponent },
      { path: 'lectures', component: LecturesScreenComponent },
      { path: 'lectures/new', component: LectureFormScreenComponent },
      { path: 'lectures/:id', component: LectureDetailScreenComponent },
      { path: 'lectures/:id/edit', component: LectureFormScreenComponent },
      { path: 'notes', component: NotesScreenComponent },
      { path: 'notes/new', component: NoteFormScreenComponent },
      { path: 'notes/:id', component: NoteDetailScreenComponent },
      { path: 'notes/:id/edit', component: NoteFormScreenComponent },
    ],
  },
  { path: '**', redirectTo: 'university' },
];

/** Route params and query params arrive as screen inputs; native headers follow the system colour scheme. */
export const provideAppRouter = () =>
  provideNativeRouter(appRoutes, withComponentInputBinding(), withHeaderDefaults(headerDefaults));