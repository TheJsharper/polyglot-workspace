import { ChangeDetectionStrategy, Component, input, model, output } from '@angular/core';
import { ActivityIndicator, Pressable, ScrollView, Text, TextInput, View } from '@ng-native/components';
import type { KeyboardType } from '@ng-native/components';
import { NativeHeader } from '@ng-native/router';

export const COLORS = {
  text: '#172033',
  muted: '#596579',
  primary: '#2f5bea',
  danger: '#c62828',
  surface: '#ffffff',
  border: '#d8dde6',
};

export type ButtonVariant = 'primary' | 'secondary' | 'danger';

@Component({
  selector: 'app-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Pressable, Text],
  template: `
    <pressable
      accessibilityRole="button"
      [accessibilityLabel]="label()"
      [accessibilityState]="{ disabled: disabled() }"
      [style]="buttonStyle()"
      (press)="onPress()"
    >
      <text [style]="labelStyle()">{{ label() }}</text>
    </pressable>
  `,
})
export class AppButton {
  readonly label = input.required<string>();
  readonly variant = input<ButtonVariant>('primary');
  readonly disabled = input(false);
  readonly press = output<void>();

  protected buttonStyle() {
    const variant = this.variant();
    return {
      minHeight: 44,
      justifyContent: 'center' as const,
      alignItems: 'center' as const,
      paddingHorizontal: 16,
      borderRadius: 10,
      opacity: this.disabled() ? 0.5 : 1,
      backgroundColor: variant === 'primary' ? COLORS.primary : variant === 'danger' ? COLORS.danger : '#e8ecf4',
    };
  }

  protected labelStyle() {
    return { fontSize: 15, fontWeight: '700' as const, color: this.variant() === 'secondary' ? COLORS.text : '#ffffff' };
  }

  protected onPress(): void {
    if (!this.disabled()) {
      this.press.emit();
    }
  }
}

@Component({
  selector: 'app-field',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Text, TextInput, View],
  template: `
    <view class="field">
      <text class="label">{{ label() }}</text>
      <text-input
        class="input"
        [accessibilityLabel]="label()"
        [placeholder]="placeholder()"
        [(value)]="value"
        [secureTextEntry]="secure()"
        [keyboardType]="keyboard()"
        [multiline]="multiline()"
        [editable]="!readonly()"
        autoCapitalize="none"
        [autoCorrect]="false"
      />
    </view>
  `,
  styles: `
    .field {
      gap: 6px;
    }
    .label {
      color: #596579;
      font-size: 13px;
      font-weight: 700;
    }
    .input {
      min-height: 44px;
      padding-left: 12px;
      padding-right: 12px;
      border-width: 1px;
      border-color: #d8dde6;
      border-radius: 10px;
      background-color: #ffffff;
      color: #172033;
      font-size: 16px;
    }
  `,
})
export class AppField {
  readonly label = input.required<string>();
  readonly value = model('');
  readonly placeholder = input<string>();
  readonly secure = input(false);
  readonly multiline = input(false);
  readonly readonly = input(false);
  readonly keyboard = input<KeyboardType>('default');
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
      background-color: #ffffff;
    }
    .main {
      flex: 1;
      gap: 2px;
    }
    .title {
      color: #172033;
      font-size: 16px;
      font-weight: 700;
    }
    .subtitle {
      color: #596579;
      font-size: 13px;
    }
    .badge {
      color: #2f5bea;
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
            [style]="chipStyle(option.value === selected())"
            (press)="select(option.value)"
          >
            <text [style]="chipText(option.value === selected())">{{ option.label }}</text>
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
      color: #596579;
      font-size: 13px;
      font-weight: 700;
    }
    .chips {
      flex-direction: row;
      flex-wrap: wrap;
      gap: 8px;
    }
    .empty {
      color: #596579;
      font-size: 13px;
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

  protected chipStyle(active: boolean) {
    return {
      paddingHorizontal: 12,
      paddingVertical: 8,
      borderRadius: 16,
      borderWidth: 1,
      borderColor: active ? COLORS.primary : COLORS.border,
      backgroundColor: active ? COLORS.primary : COLORS.surface,
    };
  }

  protected chipText(active: boolean) {
    return { fontSize: 14, fontWeight: '600' as const, color: active ? '#ffffff' : COLORS.text };
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
      border-color: #c62828;
      border-radius: 12px;
      background-color: #fdecea;
    }
    .message {
      color: #7f1d1d;
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
      color: #172033;
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
  imports: [ActivityIndicator, AppButton, NativeHeader, ScrollView, Text, View],
  host: { style: 'flex: 1' },
  template: `
    <native-header [title]="title()" />
    <scroll-view class="scroll" keyboardShouldPersistTaps="handled">
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
      background-color: #f4f5f8;
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
      color: #596579;
    }
    .error {
      gap: 10px;
      padding: 12px;
      border-radius: 10px;
      background-color: #fdecea;
    }
    .error-text {
      color: #b71c1c;
      font-size: 14px;
    }
    .notice {
      padding: 12px;
      border-radius: 10px;
      background-color: #e6f4ea;
    }
    .notice-text {
      color: #1b5e20;
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
}

@Component({
  selector: 'app-row',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: '<ng-content />',
  styles: ':host { flex-direction: row; flex-wrap: wrap; gap: 8px; }',
})
export class AppRow {}

export const UI = [AppButton, AppCard, AppChoice, AppConfirm, AppField, AppPage, AppRow, AppSection, Text, View] as const;