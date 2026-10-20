#!/usr/bin/env node
// Local-only ES256 recruiter demo token helper. The private key stays in ignored .demo-keys/.
//   node scripts/demo-token.mjs keys
//   node scripts/demo-token.mjs issue [--label "local"] [--days 10] [--start-in-days 0]
import { createPrivateKey, generateKeyPairSync, randomUUID, sign } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';

const [command, ...args] = process.argv.slice(2);
const options = Object.fromEntries(
  args.flatMap((arg, index) => (arg.startsWith('--') ? [[arg.slice(2), args[index + 1]]] : [])),
);
const directory = '.demo-keys';
const base64url = (value) => Buffer.from(value).toString('base64url');

if (command === 'keys') {
  if (existsSync(`${directory}/private.pem`)) {
    console.error(`${directory}/private.pem already exists; remove it deliberately before rotating the local key.`);
    process.exit(1);
  }
  const { privateKey, publicKey } = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  mkdirSync(directory, { recursive: true });
  writeFileSync(`${directory}/private.pem`, privateKey.export({ type: 'pkcs8', format: 'pem' }), { mode: 0o600 });
  const publicPem = publicKey.export({ type: 'spki', format: 'pem' });
  writeFileSync(`${directory}/public.pem`, publicPem);
  console.log(publicPem);
} else if (command === 'issue') {
  const keyFile = options.key ?? `${directory}/private.pem`;
  if (!existsSync(keyFile)) {
    console.error(`Private key ${keyFile} not found. Run "keys" first.`);
    process.exit(1);
  }
  const days = Number(options.days ?? 10);
  const startInDays = Number(options['start-in-days'] ?? 0);
  if (!Number.isFinite(days) || days <= 0 || days > 30 || !Number.isFinite(startInDays) || startInDays < 0) {
    console.error('--days must be > 0 and <= 30; --start-in-days must be >= 0');
    process.exit(1);
  }
  const now = Math.floor(Date.now() / 1000);
  const notBefore = now + Math.round(startInDays * 86400);
  const claims = {
    jti: randomUUID(),
    iat: now,
    nbf: notBefore,
    exp: notBefore + Math.round(days * 86400),
    ...(options.label ? { label: options.label } : {}),
  };
  const unsigned = `${base64url(JSON.stringify({ alg: 'ES256', typ: 'JWT' }))}.${base64url(JSON.stringify(claims))}`;
  const signature = sign('sha256', Buffer.from(unsigned), {
    key: createPrivateKey(readFileSync(keyFile)),
    dsaEncoding: 'ieee-p1363',
  });
  console.error(`jti=${claims.jti} valid ${new Date(notBefore * 1000).toISOString()} -> ${new Date(claims.exp * 1000).toISOString()}`);
  console.log(`${unsigned}.${base64url(signature)}`);
} else {
  console.error('Usage: demo-token.mjs keys | issue [--label text] [--days 10] [--start-in-days 0] [--key file]');
  process.exit(1);
}
