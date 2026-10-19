import type { Routes } from '@angular/router';
import { provideUniversityState } from '@project-00/services-university-state';
import { NotesPage } from './notes.page';

/** Exposed as `notes/./Routes` through Native Federation. */
export const routes: Routes = [
  { path: '', providers: [provideUniversityState()], component: NotesPage },
];
