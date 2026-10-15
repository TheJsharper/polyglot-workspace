import { ChangeDetectionStrategy, Component } from '@angular/core';
import { HOME_STATS } from '@project-00/services-core';
import { ScrollView, Text, View } from '@ng-native/components';

@Component({
  selector: 'app-home-screen',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ScrollView, Text, View],
  template: `
    <scroll-view class="scroll">
      <view class="page">
        <text class="title">Home</text>
        <text class="subtitle">Your workspace at a glance.</text>
        <view class="stats">
          @for (stat of stats; track stat.label) {
            <view class="card">
              <text class="label">{{ stat.label }}</text>
              <text class="value">{{ stat.value }}</text>
            </view>
          }
        </view>
      </view>
    </scroll-view>
  `,
  styles: `
    :host {
      flex: 1;
    }
    .scroll {
      flex: 1;
    }
    .page {
      flex: 1;
      padding: 24px 20px;
      gap: 8px;
    }
    .title {
      color: #172033;
      font-size: 28px;
      font-weight: 700;
    }
    .subtitle {
      margin-bottom: 12px;
      color: #596579;
      font-size: 15px;
    }
    .stats {
      gap: 12px;
    }
    .card {
      padding: 18px;
      gap: 8px;
      border-radius: 14px;
      background-color: #ffffff;
    }
    .label {
      color: #596579;
      font-size: 14px;
    }
    .value {
      color: #172033;
      font-size: 25px;
      font-weight: 700;
    }
  `,
})
export class HomeScreenComponent {
  protected readonly stats = HOME_STATS;
}
