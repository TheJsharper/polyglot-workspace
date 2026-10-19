import { inject } from '@angular/core';
import { Route, Router } from '@angular/router';
import { UniversitySession } from '@project-00/services-session';
import { LoginComponent } from '@project-00/views-university';
import { routes } from './remote.routes';

/** Routes for serving this remote on its own; the shell uses only `remote.routes.ts`. */
export const standaloneRoutes: Route[] = [
  { path: 'login', component: LoginComponent },
  {
    path: 'university/lectures',
    canActivate: [(_route, state) => inject(UniversitySession).authenticated()
      || inject(Router).createUrlTree(['/login'], { queryParams: { returnUrl: state.url } })],
    children: routes,
  },
  { path: '**', redirectTo: 'university/lectures' },
];
