import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';
import { UNIVERSITY_API_BASE_URL } from '@project-00/services-session';
import { provideUniversityAuth } from '@project-00/services-university-state';
import { standaloneRoutes } from './standalone.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(standaloneRoutes),
    { provide: UNIVERSITY_API_BASE_URL, useValue: '' },
    provideStore(),
    provideEffects(),
    provideUniversityAuth(),
  ],
};
