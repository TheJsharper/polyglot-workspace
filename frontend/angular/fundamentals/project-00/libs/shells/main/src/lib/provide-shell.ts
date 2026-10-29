import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import {
  provideRouter,
  withComponentInputBinding,
  withEnabledBlockingInitialNavigation,
} from '@angular/router';
import { MENU_CONFIG, provideAppState } from '@project-00/services-state';
import { UNIVERSITY_API_BASE_URL } from '@project-00/services-session';
import { provideUniversityAuth } from '@project-00/services-university-state';
import { SHELL_MENU } from './shell.menu';
import { shellRoutes } from './shell.routes';

export interface ShellOptions {
  blockingInitialNavigation?: boolean;
}

export function provideShell({ blockingInitialNavigation = false }: ShellOptions = {}): EnvironmentProviders {
  const routerProviders = blockingInitialNavigation
    ? provideRouter(shellRoutes, withComponentInputBinding(), withEnabledBlockingInitialNavigation())
    : provideRouter(shellRoutes, withComponentInputBinding());

  return makeEnvironmentProviders([
    routerProviders,
    { provide: MENU_CONFIG, useValue: SHELL_MENU },
    { provide: UNIVERSITY_API_BASE_URL, useValue: '' },
    provideAppState(),
    provideUniversityAuth(),
  ]);
}
