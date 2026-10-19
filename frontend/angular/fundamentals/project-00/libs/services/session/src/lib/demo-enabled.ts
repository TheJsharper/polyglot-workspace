import { InjectionToken } from '@angular/core';

/** True only in the public recruiter-demo deployment; gates the demo-token page and its entry link. */
export const DEMO_ENABLED = new InjectionToken<boolean>('DEMO_ENABLED', {
  providedIn: 'root',
  factory: () => false,
});
