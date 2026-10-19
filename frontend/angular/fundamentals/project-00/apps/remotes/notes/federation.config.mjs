import { withNativeFederation, shareAll } from '@angular-architects/native-federation/config';

// Remote "notes": exposes its University routes; the shell loads them lazily through the manifest.
export default withNativeFederation({
  name: 'notes',

  exposes: {
    './Routes': './apps/remotes/notes/src/app/remote.routes.ts',
  },

  shared: {
    ...shareAll(
      { singleton: true, strictVersion: true, requiredVersion: 'auto', build: 'package' },
      {
        overrides: {
          '@angular/core': { singleton: true, strictVersion: true, requiredVersion: 'auto', build: 'package', includeSecondaries: { keepAll: true } },
        },
      },
    ),
  },

  skip: ['rxjs/ajax', 'rxjs/fetch', 'rxjs/testing', 'rxjs/webSocket'],

  features: {
    denseChunking: true,
  },
});
