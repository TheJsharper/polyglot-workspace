export type Theme = 'system' | 'light' | 'dark';

export const THEME_STORAGE_KEY = 'app.theme';
export const DARK_THEME_CLASS = 'dark-theme';
export const LIGHT_THEME_CLASS = 'light-theme';

export function isTheme(value: unknown): value is Theme {
  return value === 'system' || value === 'light' || value === 'dark';
}

export function nextTheme(theme: Theme): Theme {
  if (theme === 'system') return 'light';
  return theme === 'light' ? 'dark' : 'system';
}
