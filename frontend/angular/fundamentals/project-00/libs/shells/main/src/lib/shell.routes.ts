import { inject } from '@angular/core';
import { CanActivateFn, Route, Router, Routes } from '@angular/router';
import { loadRemoteModule } from '@angular-architects/native-federation';
import { DEMO_ENABLED, UniversitySession } from '@project-00/services-session';
import { RESOURCES, type Resource } from '@project-00/services-university-state';
import { RemoteUnavailableComponent } from './remote-unavailable.component';
import { allowedResources } from './shell.menu';

const universityGuard: CanActivateFn = (_route, state) => {
  const session = inject(UniversitySession);
  const router = inject(Router);
  const redirect = () => router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
  return session.authenticated() ? true : session.restore().then((restored) => restored || redirect());
};

const demoGuard: CanActivateFn = () =>
  inject(DEMO_ENABLED) ? true : inject(Router).createUrlTree(['/login']);

const roleGuard: CanActivateFn = (route) => {
  const user = inject(UniversitySession).user();
  const allowed = allowedResources(user?.role, user?.demo === true);
  const path = route.routeConfig?.path ?? '';
  return allowed.includes(path) ? true : inject(Router).createUrlTree(['/university', allowed[0] ?? '']);
};

/**
 * Lazily loads a University remote (Native Federation). Remote names match the keys in
 * `federation.manifest.json`; each remote exposes `./Routes` exporting `routes`.
 */
export function remoteRoute(remote: Resource): Route {
  return {
    path: remote,
    canActivate: [roleGuard],
    loadChildren: () =>
      loadRemoteModule<{ routes: Routes }>(remote, './Routes')
        .then((m) => m.routes)
        .catch((error: unknown): Routes => {
          console.error(`Failed to load the "${remote}" remote`, error);
          return [{ path: '**', component: RemoteUnavailableComponent, data: { remote } }];
        }),
  };
}

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
    path: 'sso/callback',
    loadComponent: () =>
      import('@project-00/views-university').then((m) => m.GoogleCallbackComponent),
  },
  {
    path: 'demo',
    canActivate: [demoGuard],
    loadComponent: () =>
      import('@project-00/views-university').then((m) => m.DemoComponent),
  },
  {
    path: 'university',
    canActivate: [universityGuard],
    children: [
      { path: 'demo-keys', canActivate: [roleGuard], loadComponent: () =>
          import('@project-00/views-university').then((m) => m.DemoKeysComponent) },
      { path: 'demo-logins', canActivate: [roleGuard], loadComponent: () =>
          import('@project-00/views-university').then((m) => m.DemoLoginsComponent) },
      ...RESOURCES.map(remoteRoute),
      { path: '', pathMatch: 'full', redirectTo: 'students' },
      { path: '**', redirectTo: 'students' },
    ],
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'university',
  },
  { path: '**', redirectTo: 'university' },
];
