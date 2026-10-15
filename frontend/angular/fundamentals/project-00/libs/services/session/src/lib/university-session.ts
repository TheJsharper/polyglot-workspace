import { computed, inject, Injectable, InjectionToken, signal } from '@angular/core';
import { ApiError, UniversityApi } from '@project-00/services-api';
import type { LoginRequest, MeResponse, RegisterRequest, RegisterResponse } from '@project-00/services-api';

export const UNIVERSITY_API_BASE_URL = new InjectionToken<string>('University API base URL');

@Injectable({ providedIn: 'root' })
export class UniversitySession {
  private readonly baseUrl = inject(UNIVERSITY_API_BASE_URL);
  private readonly accessToken = signal<string | null>(null);
  private readonly currentUser = signal<MeResponse | null>(null);
  private expiresAt = 0;
  private expirationTimer: ReturnType<typeof setTimeout> | undefined;

  readonly user = this.currentUser.asReadonly();
  readonly authenticated = computed(() => !!this.accessToken() && !!this.currentUser());
  readonly api = new UniversityApi(this.baseUrl, () => this.token());

  async login(credentials: LoginRequest): Promise<MeResponse> {
    this.logout();
    const issued = await this.api.login(credentials);
    if (
      typeof issued.accessToken !== 'string' ||
      !issued.accessToken ||
      issued.tokenType?.toLowerCase() !== 'bearer' ||
      typeof issued.expiresIn !== 'number' ||
      !Number.isFinite(issued.expiresIn) ||
      issued.expiresIn <= 0
    ) {
      throw new Error('The server returned an invalid access token.');
    }
    this.accessToken.set(issued.accessToken);
    this.expiresAt = Date.now() + issued.expiresIn * 1000;
    this.expirationTimer = setTimeout(() => this.logout(), issued.expiresIn * 1000);
    try {
      const user = await this.api.me();
      if (!user.username || !user.role) {
        throw new Error('The server returned an invalid user profile.');
      }
      this.currentUser.set(user);
      return user;
    } catch (error) {
      this.logout();
      throw error;
    }
  }

  register(request: RegisterRequest): Promise<RegisterResponse> {
    return this.run(() => this.api.register(request));
  }

  async run<T>(action: () => Promise<T>): Promise<T> {
    try {
      return await action();
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        this.logout();
      }
      throw error;
    }
  }

  logout(): void {
    if (this.expirationTimer) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = undefined;
    }
    this.expiresAt = 0;
    this.accessToken.set(null);
    this.currentUser.set(null);
  }

  private token(): string | null {
    if (this.expiresAt && Date.now() >= this.expiresAt) {
      this.logout();
    }
    return this.accessToken();
  }
}
