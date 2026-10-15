import { provideNativeRouter } from '@ng-native/router';
import { render, screen, userEvent } from '@ng-native/testing';
import { expect, test } from 'vitest';
import { App } from './app.ts';
import { appRoutes } from './app.routes.ts';

test('opens Home with shared summary stats', async () => {
  await render(App, { providers: [provideNativeRouter(appRoutes)] });

  expect(screen.getByText('Your workspace at a glance.')).toBeTruthy();
  expect(screen.getByText('Libraries')).toBeTruthy();
  expect(screen.getByText('Remotes')).toBeTruthy();
});

test('opens the left navigation and selects the Dashboard screen', async () => {
  await render(App, { providers: [provideNativeRouter(appRoutes)] });

  await userEvent.setup().press(screen.getByRole('button', { name: 'Open navigation menu' }));
  expect(screen.getByText('Main navigation')).toBeTruthy();

  await userEvent.setup().press(screen.getByRole('button', { name: 'Open Dashboard' }));
  expect(screen.getByText('A native view of your dashboard.')).toBeTruthy();
  expect(screen.getByText('Active users')).toBeTruthy();
});
