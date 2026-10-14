import { DOCUMENT, inject } from '@angular/core';
import { Actions, ROOT_EFFECTS_INIT, createEffect, ofType } from '@ngrx/effects';
import { Store } from '@ngrx/store';
import { concatLatestFrom } from '@ngrx/operators';
import { DARK_THEME_CLASS, THEME_STORAGE_KEY, isTheme } from '@project-00/services-core';
import { map, tap } from 'rxjs';
import { LayoutActions, initialLayoutState, selectTheme } from './layout/layout.feature';
import { MENU_CONFIG } from './navigation/menu-config.token';
import { NavigationActions } from './navigation/navigation.feature';

export const restoreTheme$ = createEffect(
  (actions$ = inject(Actions), document = inject(DOCUMENT)) =>
    actions$.pipe(
      ofType(ROOT_EFFECTS_INIT),
      map(() => document.defaultView?.localStorage.getItem(THEME_STORAGE_KEY)),
      map((stored) => LayoutActions.setTheme({ theme: isTheme(stored) ? stored : initialLayoutState.theme }))
    ),
  { functional: true }
);

export const applyTheme$ = createEffect(
  (actions$ = inject(Actions), store = inject(Store), document = inject(DOCUMENT)) =>
    actions$.pipe(
      ofType(LayoutActions.setTheme, LayoutActions.toggleTheme),
      concatLatestFrom(() => store.select(selectTheme)),
      tap(([, theme]) => {
        document.documentElement.classList.toggle(DARK_THEME_CLASS, theme === 'dark');
        document.defaultView?.localStorage.setItem(THEME_STORAGE_KEY, theme);
      })
    ),
  { functional: true, dispatch: false }
);

export const loadMenuOnInit$ = createEffect(
  (actions$ = inject(Actions)) =>
    actions$.pipe(
      ofType(ROOT_EFFECTS_INIT),
      map(() => NavigationActions.loadMenu())
    ),
  { functional: true }
);

export const loadMenu$ = createEffect(
  (actions$ = inject(Actions), menuItems = inject(MENU_CONFIG)) =>
    actions$.pipe(
      ofType(NavigationActions.loadMenu),
      map(() => NavigationActions.loadMenuSuccess({ menuItems }))
    ),
  { functional: true }
);
