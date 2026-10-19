import { EnvironmentProviders, isDevMode, makeEnvironmentProviders } from '@angular/core';
import { provideEffects } from '@ngrx/effects';
import { provideRouterStore, routerReducer } from '@ngrx/router-store';
import { Action, provideStore } from '@ngrx/store';
import { provideStoreDevtools } from '@ngrx/store-devtools';
import * as appEffects from './app.effects';
import { layoutFeature } from './layout/layout.feature';
import { navigationFeature } from './navigation/navigation.feature';

export function provideAppState(): EnvironmentProviders {
  return makeEnvironmentProviders([
    provideStore({
      [layoutFeature.name]: layoutFeature.reducer,
      [navigationFeature.name]: navigationFeature.reducer,
      router: routerReducer,
    }),
    provideEffects(appEffects),
    provideRouterStore(),
    isDevMode() ? provideStoreDevtools({ maxAge: 25, trace: false, actionSanitizer: redactPasswords }) : [],
  ]);
}

/** Keeps credentials (e.g. a new professor's temporary password) out of the Redux DevTools log. */
export function redactPasswords<A extends Action>(action: A): A {
  return JSON.parse(JSON.stringify(action), (key, value) => key === 'password' ? '[redacted]' : value) as A;
}
