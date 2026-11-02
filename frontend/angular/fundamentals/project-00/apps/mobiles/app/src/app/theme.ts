import type { Scheme } from '@ng-native/device';
import type { HeaderDefaults } from '@ng-native/router';

/**
 * One palette per system appearance; keep it in step with the CSS tokens in `app.ts`. Light is the paper-and-ink day theme; dark is an
 * "evening library" ink-navy rather than a flat near-black, with a softened periwinkle accent.
 */
export const PALETTE = {
  light: {
    bg: '#f4f5f8',
    surface: '#ffffff',
    raised: '#e8ecf4',
    border: '#d8dde6',
    text: '#172033',
    muted: '#596579',
    primary: '#2f5bea',
    onPrimary: '#ffffff',
    danger: '#c62828',
    onDanger: '#ffffff',
    errorBg: '#fdecea',
    errorFg: '#b71c1c',
    successBg: '#e6f4ea',
    successFg: '#1b5e20',
    backdrop: 'rgba(16, 24, 40, 0.48)',
  },
  dark: {
    bg: '#121826',
    surface: '#1a2233',
    raised: '#232d42',
    border: '#2f3a52',
    text: '#e6e9f2',
    muted: '#a0abc2',
    primary: '#8fa8ff',
    onPrimary: '#0e1630',
    danger: '#ff8f8f',
    onDanger: '#2a0f12',
    errorBg: '#3a1d24',
    errorFg: '#ffb4ab',
    successBg: '#16301f',
    successFg: '#8fd9a8',
    backdrop: 'rgba(4, 7, 14, 0.64)',
  },
} as const satisfies Record<Scheme, Record<string, string>>;

/** Native stack headers cannot read CSS tokens, so they take the palette for the current scheme. */
export const headerDefaults = (scheme: Scheme): HeaderDefaults => {
  const p = PALETTE[scheme];
  return {
    userInterfaceStyle: scheme,
    backgroundColor: p.surface,
    titleColor: p.text,
    largeTitleColor: p.text,
    color: p.primary,
  };
};
