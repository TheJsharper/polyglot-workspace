import type { Routes } from '@angular/router';
import { provideUniversityState } from '@project-00/services-university-state';
import { ProfessorsPage } from './professors.page';

/** Exposed as `professors/./Routes` through Native Federation. */
export const routes: Routes = [
  { path: '', providers: [provideUniversityState()], component: ProfessorsPage },
];
