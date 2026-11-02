import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import { ngNative } from '@ng-native/testing/vitest';
import { nxViteTsPaths } from '@nx/vite/plugins/nx-tsconfig-paths.plugin';
import { defineConfig } from 'vitest/config';

const componentsDir = dirname(createRequire(import.meta.url).resolve('@ng-native/components/package.json'));

// Compiles Angular for the tests the way Metro compiles it for the app. Tests run in Node against
// a fake of the native side: no simulator, no device.
export default defineConfig({
  plugins: [nxViteTsPaths(), ngNative()],
  // React Native's Animated source cannot load in Node; the package ships a React-free build with the same API.
  resolve: {
    alias: [
      {
        find: /^@ng-native\/components\/animations$/,
        replacement: join(componentsDir, 'dist', 'animations-web.js'),
      },
    ],
  },
});
