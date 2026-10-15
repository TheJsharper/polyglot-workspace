import type { Routes } from '@angular/router';

export const universityRoutes: Routes = [
  {
    path: '',
    loadComponent: () => import('./university.component').then((m) => m.UniversityComponent),
  },
];
