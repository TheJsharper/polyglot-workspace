import { dashboardRoutes } from '@project-00/dashboards-main';
import { remoteRoutes } from './entry.routes';

describe('remoteRoutes', () => {
  it('exposes the dashboard routes', () => {
    expect(remoteRoutes).toBe(dashboardRoutes);
  });
});
