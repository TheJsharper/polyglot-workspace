import { Routes } from '@angular/router';
import { RemoteUnavailableComponent } from './remote-unavailable.component';
import { remoteRoute, shellRoutes } from './shell.routes';

const { loadRemoteModule } = vi.hoisted(() => ({ loadRemoteModule: vi.fn() }));
vi.mock('@angular-architects/native-federation', () => ({ loadRemoteModule }));

describe('shellRoutes', () => {
  it('lazy-loads the login and register screens', () => {
    const auth = shellRoutes.filter((r) => r.path === 'login' || r.path === 'register');
    expect(auth).toHaveLength(2);
    expect(auth.every((r) => typeof r.loadComponent === 'function')).toBe(true);
  });

  it('maps every University sidebar item to a lazily loaded remote', () => {
    const university = shellRoutes.find((r) => r.path === 'university');
    expect(university?.canActivate).toHaveLength(1);
    const remotes = (university?.children ?? []).filter((r) => typeof r.loadChildren === 'function');
    expect(remotes.map((r) => r.path)).toEqual(['students', 'professors', 'courses', 'lectures', 'notes']);
    expect(university?.children?.find((r) => r.path === '')?.redirectTo).toBe('students');
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

describe('remoteRoute', () => {
  afterEach(() => vi.restoreAllMocks());

  it('loads the exposed ./Routes module of the remote', async () => {
    const routes: Routes = [{ path: '' }];
    loadRemoteModule.mockResolvedValueOnce({ routes });
    const loaded = await (remoteRoute('courses').loadChildren as () => Promise<Routes>)();
    expect(loadRemoteModule).toHaveBeenCalledWith('courses', './Routes');
    expect(loaded).toBe(routes);
  });

  it('falls back to an unavailable notice when the remote cannot be loaded', async () => {
    vi.spyOn(console, 'error').mockImplementation(() => undefined);
    loadRemoteModule.mockRejectedValueOnce(new Error('offline'));
    const loaded = await (remoteRoute('notes').loadChildren as () => Promise<Routes>)();
    expect(loaded).toEqual([{ path: '**', component: RemoteUnavailableComponent, data: { remote: 'notes' } }]);
  });
});
