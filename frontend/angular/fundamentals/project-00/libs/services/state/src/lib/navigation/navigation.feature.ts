import { createActionGroup, createFeature, createReducer, emptyProps, on, props } from '@ngrx/store';
import { MenuItem } from '@project-00/services-core';

export interface NavigationState {
  menuItems: MenuItem[];
  loading: boolean;
}

export const initialNavigationState: NavigationState = {
  menuItems: [],
  loading: false,
};

export const NavigationActions = createActionGroup({
  source: 'Navigation',
  events: {
    'Load Menu': emptyProps(),
    'Load Menu Success': props<{ menuItems: MenuItem[] }>(),
  },
});

export const navigationFeature = createFeature({
  name: 'navigation',
  reducer: createReducer(
    initialNavigationState,
    on(NavigationActions.loadMenu, (state): NavigationState => ({ ...state, loading: true })),
    on(NavigationActions.loadMenuSuccess, (state, { menuItems }): NavigationState => ({
      ...state,
      menuItems,
      loading: false,
    }))
  ),
});

export const { selectMenuItems, selectLoading: selectMenuLoading } = navigationFeature;
