import { inject } from '@angular/core';
import { CanActivateFn, Route, Router } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';

const universityGuard: CanActivateFn = (_route, state) =>
  inject(UniversitySession).authenticated()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });

export const shellRoutes: Route[] = [
  {
    path: 'login',
    loadComponent: () =>
      import('@project-00/views-university').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('@project-00/views-university').then((m) => m.RegisterComponent),
  },
  {
    path: 'university',
    canActivate: [universityGuard],
    loadChildren: () =>
      import('@project-00/views-university').then((m) => m.universityRoutes),
  },
  {
    path: '',
    pathMatch: 'full',
    loadChildren: () => import('@project-00/views-home').then((m) => m.homeRoutes),
  },
  {
    path: 'dashboard',
    // Federated remote: resolved by Module Federation at runtime (see apps/web-app/module-federation.config.ts).
    loadChildren: () => import('dashboard_remote/Routes').then((m) => m.remoteRoutes),
  },
  { path: '**', redirectTo: '' },
];
