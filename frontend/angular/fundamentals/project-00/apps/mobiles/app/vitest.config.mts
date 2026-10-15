import { ngNative } from '@ng-native/testing/vitest';
import { nxViteTsPaths } from '@nx/vite/plugins/nx-tsconfig-paths.plugin';
import { defineConfig } from 'vitest/config';

// Compiles Angular for the tests the way Metro compiles it for the app. Tests run in Node against
// a fake of the native side: no simulator, no device.
export default defineConfig({
  plugins: [nxViteTsPaths(), ngNative()],
});
