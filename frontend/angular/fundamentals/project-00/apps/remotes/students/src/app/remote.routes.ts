import type { Routes } from '@angular/router';
import { provideUniversityState } from '@project-00/services-university-state';
import { StudentsPage } from './students.page';

/** Exposed as `students/./Routes` through Native Federation. */
export const routes: Routes = [
  { path: '', providers: [provideUniversityState()], component: StudentsPage },
];
