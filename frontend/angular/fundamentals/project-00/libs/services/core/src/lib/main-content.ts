export interface FeatureStat {
  label: string;
  value: string;
  icon: string;
}

export const HOME_STATS: FeatureStat[] = [
  { label: 'Libraries', value: '9', icon: 'folder' },
  { label: 'Remotes', value: '1', icon: 'hub' },
];

export const DASHBOARD_STATS: FeatureStat[] = [
  { label: 'Active users', value: '1,284', icon: 'group' },
  { label: 'Revenue', value: '$48.2k', icon: 'payments' },
  { label: 'Orders', value: '326', icon: 'shopping_cart' },
  { label: 'Uptime', value: '99.98%', icon: 'monitor_heart' },
];
