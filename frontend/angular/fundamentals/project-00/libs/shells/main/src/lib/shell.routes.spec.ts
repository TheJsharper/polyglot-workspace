import { shellRoutes } from './shell.routes';

describe('shellRoutes', () => {
  it('lazy-loads the University feature routes', () => {
    const featureRoutes = shellRoutes.filter((r) => r.path === 'login' || r.path === 'register' || r.path === 'university');
    expect(featureRoutes.every((r) => typeof r.loadChildren === 'function' || typeof r.loadComponent === 'function')).toBe(true);
  });

  it('redirects the root and unknown routes to University', () => {
    expect(shellRoutes.find((r) => r.path === '')).toMatchObject({
      pathMatch: 'full',
      redirectTo: 'university',
    });
    expect(shellRoutes.find((r) => r.path === '**')?.redirectTo).toBe('university');
    expect(shellRoutes.some((r) => r.path === 'dashboard')).toBe(false);
  });
});
