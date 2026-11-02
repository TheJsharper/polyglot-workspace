import { UNIVERSITY_API_BASE_URL } from '@project-00/services-session';

const LOCAL_URL = 'http://localhost:8080';

/**
 * `EXPO_PUBLIC_API_URL` wins when set (use the computer's LAN address for a physical device without USB).
 * Otherwise both simulators use localhost. On Android run `adb reverse tcp:8080 tcp:8080` so localhost reaches
 * the host: Google SSO needs the API on the same host as the registered `http://localhost:8080` OAuth callback,
 * otherwise the browser session that holds the PKCE state is lost (Google also rejects private IPs like 10.0.2.2).
 */
export function resolveApiBaseUrl(_platformOs: string, configured?: string): string {
  const url = configured?.trim();
  if (url) {
    return url.replace(/\/+$/, '');
  }
  return LOCAL_URL;
}

export function provideUniversityApiUrl(baseUrl: string) {
  return { provide: UNIVERSITY_API_BASE_URL, useValue: baseUrl };
}