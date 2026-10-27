export async function proxyToApi({ request, env }) {
  const origin = env.API_ORIGIN;
  if (!origin) return new Response('API_ORIGIN is not configured', { status: 500 });

  const incoming = new URL(request.url);
  const target = new URL(incoming.pathname + incoming.search, origin);
  const upstream = new Request(target, request);
  const clientIp = request.headers.get('CF-Connecting-IP');
  const proxySecret = env.PROXY_SHARED_SECRET;

  upstream.headers.delete('CF-Connecting-IP');
  upstream.headers.delete('Forwarded');
  upstream.headers.delete('X-Forwarded-For');
  upstream.headers.delete('X-Real-IP');
  upstream.headers.delete('X-Trusted-Client-IP');
  upstream.headers.delete('X-Proxy-Shared-Secret');
  upstream.headers.set('X-Forwarded-Host', incoming.host);
  upstream.headers.set('X-Forwarded-Proto', 'https');
  if (clientIp && proxySecret) {
    upstream.headers.set('X-Trusted-Client-IP', clientIp);
    upstream.headers.set('X-Proxy-Shared-Secret', proxySecret);
  }
  return fetch(upstream, { redirect: 'manual' });
}
