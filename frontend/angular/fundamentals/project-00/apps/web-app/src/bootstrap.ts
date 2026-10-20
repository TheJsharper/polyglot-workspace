import { bootstrapApplication } from '@angular/platform-browser';
import { DEMO_ENABLED } from '@project-00/services-session';
import { appConfig } from './app/app.config';
import { App } from './app/app';

async function demoEnabled(): Promise<boolean> {
  try {
    const response = await fetch('/api/demo/status', { cache: 'no-store' });
    if (!response.ok) return false;
    const status: unknown = await response.json();
    return typeof status === 'object' && status !== null
      && 'enabled' in status && status.enabled === true;
  } catch {
    return false;
  }
}

demoEnabled()
  .then((enabled) =>
    bootstrapApplication(App, {
      ...appConfig,
      providers: [...appConfig.providers, { provide: DEMO_ENABLED, useValue: enabled }],
    }),
  )
  .catch((err) => console.error(err));
