import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { MENU_CONFIG, provideAppState } from '@project-00/services-state';
import { UNIVERSITY_API_BASE_URL } from '@project-00/services-session';
import { SHELL_MENU } from './shell.menu';
import { shellRoutes } from './shell.routes';

export function provideShell(): EnvironmentProviders {
  return makeEnvironmentProviders([
    provideRouter(shellRoutes, withComponentInputBinding()),
    { provide: MENU_CONFIG, useValue: SHELL_MENU },
    { provide: UNIVERSITY_API_BASE_URL, useValue: '' },
    provideAppState(),
  ]);
}
