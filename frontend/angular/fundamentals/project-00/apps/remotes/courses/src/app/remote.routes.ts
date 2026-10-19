import type { Routes } from '@angular/router';
import { provideUniversityState } from '@project-00/services-university-state';
import { CoursesPage } from './courses.page';

/** Exposed as `courses/./Routes` through Native Federation. */
export const routes: Routes = [
  { path: '', providers: [provideUniversityState()], component: CoursesPage },
];
