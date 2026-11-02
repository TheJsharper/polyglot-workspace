import { provideNativeRouter } from '@ng-native/router';
import { UniversitySession } from '@project-00/services-session';
import { render, screen } from '@ng-native/testing';
import { expect, test, vi } from 'vitest';
import { App } from './app.ts';
import { appRoutes } from './app.routes.ts';

vi.mock('expo-crypto', () => ({}));
vi.mock('expo-linking', () => ({}));
vi.mock('expo-web-browser', () => ({}));

test('opens the University sign-in page instead of sample Home and Dashboard screens', async () => {
  await render(App, {
    providers: [
      provideNativeRouter(appRoutes),
      {
        provide: UniversitySession,
        useValue: {
          authenticated: () => false,
          user: () => null,
          api: { googleStatus: async () => ({ enabled: false }) },
        },
      },
    ],
  });

  expect(await screen.findByText('Use the username provided by your University administrator, student id, or employee id.')).toBeTruthy();
  expect(screen.queryByText('Your workspace at a glance.')).toBeNull();
  expect(screen.queryByText('A native view of your dashboard.')).toBeNull();
}, 15000);
