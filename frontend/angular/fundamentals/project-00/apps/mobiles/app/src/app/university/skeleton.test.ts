import { render, screen } from '@ng-native/testing';
import { expect, test } from 'vitest';
import { AppSkeleton } from './skeleton.ts';

test('the loading skeleton is announced as a progress indicator', async () => {
  await render(AppSkeleton);

  expect(screen.getByRole('progressbar', { name: 'Loading' })).toBeTruthy();
});
