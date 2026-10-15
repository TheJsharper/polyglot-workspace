import { TestBed } from '@angular/core/testing';
import { ApiError, UniversityApi } from '@project-00/services-api';
import { UNIVERSITY_API_BASE_URL, UniversitySession } from './university-session';

describe('UniversitySession', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [{ provide: UNIVERSITY_API_BASE_URL, useValue: '' }],
    });
  });

  afterEach(() => {
    vi.restoreAllMocks();
    vi.useRealTimers();
  });

  it('holds bearer credentials only in memory and expires them', async () => {
    vi.useFakeTimers();
    vi.spyOn(UniversityApi.prototype, 'login').mockResolvedValue({
      accessToken: 'test-token', tokenType: 'Bearer', expiresIn: 2,
    });
    vi.spyOn(UniversityApi.prototype, 'me').mockResolvedValue({ username: 'S-1', role: 'STUDENT' });
    const session = TestBed.inject(UniversitySession);
    await session.login({ username: 'S-1', password: 'password-123' });
    expect(session.user()?.username).toBe('S-1');
    expect(session.authenticated()).toBe(true);
    vi.advanceTimersByTime(2000);
    expect(session.authenticated()).toBe(false);
    expect(session.user()).toBeNull();
  });

  it('rejects a malformed login response without retaining an account', async () => {
    vi.spyOn(UniversityApi.prototype, 'login').mockResolvedValue({ expiresIn: 10 });
    const session = TestBed.inject(UniversitySession);
    await expect(session.login({ username: 'S-1', password: 'password-123' }))
      .rejects.toThrow('invalid access token');
    expect(session.authenticated()).toBe(false);
  });

  it('logs out on unauthorized requests but not on forbidden requests', async () => {
    vi.spyOn(UniversityApi.prototype, 'login').mockResolvedValue({
      accessToken: 'test-token', tokenType: 'Bearer', expiresIn: 60,
    });
    vi.spyOn(UniversityApi.prototype, 'me').mockResolvedValue({ username: 'S-1', role: 'STUDENT' });
    const session = TestBed.inject(UniversitySession);
    await session.login({ username: 'S-1', password: 'password-123' });
    await expect(session.run(() => Promise.reject(new ApiError(403, { title: 'Forbidden' }, 'GET', '/api'))))
      .rejects.toBeInstanceOf(ApiError);
    expect(session.authenticated()).toBe(true);
    await expect(session.run(() => Promise.reject(new ApiError(401, { title: 'Unauthorized' }, 'GET', '/api'))))
      .rejects.toBeInstanceOf(ApiError);
    expect(session.authenticated()).toBe(false);
  });
});
