import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { provideIcons } from '@ng-icons/core';
import { lucideUniversity } from '@ng-icons/lucide';
import { NgIcon } from '@ng-native/icons';
import type { GoogleOnboardingRequest } from '@project-00/services-api';
import * as Crypto from 'expo-crypto';
import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';
import { UniversitySession } from '@project-00/services-session';
import { injectTask, parseOptionalInt } from '../task.ts';
import { UI } from '../ui.ts';

const MOBILE_REDIRECT_URI = 'app://sso/callback';

/** Google's standard multi-colour "G" mark, as used on its sign-in buttons. */
const GOOGLE_MARK =
  '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">' +
  '<path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>' +
  '<path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>' +
  '<path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>' +
  '<path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>' +
  '</svg>';

function queryString(value: string | string[] | undefined): string | null {
  return typeof value === 'string' ? value : null;
}

@Component({
  selector: 'app-login-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [...UI, NgIcon],
  viewProviders: [provideIcons({ lucideUniversity })],
  template: `
    <app-page title="Sign in" [loading]="task.loading()" [error]="task.error()" [retryable]="false">
      @if (googleTicket()) {
        <text class="lead">Choose your University account type to finish Google sign-in.</text>
        <app-choice label="Account type" [options]="googleRoles" [(selected)]="googleRole" />
        @if (googleRole() === 'STUDENT') {
          <app-field label="Enrollment year" [(value)]="googleYear" keyboard="number-pad" />
        } @else {
          <app-field label="Department" [(value)]="googleDepartment" />
        }
        <app-button label="Continue" [disabled]="task.loading() || !canCompleteOnboarding()" (press)="completeOnboarding()" />
        <app-button label="Cancel" variant="secondary" [disabled]="task.loading()" (press)="cancelOnboarding()" />
      } @else {
        <view class="brand">
          <ng-icon name="lucideUniversity" [size]="40" color="var(--primary)" [strokeWidth]="1.75" />
          <text class="brand-name">University</text>
        </view>
        <text class="lead">Use the username provided by your University administrator, student id, or employee id.</text>
        @if (googleEnabled()) {
          <app-button
            label="Continue with Google"
            variant="secondary"
            [iconSvg]="googleMark"
            [disabled]="task.loading()"
            (press)="signInWithGoogle()"
          />
        } @else if (googleStatusError()) {
          <text class="status-error">{{ googleStatusError() }}</text>
        }
        <app-field label="Username" [(value)]="username" placeholder="University username or student/employee id" />
        <app-field label="Password" [(value)]="password" [secure]="true" [revealable]="true" />
        <app-button label="Sign in" [disabled]="task.loading() || !canSubmit()" (press)="submit()" />
        <app-button label="Create an account" variant="secondary" (press)="register()" />
      }
    </app-page>
  `,
  styles: `
    :host {
      flex: 1;
    }
    .brand {
      align-items: center;
      gap: 6px;
      padding-top: 8px;
      padding-bottom: 4px;
    }
    .brand-name {
      color: var(--text);
      font-size: 22px;
      font-weight: 700;
    }
    .lead {
      color: var(--muted);
      font-size: 15px;
    }
    .status-error {
      color: var(--error-fg);
      font-size: 14px;
    }
  `,
})
export class LoginScreenComponent {
  private readonly session = inject(UniversitySession);
  private readonly router = inject(Router);
  protected readonly task = injectTask();
  protected readonly googleMark = GOOGLE_MARK;
  readonly returnUrl = input<string>();
  protected readonly username = signal('');
  protected readonly password = signal('');
  protected readonly googleEnabled = signal(false);
  protected readonly googleStatusError = signal('');
  protected readonly googleTicket = signal('');
  private readonly googleVerifier = signal('');
  protected readonly googleRole = signal('STUDENT');
  protected readonly googleYear = signal('');
  protected readonly googleDepartment = signal('');
  protected readonly googleRoles = [
    { value: 'STUDENT', label: 'Student' },
    { value: 'PROFESSOR', label: 'Professor' },
  ];

  constructor() {
    void this.loadGoogleStatus();
  }

  protected canSubmit(): boolean {
    return !!this.username().trim() && !!this.password();
  }

  protected canCompleteOnboarding(): boolean {
    if (this.googleRole() === 'STUDENT') {
      const year = parseOptionalInt(this.googleYear());
      return year !== undefined && year >= 1900 && year <= 9999;
    }
    return this.googleRole() === 'PROFESSOR' && !!this.googleDepartment().trim();
  }

  protected async submit(): Promise<void> {
    const user = await this.task.run(() =>
      this.session.loginMobile({ username: this.username().trim(), password: this.password() }),
    );
    if (user) {
      this.password.set('');
      await this.navigateAfterSignIn(user.role);
    }
  }

  protected async signInWithGoogle(): Promise<void> {
    const callback = await this.task.run(async () => {
      const randomHex = async () =>
        Array.from(await Crypto.getRandomBytesAsync(32), (byte) => byte.toString(16).padStart(2, '0')).join('');
      const state = await randomHex();
      const verifier = await randomHex();
      const digest = await Crypto.digestStringAsync(Crypto.CryptoDigestAlgorithm.SHA256, verifier, {
        encoding: Crypto.CryptoEncoding.BASE64,
      });
      const codeChallenge = digest.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
      const authUrl = this.session.api.googleMobileStartUrl(state, codeChallenge);
      const result = await WebBrowser.openAuthSessionAsync(authUrl, MOBILE_REDIRECT_URI);
      if (result.type !== 'success') {
        return null;
      }

      const query = Linking.parse(result.url).queryParams ?? {};
      const returnedState = queryString(query['state']);
      if (returnedState !== state) {
        throw new Error('Google sign-in could not be verified. Please try again.');
      }
      if (queryString(query['error'])) {
        throw new Error('Google sign-in was not completed. Please try again.');
      }
      const ticket = queryString(query['ticket']);
      if (!ticket) {
        throw new Error('Google sign-in did not return a login ticket. Please try again.');
      }
      return { ticket, verifier };
    });
    if (!callback) {
      return;
    }

    const completed = await this.task.run(() =>
      this.session.completeGoogleMobile(callback.ticket, callback.verifier),
    );
    if (!completed) {
      return;
    }
    if (completed.onboardingRequired) {
      this.googleTicket.set(callback.ticket);
      this.googleVerifier.set(callback.verifier);
      return;
    }
    await this.navigateAfterSignIn(this.session.user()?.role);
  }

  protected async completeOnboarding(): Promise<void> {
    const onboarding: GoogleOnboardingRequest = {
      role: this.googleRole() === 'PROFESSOR' ? 'PROFESSOR' : 'STUDENT',
      ...(this.googleRole() !== 'PROFESSOR'
        ? { enrollmentYear: parseOptionalInt(this.googleYear()) }
        : { department: this.googleDepartment().trim() }),
    };
    const completed = await this.task.run(() =>
      this.session.completeGoogleMobile(this.googleTicket(), this.googleVerifier(), onboarding),
    );
    if (completed && !completed.onboardingRequired) {
      this.clearGoogleOnboarding();
      await this.navigateAfterSignIn(this.session.user()?.role);
    }
  }

  protected cancelOnboarding(): void {
    this.clearGoogleOnboarding();
  }

  protected register(): void {
    void this.router.navigate(['/register']);
  }

  private async loadGoogleStatus(): Promise<void> {
    try {
      const status = await this.session.api.googleStatus();
      this.googleEnabled.set(status.enabled);
      this.googleStatusError.set('');
    } catch {
      this.googleStatusError.set('Google sign-in status could not be checked. You can still sign in with your University account.');
    }
  }

  private clearGoogleOnboarding(): void {
    this.googleTicket.set('');
    this.googleVerifier.set('');
    this.googleYear.set('');
    this.googleDepartment.set('');
    this.googleRole.set('STUDENT');
  }

  private async navigateAfterSignIn(role: string | undefined): Promise<void> {
    const defaultRoute = role === 'ADMIN' ? '/university/students' : '/university';
    await this.router.navigateByUrl(this.returnUrl() || defaultRoute, { replaceUrl: true });
  }
}
