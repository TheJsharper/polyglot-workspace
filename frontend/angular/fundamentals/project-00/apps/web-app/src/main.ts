import { initFederation } from '@angular-architects/native-federation';
import {
  completeStartupScreen,
  failStartupScreen,
  startStartupTimer,
} from './startup-status';

startStartupTimer();

async function startApplication(): Promise<void> {
  try {
    await initFederation('federation.manifest.json', {
      hostRemoteEntry: { url: './remoteEntry.json' },
    });
    const { bootstrap } = await import('./bootstrap');
    await bootstrap();
    completeStartupScreen();
  } catch (error) {
    console.error('Failed to start University', error);
    failStartupScreen(() => window.location.reload());
  }
}

void startApplication();
