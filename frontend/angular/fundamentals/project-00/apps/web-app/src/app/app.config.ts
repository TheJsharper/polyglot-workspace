import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideShell } from '@project-00/shells-main';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideShell({ blockingInitialNavigation: true }),
  ],
};
