import { NavigationActions, initialNavigationState, navigationFeature } from './navigation.feature';

describe('navigationFeature', () => {
  const { reducer } = navigationFeature;
  const menuItems = [{ id: 'home', label: 'Home', icon: 'home', route: '/' }];

  it('marks the menu as loading', () => {
    expect(reducer(initialNavigationState, NavigationActions.loadMenu()).loading).toBe(true);
  });

  it('stores loaded menu items', () => {
    const loading = reducer(initialNavigationState, NavigationActions.loadMenu());
    const state = reducer(loading, NavigationActions.loadMenuSuccess({ menuItems }));
    expect(state).toEqual({ menuItems, loading: false });
  });
});
