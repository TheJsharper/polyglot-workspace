import { ChangeDetectionStrategy, Component, DestroyRef, afterNextRender, inject } from '@angular/core';
import { View } from '@ng-native/components';
import { Animated, AnimatedStyle, Easing } from '@ng-native/components/animations';

const PULSE_MS = 800;
const PULSE_LOW = 0.45;

/**
 * The mobile counterpart of the web startup skeleton: a profile block and two post cards that pulse while a screen loads.
 * Every color is a theme token (`--surface`, `--raised`, `--border`), so it follows System, Light and Dark like the rest of the app.
 */
@Component({
  selector: 'app-skeleton',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AnimatedStyle, View],
  template: `
    <view class="skeleton" accessibilityRole="progressbar" accessibilityLabel="Loading" [animatedStyle]="{ opacity: pulse }">
      <view class="card profile">
        <view class="avatar"></view>
        <view class="lines">
          <view class="bar w60"></view>
          <view class="bar w40"></view>
        </view>
      </view>
      @for (post of posts; track post) {
        <view class="card">
          <view class="bar w50"></view>
          <view class="bar"></view>
          <view class="bar w80"></view>
          <view class="media"></view>
        </view>
      }
    </view>
  `,
  styles: `
    .skeleton {
      gap: 14px;
    }
    .card {
      gap: 10px;
      padding: 14px;
      border-radius: 12px;
      border-width: 1px;
      border-color: var(--border);
      background-color: var(--surface);
    }
    .profile {
      flex-direction: row;
      align-items: center;
      gap: 12px;
    }
    .lines {
      flex: 1;
      gap: 8px;
    }
    .avatar {
      width: 48px;
      height: 48px;
      border-radius: 24px;
      background-color: var(--raised);
    }
    .bar {
      height: 12px;
      border-radius: 6px;
      background-color: var(--raised);
    }
    .media {
      height: 96px;
      border-radius: 10px;
      background-color: var(--raised);
    }
    .w80 { width: 80%; }
    .w60 { width: 60%; }
    .w50 { width: 50%; }
    .w40 { width: 40%; }
  `,
})
export class AppSkeleton {
  protected readonly posts = [1, 2];
  protected readonly pulse = new Animated.Value(1);

  constructor() {
    let running = true;
    const timing = (toValue: number) =>
      Animated.timing(this.pulse, {
        toValue,
        duration: PULSE_MS,
        easing: Easing.inOut(Easing.ease),
        useNativeDriver: true,
      });
    const run = () => {
      if (!running) {
        return;
      }
      Animated.sequence([timing(PULSE_LOW), timing(1)]).start(({ finished }) => finished && run());
    };
    afterNextRender(run);
    inject(DestroyRef).onDestroy(() => {
      running = false;
      this.pulse.stopAnimation();
    });
  }
}
