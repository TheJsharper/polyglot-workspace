export type Theme = 'light' | 'dark';

export const THEME_STORAGE_KEY = 'app.theme';
export const DARK_THEME_CLASS = 'dark-theme';

export function isTheme(value: unknown): value is Theme {
  return value === 'light' || value === 'dark';
}

export function nextTheme(theme: Theme): Theme {
  return theme === 'light' ? 'dark' : 'light';
}
