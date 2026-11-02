import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  Injectable,
  afterNextRender,
  computed,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { provideIcons } from '@ng-icons/core';
import { lucideEye, lucideEyeOff } from '@ng-icons/lucide';
import { ActivityIndicator, Pressable, ScrollView, Text, TextInput, View } from '@ng-native/components';
import type { KeyboardType } from '@ng-native/components';
import { ColorScheme, Keyboard } from '@ng-native/device';
import { NgIcon } from '@ng-native/icons';
import { NativeHeader, NativeHeaderItem } from '@ng-native/router';
import { PALETTE } from '../theme.ts';
import { ThemeToggle } from '../theme-preference.ts';

export type ButtonVariant = 'primary' | 'secondary' | 'danger';

/**
 * Puts the keyboard away. RN's `Keyboard.dismiss()` only blurs inputs RN itself tracks, which on Android misses ng-native's
 * `<text-input>`, so every `app-field` registers here and the focused one is blurred directly as well.
 */
@Injectable({ providedIn: 'root' })
export class SoftKeyboard {
  private readonly keyboard = inject(Keyboard);
  private readonly fields = new Set<TextInput>();

  register(field: TextInput): () => void {
    this.fields.add(field);
    return () => this.fields.delete(field);
  }

  dismiss(): void {
    for (const field of this.fields) {
      if (field.isFocused()) {
        field.blur();
      }
    }
    this.keyboard.dismiss();
  }
}

@Component({
  selector: 'app-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon, Pressable, Text],
  template: `
    <pressable
      accessibilityRole="button"
      class="button"
      [class.primary]="variant() === 'primary'"
      [class.secondary]="variant() === 'secondary'"
      [class.danger]="variant() === 'danger'"
      [class.disabled]="disabled()"
      [accessibilityLabel]="label()"
      [accessibilityState]="{ disabled: disabled() }"
      (press)="onPress()"
    >
      @if (iconSvg(); as svg) {
        <ng-icon [svg]="svg" [size]="20" />
      }
      <text
        class="label"
        [class.on-primary]="variant() === 'primary'"
        [class.on-secondary]="variant() === 'secondary'"
        [class.on-danger]="variant() === 'danger'"
      >{{ label() }}</text>
    </pressable>
  `,
  styles: `
    .button {
      min-height: 44px;
      flex-direction: row;
      gap: 10px;
      justify-content: center;
      align-items: center;
      padding-left: 16px;
      padding-right: 16px;
      border-radius: 10px;
    }
    .primary { background-color: var(--primary); }
    .secondary { background-color: var(--raised); }
    .danger { background-color: var(--danger); }
    .disabled { opacity: 0.5; }
    .label { font-size: 15px; font-weight: 700; }
    .on-primary { color: var(--on-primary); }
    .on-secondary { color: var(--text); }
    .on-danger { color: var(--on-danger); }
  `,
})
export class AppButton {
  readonly label = input.required<string>();
  readonly variant = input<ButtonVariant>('primary');
  readonly disabled = input(false);
  /** Decorative leading icon as raw SVG markup, e.g. a multi-colour brand mark. */
  readonly iconSvg = input<string>();
  readonly press = output<void>();
  private readonly keyboard = inject(SoftKeyboard);

  protected onPress(): void {
    if (!this.disabled()) {
      this.keyboard.dismiss();
      this.press.emit();
    }
  }
}

@Component({
  selector: 'app-field',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon, Pressable, Text, TextInput, View],
  viewProviders: [provideIcons({ lucideEye, lucideEyeOff })],
  template: `
    <view class="field">
      <text class="label">{{ label() }}</text>
      <view class="box">
        <text-input
          class="input"
          [accessibilityLabel]="label()"
          [placeholder]="placeholder()"
          [placeholderTextColor]="placeholderColor()"
          [(value)]="value"
          [secureTextEntry]="secure() && !revealed()"
          [keyboardType]="keyboard()"
          [multiline]="multiline()"
          [editable]="!readonly()"
          autoCapitalize="none"
          [autoCorrect]="false"
        />
        @if (secure() && revealable()) {
          <pressable
            accessibilityRole="button"
            class="reveal"
            [accessibilityLabel]="revealed() ? 'Hide ' + label() : 'Show ' + label()"
            [accessibilityState]="{ checked: revealed() }"
            [hitSlop]="6"
            (press)="toggleReveal()"
          >
            <ng-icon [name]="revealed() ? 'lucideEyeOff' : 'lucideEye'" [size]="20" color="var(--muted)" [strokeWidth]="2" />
          </pressable>
        }
      </view>
    </view>
  `,
  styles: `
    .field {
      gap: 6px;
    }
    .label {
      color: var(--muted);
      font-size: 13px;
      font-weight: 700;
    }
    .box {
      flex-direction: row;
      align-items: center;
      min-height: 44px;
      border-width: 1px;
      border-color: var(--border);
      border-radius: 10px;
      background-color: var(--surface);
    }
    .input {
      flex: 1;
      min-height: 44px;
      padding-left: 12px;
      padding-right: 12px;
      color: var(--text);
      font-size: 16px;
    }
    .reveal {
      width: 44px;
      height: 44px;
      justify-content: center;
      align-items: center;
    }
  `,
})
export class AppField {
  readonly label = input.required<string>();
  readonly value = model('');
  readonly placeholder = input<string>();
  readonly secure = input(false);
  /** Adds an eye button to a secure field so users can check what they typed. */
  readonly revealable = input(false);
  readonly multiline = input(false);
  readonly readonly = input(false);
  readonly keyboard = input<KeyboardType>('default');
  protected readonly revealed = signal(false);
  // A prop cannot read the CSS cascade, so the placeholder takes the palette for the current scheme.
  private readonly scheme = inject(ColorScheme);
  protected readonly placeholderColor = computed(() => PALETTE[this.scheme.current()].muted);
  private readonly input = viewChild.required(TextInput);

  constructor() {
    const keyboard = inject(SoftKeyboard);
    let unregister: (() => void) | undefined;
    afterNextRender(() => (unregister = keyboard.register(this.input())));
    inject(DestroyRef).onDestroy(() => unregister?.());
  }

  protected toggleReveal(): void {
    this.revealed.update((revealed) => !revealed);
  }
}

@Component({
  selector: 'app-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Pressable, Text, View],
  template: `
    <pressable accessibilityRole="button" [accessibilityLabel]="title()" class="card" (press)="press.emit()">
      <view class="main">
        <text class="title">{{ title() }}</text>
        @if (subtitle()) {
          <text class="subtitle">{{ subtitle() }}</text>
        }
      </view>
      @if (badge()) {
        <text class="badge">{{ badge() }}</text>
      }
    </pressable>
  `,
  styles: `
    .card {
      flex-direction: row;
      align-items: center;
      gap: 12px;
      padding: 14px;
      border-radius: 12px;
      border-width: 1px;
      border-color: var(--border);
      background-color: var(--surface);
    }
    .main {
      flex: 1;
      gap: 2px;
    }
    .title {
      color: var(--text);
      font-size: 16px;
      font-weight: 700;
    }
    .subtitle {
      color: var(--muted);
      font-size: 13px;
    }
    .badge {
      color: var(--primary);
      font-size: 12px;
      font-weight: 700;
    }
  `,
})
export class AppCard {
  readonly title = input.required<string>();
  readonly subtitle = input<string>();
  readonly badge = input<string>();
  readonly press = output<void>();
}

export interface Choice {
  value: string;
  label: string;
}

@Component({
  selector: 'app-choice',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Pressable, Text, View],
  template: `
    <view class="group">
      <text class="label">{{ label() }}</text>
      <view class="chips">
        @for (option of options(); track option.value) {
          <pressable
            accessibilityRole="button"
            [accessibilityLabel]="label() + ': ' + option.label"
            [accessibilityState]="{ selected: option.value === selected() }"
            class="chip"
            [class.chip-active]="option.value === selected()"
            (press)="select(option.value)"
          >
            <text class="chip-text" [class.chip-text-active]="option.value === selected()">{{ option.label }}</text>
          </pressable>
        } @empty {
          <text class="empty">{{ empty() }}</text>
        }
      </view>
    </view>
  `,
  styles: `
    .group {
      gap: 6px;
    }
    .label {
      color: var(--muted);
      font-size: 13px;
      font-weight: 700;
    }
    .chips {
      flex-direction: row;
      flex-wrap: wrap;
      gap: 8px;
    }
    .empty {
      color: var(--muted);
      font-size: 13px;
    }
    .chip {
      padding: 8px 12px;
      border-radius: 16px;
      border-width: 1px;
      border-color: var(--border);
      background-color: var(--surface);
    }
    .chip-active {
      border-color: var(--primary);
      background-color: var(--primary);
    }
    .chip-text {
      color: var(--text);
      font-size: 14px;
      font-weight: 600;
    }
    .chip-text-active {
      color: var(--on-primary);
    }
  `,
})
export class AppChoice {
  readonly label = input.required<string>();
  readonly options = input.required<Choice[]>();
  readonly selected = model('');
  readonly empty = input('Nothing to choose from.');
  /** Pressing the selected option again clears the choice when true. */
  readonly clearable = input(false);

  protected select(value: string): void {
    this.selected.set(this.clearable() && this.selected() === value ? '' : value);
  }
}

@Component({
  selector: 'app-confirm',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AppButton, Text, View],
  template: `
    <view class="box" accessibilityRole="alert">
      <text class="message">{{ message() }}</text>
      <view class="actions">
        <app-button label="Cancel" variant="secondary" (press)="cancel.emit()" />
        <app-button [label]="confirmLabel()" variant="danger" (press)="confirm.emit()" />
      </view>
    </view>
  `,
  styles: `
    .box {
      gap: 12px;
      padding: 14px;
      border-width: 1px;
      border-color: var(--danger);
      border-radius: 12px;
      background-color: var(--error-bg);
    }
    .message {
      color: var(--error-fg);
      font-size: 15px;
      font-weight: 600;
    }
    .actions {
      flex-direction: row;
      gap: 8px;
    }
  `,
})
export class AppConfirm {
  readonly message = input.required<string>();
  readonly confirmLabel = input('Delete');
  readonly confirm = output<void>();
  readonly cancel = output<void>();
}

@Component({
  selector: 'app-section',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Text, View],
  template: `
    <view class="section">
      <text class="heading">{{ title() }}</text>
      <ng-content />
    </view>
  `,
  styles: `
    .section {
      gap: 8px;
    }
    .heading {
      color: var(--text);
      font-size: 18px;
      font-weight: 700;
    }
  `,
})
export class AppSection {
  readonly title = input.required<string>();
}

/** A native-header screen with explicit loading, error and success states above its projected content. */
@Component({
  selector: 'app-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ActivityIndicator, AppButton, NativeHeader, NativeHeaderItem, ScrollView, Text, ThemeToggle, View],
  host: { style: 'flex: 1' },
  template: `
    <native-header [title]="title()">
      <native-header-item type="right">
        <app-theme-toggle />
      </native-header-item>
    </native-header>
    <!-- "handled" lets buttons work with the keyboard up while a tap on empty space or a drag closes it. -->
    <scroll-view class="scroll" keyboardShouldPersistTaps="handled" (scrollBeginDrag)="keyboard.dismiss()">
      <view class="body">
        @if (loading()) {
          <view class="loading" accessibilityRole="progressbar" accessibilityLabel="Loading">
            <activity-indicator />
            <text class="muted">Loading…</text>
          </view>
        }
        @if (error(); as message) {
          <view class="error" accessibilityRole="alert">
            <text class="error-text">{{ message }}</text>
            @if (retryable()) {
              <app-button label="Retry" variant="secondary" (press)="retry.emit()" />
            }
          </view>
        }
        @if (notice(); as message) {
          <view class="notice" accessibilityRole="alert">
            <text class="notice-text">{{ message }}</text>
          </view>
        }
        <ng-content />
      </view>
    </scroll-view>
  `,
  styles: `
    .scroll {
      flex: 1;
      background-color: var(--bg);
    }
    .body {
      padding: 20px 16px 32px 16px;
      gap: 14px;
    }
    .loading {
      flex-direction: row;
      align-items: center;
      gap: 10px;
    }
    .muted {
      color: var(--muted);
    }
    .error {
      gap: 10px;
      padding: 12px;
      border-radius: 10px;
      background-color: var(--error-bg);
    }
    .error-text {
      color: var(--error-fg);
      font-size: 14px;
    }
    .notice {
      padding: 12px;
      border-radius: 10px;
      background-color: var(--success-bg);
    }
    .notice-text {
      color: var(--success-fg);
      font-size: 14px;
    }
  `,
})
export class AppPage {
  readonly title = input.required<string>();
  readonly loading = input(false);
  readonly error = input<string | null>(null);
  readonly notice = input<string | null>(null);
  readonly retryable = input(true);
  readonly retry = output<void>();
  protected readonly keyboard = inject(SoftKeyboard);
}

@Component({
  selector: 'app-row',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '<ng-content />',
  styles: ':host { flex-direction: row; flex-wrap: wrap; gap: 8px; }',
})
export class AppRow {}

export const UI = [AppButton, AppCard, AppChoice, AppConfirm, AppField, AppPage, AppRow, AppSection, Text, View] as const;