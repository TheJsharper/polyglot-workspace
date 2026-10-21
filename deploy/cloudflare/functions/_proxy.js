export async function proxyToApi({ request, env }) {
  const origin = env.API_ORIGIN;
  if (!origin) return new Response('API_ORIGIN is not configured', { status: 500 });

  const incoming = new URL(request.url);
  const target = new URL(incoming.pathname + incoming.search, origin);
  const upstream = new Request(target, request);
  upstream.headers.set('X-Forwarded-Host', incoming.host);
  upstream.headers.set('X-Forwarded-Proto', 'https');
  return fetch(upstream, { redirect: 'manual' });
}
