import { createActionGroup, createFeature, createReducer, emptyProps, on, props } from '@ngrx/store';
import { Theme, nextTheme } from '@project-00/services-core';

export interface LayoutState {
  sidenavOpened: boolean;
  theme: Theme;
}

export const initialLayoutState: LayoutState = {
  sidenavOpened: true,
  theme: 'light',
};

export const LayoutActions = createActionGroup({
  source: 'Layout',
  events: {
    'Toggle Sidenav': emptyProps(),
    'Set Sidenav': props<{ opened: boolean }>(),
    'Set Theme': props<{ theme: Theme }>(),
    'Toggle Theme': emptyProps(),
  },
});

export const layoutFeature = createFeature({
  name: 'layout',
  reducer: createReducer(
    initialLayoutState,
    on(LayoutActions.toggleSidenav, (state): LayoutState => ({ ...state, sidenavOpened: !state.sidenavOpened })),
    on(LayoutActions.setSidenav, (state, { opened }): LayoutState => ({ ...state, sidenavOpened: opened })),
    on(LayoutActions.setTheme, (state, { theme }): LayoutState => ({ ...state, theme })),
    on(LayoutActions.toggleTheme, (state): LayoutState => ({ ...state, theme: nextTheme(state.theme) }))
  ),
});

export const { selectSidenavOpened, selectTheme } = layoutFeature;
