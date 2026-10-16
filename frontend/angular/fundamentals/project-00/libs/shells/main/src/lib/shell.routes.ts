import { inject, isDevMode } from '@angular/core';
import { CanActivateFn, Route, Router } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { loadRemote, registerRemotes } from '@module-federation/enhanced/runtime';

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
    loadChildren: async () => {
      if (!isDevMode()) {
        return import('dashboard_remote/Routes').then((m) => m.remoteRoutes);
      }
      registerRemotes([{
        name: 'dashboard_remote',
        entry: `${location.protocol}//${location.hostname}:4301/mf-manifest.json`,
      }]);
      const remote = await loadRemote<{ remoteRoutes: Route[] }>('dashboard_remote/Routes');
      if (!remote) {
        throw new Error('Dashboard remote routes could not be loaded.');
      }
      return remote.remoteRoutes;
    },
  },
  { path: '**', redirectTo: '' },
];
