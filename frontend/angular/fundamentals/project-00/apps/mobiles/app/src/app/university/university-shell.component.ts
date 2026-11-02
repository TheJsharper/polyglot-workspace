import { Component, DestroyRef, computed, effect, inject, signal } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { provideIcons } from '@ng-icons/core';
import {
  lucideBookOpen,
  lucideCalendarClock,
  lucideGraduationCap,
  lucideKeyRound,
  lucideLogOut,
  lucideMenu,
  lucideNotebookPen,
  lucidePresentation,
  lucideUniversity,
} from '@ng-icons/lucide';
import { Pressable, SafeAreaView, Text, View } from '@ng-native/components';
import { NgIcon } from '@ng-native/icons';
import { NativeStackOutlet, NativeRouterLink } from '@ng-native/router';
import { UniversitySession } from '@project-00/services-session';
import { Access } from './access.ts';
import { SoftKeyboard } from './ui.ts';

const RESOURCES = [
  { id: 'students', label: 'Students', route: '/university/students', icon: 'lucideGraduationCap' },
  { id: 'professors', label: 'Professors', route: '/university/professors', icon: 'lucidePresentation' },
  { id: 'courses', label: 'Courses', route: '/university/courses', icon: 'lucideBookOpen' },
  { id: 'lectures', label: 'Lectures', route: '/university/lectures', icon: 'lucideCalendarClock' },
  { id: 'notes', label: 'Notes & reviews', route: '/university/notes', icon: 'lucideNotebookPen' },
  { id: 'demo-logins', label: 'Demo logins', route: '/university/demo-logins', icon: 'lucideKeyRound' },
] as const;

/** Hosts the University screens as their own native stack and leaves them when the session ends. */
@Component({
  selector: 'app-university-shell',
  imports: [NativeRouterLink, NativeStackOutlet, NgIcon, Pressable, SafeAreaView, Text, View],
  viewProviders: [
    provideIcons({
      lucideBookOpen,
      lucideCalendarClock,
      lucideGraduationCap,
      lucideKeyRound,
      lucideLogOut,
      lucideMenu,
      lucideNotebookPen,
      lucidePresentation,
      lucideUniversity,
    }),
  ],
  template: `
    <safe-area-view class="shell">
      <view class="header">
        <pressable
          accessibilityRole="button"
          accessibilityLabel="Open University navigation"
          class="icon-button"
          [hitSlop]="6"
          (press)="toggleDrawer()"
        >
          <ng-icon name="lucideMenu" [size]="24" color="var(--primary)" [strokeWidth]="2" />
        </pressable>
        <ng-icon name="lucideUniversity" [size]="20" color="var(--text)" [strokeWidth]="2" />
        <text class="title">University</text>
        <view class="account">
          <text class="username">{{ access.username() }}</text>
          <text class="role">{{ access.role() }}</text>
        </view>
        <pressable accessibilityRole="button" accessibilityLabel="Sign out" class="icon-button" [hitSlop]="6" (press)="signOut()">
          <ng-icon name="lucideLogOut" [size]="22" color="var(--primary)" [strokeWidth]="2" />
        </pressable>
      </view>

      <view class="content">
        <native-stack-outlet />
      </view>

      @if (drawerOpen()) {
        <view class="drawer-layer">
          <pressable
            accessibilityRole="button"
            accessibilityLabel="Close University navigation"
            class="backdrop"
            (press)="drawerOpen.set(false)"
          />
          <safe-area-view class="drawer">
            <view class="drawer-brand">
              <ng-icon name="lucideUniversity" [size]="28" color="var(--primary)" [strokeWidth]="2" />
              <text class="drawer-brand-name">University</text>
            </view>
            <text class="drawer-title">University resources</text>
            @for (item of resources(); track item.id) {
              <pressable
                [nativeRouterLink]="item.route"
                accessibilityRole="button"
                [accessibilityLabel]="'Open ' + item.label"
                [accessibilityState]="{ selected: item.route === currentUrl() }"
                class="drawer-item"
                [class.drawer-item-active]="item.route === currentUrl()"
                (press)="drawerOpen.set(false)"
              >
                <ng-icon
                  [name]="item.icon"
                  [size]="22"
                  [color]="item.route === currentUrl() ? 'var(--on-primary)' : 'var(--primary)'"
                  [strokeWidth]="2"
                />
                <text class="drawer-label" [class.drawer-label-active]="item.route === currentUrl()">{{ item.label }}</text>
              </pressable>
            }
          </safe-area-view>
        </view>
      }
    </safe-area-view>
  `,
  styles: `
    :host { flex: 1; }
    .shell { flex: 1; background-color: var(--bg); }
    .header {
      min-height: 58px;
      flex-direction: row;
      align-items: center;
      gap: 10px;
      padding-left: 8px;
      padding-right: 10px;
      background-color: var(--surface);
      border-bottom-width: 1px;
      border-bottom-color: var(--border);
    }
    .icon-button { width: 40px; height: 40px; justify-content: center; align-items: center; }
    .title { color: var(--text); font-size: 16px; font-weight: 700; }
    .account { flex: 1; align-items: flex-end; gap: 2px; }
    .username { color: var(--text); font-size: 11px; font-weight: 600; }
    .role { color: var(--muted); font-size: 10px; }
    .content { flex: 1; }
    .drawer-layer {
      position: absolute; top: 0; right: 0; bottom: 0; left: 0; z-index: 10; flex-direction: row;
    }
    .backdrop {
      position: absolute; top: 0; right: 0; bottom: 0; left: 0; background-color: var(--backdrop);
    }
    .drawer { width: 288px; padding: 16px; background-color: var(--surface); gap: 8px; }
    .drawer-brand { flex-direction: row; align-items: center; gap: 10px; margin-bottom: 8px; }
    .drawer-brand-name { color: var(--text); font-size: 20px; font-weight: 700; }
    .drawer-title { margin-bottom: 12px; color: var(--muted); font-size: 13px; font-weight: 700; }
    .drawer-item {
      min-height: 48px; flex-direction: row; align-items: center; gap: 12px; padding-left: 12px; padding-right: 12px;
      border-radius: 10px; background-color: var(--raised);
    }
    .drawer-item-active { background-color: var(--primary); }
    .drawer-label { color: var(--text); font-size: 16px; font-weight: 600; }
    .drawer-label-active { color: var(--on-primary); }
  `,
})
export class UniversityShellComponent {
  private readonly session: UniversitySession;
  protected readonly access = inject(Access);
  protected readonly drawerOpen = signal(false);
  private readonly keyboard = inject(SoftKeyboard);
  private readonly router = inject(Router);
  protected readonly currentUrl = signal(this.router.url.split(/[?#]/)[0]);
  protected readonly resources = computed(() => {
    const role = this.access.role();
    const allowed = role === 'ADMIN'
      ? ['students', 'professors', 'courses', 'lectures', 'notes', 'demo-logins']
      : role === 'PROFESSOR'
        ? ['students', 'courses', 'lectures', 'notes']
        : role === 'STUDENT'
          ? ['courses', 'lectures', 'notes']
          : role === 'MANAGER'
            ? ['demo-logins']
            : [];
    return RESOURCES
      .filter((item) => allowed.includes(item.id))
      .map((item) => role === 'PROFESSOR' && item.id === 'students' ? { ...item, label: 'My Students' } : item);
  });

  constructor() {
    this.session = inject(UniversitySession);
    const router = this.router;
    // A keyboard left up by the previous screen has nothing to type into on the next one.
    const events = router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        this.keyboard.dismiss();
      } else if (event instanceof NavigationEnd) {
        this.currentUrl.set(event.urlAfterRedirects.split(/[?#]/)[0]);
      }
    });
    inject(DestroyRef).onDestroy(() => events.unsubscribe());
    effect(() => {
      if (!this.session.authenticated()) {
        void router.navigate(['/login'], { replaceUrl: true });
      }
    });
  }

  protected signOut(): void {
    this.session.logout();
  }

  protected toggleDrawer(): void {
    this.keyboard.dismiss();
    this.drawerOpen.update((open) => !open);
  }
}