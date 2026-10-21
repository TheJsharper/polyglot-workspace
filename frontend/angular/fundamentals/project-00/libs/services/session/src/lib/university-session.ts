import { computed, inject, Injectable, InjectionToken, signal } from '@angular/core';
import { ApiError, UniversityApi } from '@project-00/services-api';
import type { DemoRedeemRequest, GoogleOnboardingRequest, GoogleCompleteResponse, LoginRequest, MeResponse, RegisterRequest, RegisterResponse, Role, TokenResponse } from '@project-00/services-api';

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
    this.clearLocal();
    return this.start(await this.api.login(credentials), true);
  }

  async redeemDemo(request: DemoRedeemRequest): Promise<MeResponse> {
    this.clearLocal();
    return this.start(await this.api.redeemDemo(request));
  }

  async completeGoogle(ticket: string, onboarding?: GoogleOnboardingRequest): Promise<GoogleCompleteResponse> {
    const result = await this.api.googleComplete(ticket, onboarding);
    if (result.session) {
      this.clearLocal();
      await this.start(result.session, true);
    }
    return result;
  }

  /** Acts as another role (dev manager only). Keeps the signed-in state while the new token loads. */
  async switchRole(role: Role): Promise<MeResponse> {
    const issued = await this.run(() => this.api.switchRole(role));
    if (this.expirationTimer) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = undefined;
    }
    return this.start(issued, true);
  }

  async restore(): Promise<boolean> {
    if (this.authenticated()) return true;
    try {
      await this.start(await this.api.refresh(), true);
      return true;
    } catch (error) {
      this.clearLocal();
      if (error instanceof ApiError && error.status === 401) return false;
      throw error;
    }
  }

  private async start(issued: TokenResponse, refreshable = false): Promise<MeResponse> {
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
    this.expirationTimer = setTimeout(
      () => refreshable ? void this.refreshSession() : this.clearLocal(),
      Math.max(1000, (issued.expiresIn - 30) * 1000),
    );
    try {
      const user = await this.api.me();
      if (!user.username || !user.role) {
        throw new Error('The server returned an invalid user profile.');
      }
      this.currentUser.set(user);
      return user;
    } catch (error) {
      this.clearLocal();
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
        this.clearLocal();
      }
      throw error;
    }
  }

  logout(): void {
    this.clearLocal();
    void this.api.endSession().catch((error: unknown) => {
      console.error('Could not revoke the server session', error);
    });
  }

  private clearLocal(): void {
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
      this.clearLocal();
    }
    return this.accessToken();
  }

  private async refreshSession(): Promise<void> {
    try {
      await this.start(await this.api.refresh(), true);
    } catch (error) {
      this.clearLocal();
      console.error('Could not refresh the application session', error);
    }
  }
}
