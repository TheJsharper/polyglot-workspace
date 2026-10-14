import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { MENU_CONFIG, provideAppState } from '@project-00/services-state';
import { SHELL_MENU } from './shell.menu';
import { shellRoutes } from './shell.routes';

export function provideShell(): EnvironmentProviders {
  return makeEnvironmentProviders([
    provideRouter(shellRoutes, withComponentInputBinding()),
    { provide: MENU_CONFIG, useValue: SHELL_MENU },
    provideAppState(),
  ]);
}
