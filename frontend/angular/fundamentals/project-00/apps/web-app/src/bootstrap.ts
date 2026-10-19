import { bootstrapApplication } from '@angular/platform-browser';
import { DEMO_ENABLED } from '@project-00/services-session';
import { appConfig } from './app/app.config';
import { App } from './app/app';

async function demoEnabled(): Promise<boolean> {
  try {
    const response = await fetch('api/demo/status', { cache: 'no-store' });
    return response.ok && (await response.json()).enabled === true;
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
