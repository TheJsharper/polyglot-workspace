import { shellRoutes } from './shell.routes';

const remoteRoutes = [{ path: '' }];
vi.mock('dashboard_remote/Routes', () => ({ remoteRoutes }));

describe('shellRoutes', () => {
  it('lazy-loads every feature route', () => {
    const featureRoutes = shellRoutes.filter((r) => r.path !== '**');
    expect(featureRoutes.every((r) => typeof r.loadChildren === 'function' || typeof r.loadComponent === 'function')).toBe(true);
  });

  it('loads the dashboard from the federated remote', async () => {
    const dashboard = shellRoutes.find((r) => r.path === 'dashboard');
    await expect(dashboard?.loadChildren?.()).resolves.toBe(remoteRoutes);
  });
});
