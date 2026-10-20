import { InjectionToken } from '@angular/core';

/** True only when the local backend reports that dev-only recruiter demo access is enabled. */
export const DEMO_ENABLED = new InjectionToken<boolean>('DEMO_ENABLED', {
  providedIn: 'root',
  factory: () => false,
});

export const GOOGLE_SSO_ENABLED = new InjectionToken<boolean>('GOOGLE_SSO_ENABLED', {
  providedIn: 'root',
  factory: () => false,
});
