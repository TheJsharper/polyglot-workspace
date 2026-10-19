import type { Routes } from '@angular/router';
import { provideUniversityState } from '@project-00/services-university-state';
import { LecturesPage } from './lectures.page';

/** Exposed as `lectures/./Routes` through Native Federation. */
export const routes: Routes = [
  { path: '', providers: [provideUniversityState()], component: LecturesPage },
];
