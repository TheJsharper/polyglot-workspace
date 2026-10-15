import { withComponentInputBinding } from '@angular/router';
import type { Routes } from '@angular/router';
import { provideNativeRouter } from '@ng-native/router';
import { NativeShellComponent } from './native-shell.component.ts';
import { DashboardScreenComponent } from './screens/dashboard-screen.component.ts';
import { HomeScreenComponent } from './screens/home-screen.component.ts';
import { authGuard } from './university/auth.ts';
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
  { path: 'login', component: LoginScreenComponent },
  { path: 'register', component: RegisterScreenComponent },
  {
    path: 'university',
    component: UniversityShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: UniversityHomeScreenComponent },
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
  {
    path: '',
    component: NativeShellComponent,
    children: [
      { path: '', component: HomeScreenComponent },
      { path: 'dashboard', component: DashboardScreenComponent },
      { path: '**', redirectTo: '' },
    ],
  },
];

/** Route params and query params arrive as screen inputs. */
export const provideAppRouter = () => provideNativeRouter(appRoutes, withComponentInputBinding());