import { effect, EnvironmentProviders, inject, makeEnvironmentProviders, provideEnvironmentInitializer, untracked } from '@angular/core';
import { provideState, Store } from '@ngrx/store';
import { UniversitySession } from '@project-00/services-session';
import { AuthActions, authFeature, toAuthUser } from './auth.feature';

/** Registers the `auth` feature and mirrors `UniversitySession.user()` into the store (login, logout, expiry, 401). */
export function provideUniversityAuth(): EnvironmentProviders {
  return makeEnvironmentProviders([
    provideState(authFeature),
    provideEnvironmentInitializer(() => {
      const store = inject(Store);
      const session = inject(UniversitySession);
      const sync = () => store.dispatch(AuthActions.userChanged({ user: toAuthUser(session.user()) }));
      sync();
      effect(() => {
        session.user();
        untracked(sync);
      });
    }),
  ]);
}
