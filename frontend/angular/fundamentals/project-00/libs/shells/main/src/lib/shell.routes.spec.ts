import { shellRoutes } from './shell.routes';
import { loadRemote, registerRemotes } from '@module-federation/enhanced/runtime';

const remoteRoutes = [{ path: '' }];
vi.mock('@module-federation/enhanced/runtime', () => ({ loadRemote: vi.fn(), registerRemotes: vi.fn() }));

describe('shellRoutes', () => {
  it('lazy-loads every feature route', () => {
    const featureRoutes = shellRoutes.filter((r) => r.path !== '**');
    expect(featureRoutes.every((r) => typeof r.loadChildren === 'function' || typeof r.loadComponent === 'function')).toBe(true);
  });

  it('loads the dashboard from the federated remote', async () => {
    vi.mocked(loadRemote).mockResolvedValue({ remoteRoutes });
    const dashboard = shellRoutes.find((r) => r.path === 'dashboard');
    await expect(dashboard?.loadChildren?.()).resolves.toBe(remoteRoutes);
    expect(registerRemotes).toHaveBeenCalledWith([{
      name: 'dashboard_remote',
      entry: `${location.protocol}//${location.hostname}:4301/mf-manifest.json`,
    }]);
  });

  it('reports an unavailable dashboard remote', async () => {
    vi.mocked(loadRemote).mockResolvedValue(null);
    const dashboard = shellRoutes.find((r) => r.path === 'dashboard');
    await expect(dashboard?.loadChildren?.()).rejects.toThrow('Dashboard remote routes could not be loaded.');
  });
});
