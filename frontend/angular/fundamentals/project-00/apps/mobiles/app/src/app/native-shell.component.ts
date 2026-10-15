import { Component, signal } from '@angular/core';
import { MAIN_MENU } from '@project-00/services-core';
import { Pressable, SafeAreaView, Text, View } from '@ng-native/components';
import { NativeRouterLink, NativeTab, NativeTabsOutlet } from '@ng-native/router';

@Component({
  selector: 'app-native-shell',
  imports: [NativeRouterLink, NativeTab, NativeTabsOutlet, Pressable, SafeAreaView, Text, View],
  template: `
    <safe-area-view class="shell">
      <view class="header">
        <pressable
          accessibilityRole="button"
          accessibilityLabel="Open navigation menu"
          class="menu-button"
          (press)="toggleDrawer()"
        >
          <text class="menu-glyph">Menu</text>
        </pressable>
        <text class="app-title">Polyglot Workspace</text>
      </view>

      <view class="content">
        <native-tabs-outlet>
          <native-tab path="" title="Home" />
          <native-tab path="dashboard" title="Dashboard" />
        </native-tabs-outlet>
      </view>

      @if (drawerOpen()) {
        <view class="drawer-layer">
          <pressable
            accessibilityRole="button"
            accessibilityLabel="Close navigation menu"
            class="backdrop"
            (press)="closeDrawer()"
          />
          <view class="drawer">
            <text class="drawer-title">Main navigation</text>
            @for (item of menuItems; track item.id) {
              <pressable
                [nativeRouterLink]="item.route"
                accessibilityRole="button"
                [accessibilityLabel]="'Open ' + item.label"
                class="drawer-item"
                (press)="closeDrawer()"
              >
                <text class="drawer-item-label">{{ item.label }}</text>
              </pressable>
            }
          </view>
        </view>
      }
    </safe-area-view>
  `,
  styles: `
    :host {
      flex: 1;
    }
    .shell {
      flex: 1;
      background-color: #f4f5f8;
    }
    .header {
      min-height: 58px;
      flex-direction: row;
      align-items: center;
      gap: 16px;
      padding-left: 16px;
      padding-right: 16px;
      background-color: #ffffff;
      border-bottom-width: 1px;
      border-bottom-color: #e5e7eb;
    }
    .menu-button {
      padding: 10px;
    }
    .menu-glyph {
      color: #26334a;
      font-size: 14px;
      font-weight: 700;
    }
    .app-title {
      color: #172033;
      font-size: 18px;
      font-weight: 700;
    }
    .content {
      flex: 1;
    }
    .drawer-layer {
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      z-index: 10;
      flex-direction: row;
    }
    .backdrop {
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      background-color: rgba(16, 24, 40, 0.48);
    }
    .drawer {
      width: 288px;
      padding: 24px 16px;
      background-color: #ffffff;
      gap: 8px;
    }
    .drawer-title {
      margin-bottom: 12px;
      color: #596579;
      font-size: 13px;
      font-weight: 700;
    }
    .drawer-item {
      min-height: 48px;
      justify-content: center;
      padding-left: 12px;
      padding-right: 12px;
      border-radius: 10px;
      background-color: #f4f5f8;
    }
    .drawer-item-label {
      color: #172033;
      font-size: 16px;
      font-weight: 600;
    }
  `,
})
export class NativeShellComponent {
  protected readonly menuItems = MAIN_MENU;
  protected readonly drawerOpen = signal(false);

  protected toggleDrawer(): void {
    this.drawerOpen.update((open) => !open);
  }

  protected closeDrawer(): void {
    this.drawerOpen.set(false);
  }
}
