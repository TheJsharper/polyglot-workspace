import { Route } from '@angular/router';

export const shellRoutes: Route[] = [
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
