import { UNIVERSITY_API_BASE_URL } from '@project-00/services-session';

const ANDROID_EMULATOR_URL = 'http://10.0.2.2:8080';
const LOCAL_URL = 'http://localhost:8080';

/**
 * `EXPO_PUBLIC_API_URL` wins when set (use the computer's LAN address for a physical device).
 * Otherwise the Android emulator reaches the host machine at 10.0.2.2 and the iOS simulator at localhost.
 */
export function resolveApiBaseUrl(platformOs: string, configured?: string): string {
  const url = configured?.trim();
  if (url) {
    return url.replace(/\/+$/, '');
  }
  return platformOs === 'android' ? ANDROID_EMULATOR_URL : LOCAL_URL;
}

export function provideUniversityApiUrl(baseUrl: string) {
  return { provide: UNIVERSITY_API_BASE_URL, useValue: baseUrl };
}