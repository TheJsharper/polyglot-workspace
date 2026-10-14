import { LayoutActions, initialLayoutState, layoutFeature } from './layout.feature';

describe('layoutFeature', () => {
  const { reducer } = layoutFeature;

  it('toggles the sidenav', () => {
    const state = reducer(initialLayoutState, LayoutActions.toggleSidenav());
    expect(state.sidenavOpened).toBe(false);
  });

  it('sets the sidenav explicitly', () => {
    const state = reducer(initialLayoutState, LayoutActions.setSidenav({ opened: false }));
    expect(state.sidenavOpened).toBe(false);
  });

  it('toggles and sets the theme', () => {
    const dark = reducer(initialLayoutState, LayoutActions.toggleTheme());
    expect(dark.theme).toBe('dark');
    expect(reducer(dark, LayoutActions.setTheme({ theme: 'light' })).theme).toBe('light');
  });

  it('selects layout values', () => {
    const root = { layout: { sidenavOpened: false, theme: 'dark' as const } };
    expect(layoutFeature.selectSidenavOpened(root)).toBe(false);
    expect(layoutFeature.selectTheme(root)).toBe('dark');
  });
});
