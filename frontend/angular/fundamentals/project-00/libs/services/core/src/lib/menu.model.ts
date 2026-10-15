export interface MenuItem {
  id: string;
  label: string;
  icon: string;
  route: string;
}

export const MAIN_MENU: MenuItem[] = [
  { id: 'home', label: 'Home', icon: 'home', route: '/' },
  { id: 'dashboard', label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
  { id: 'university', label: 'University', icon: 'school', route: '/university' },
];
