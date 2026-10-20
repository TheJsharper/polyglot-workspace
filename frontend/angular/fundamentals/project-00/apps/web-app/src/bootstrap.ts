import { bootstrapApplication } from '@angular/platform-browser';
import { DEMO_ENABLED, GOOGLE_SSO_ENABLED } from '@project-00/services-session';
import { appConfig } from './app/app.config';
import { App } from './app/app';

async function demoEnabled(): Promise<boolean> {
  try {
    const response = await fetch('/api/demo/status', { cache: 'no-store' });
    if (!response.ok) return false;
    const status: unknown = await response.json();
    return typeof status === 'object' && status !== null
      && 'enabled' in status && status.enabled === true;
  } catch (error) {
    console.error('Could not determine recruiter demo availability', error);
    return false;
  }
}

async function googleSsoEnabled(): Promise<boolean> {
  try {
    const response = await fetch('/api/auth/google/status', { cache: 'no-store' });
    if (!response.ok) return false;
    const status: unknown = await response.json();
    return typeof status === 'object' && status !== null
      && 'enabled' in status && status.enabled === true;
  } catch (error) {
    console.error('Could not determine Google sign-in availability', error);
    return false;
  }
}

Promise.all([demoEnabled(), googleSsoEnabled()])
  .then(([demo, google]) =>
    bootstrapApplication(App, {
      ...appConfig,
      providers: [
        ...appConfig.providers,
        { provide: DEMO_ENABLED, useValue: demo },
        { provide: GOOGLE_SSO_ENABLED, useValue: google },
      ],
    }),
  )
  .catch((err) => console.error(err));
