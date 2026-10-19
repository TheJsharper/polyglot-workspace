#!/usr/bin/env node
// Offline tool for recruiter demo tokens (ES256 JWTs). The private key never goes on the server.
//   node scripts/demo-token.mjs keys                      -> writes .demo-keys/private.pem and public.pem
//   node scripts/demo-token.mjs issue [--label "ACME HR"] [--days 10] [--start-in-days 0] [--key .demo-keys/private.pem]
import { createPrivateKey, generateKeyPairSync, randomUUID, sign } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';

const [command, ...rest] = process.argv.slice(2);
const options = Object.fromEntries(
  rest.flatMap((arg, i) => (arg.startsWith('--') ? [[arg.slice(2), rest[i + 1]]] : [])),
);
const dir = '.demo-keys';
const b64url = (input) => Buffer.from(input).toString('base64url');

if (command === 'keys') {
  if (existsSync(`${dir}/private.pem`)) {
    console.error(`${dir}/private.pem already exists; delete it first to rotate keys.`);
    process.exit(1);
  }
  const { privateKey, publicKey } = generateKeyPairSync('ec', { namedCurve: 'prime256v1' });
  mkdirSync(dir, { recursive: true });
  writeFileSync(`${dir}/private.pem`, privateKey.export({ type: 'pkcs8', format: 'pem' }), { mode: 0o600 });
  const publicPem = publicKey.export({ type: 'spki', format: 'pem' });
  writeFileSync(`${dir}/public.pem`, publicPem);
  console.log(`Keys written to ${dir}/. Set this on the server as DEMO_PUBLIC_KEY:\n\n${publicPem}`);
} else if (command === 'issue') {
  const keyFile = options.key ?? `${dir}/private.pem`;
  if (!existsSync(keyFile)) {
    console.error(`Private key ${keyFile} not found. Run "keys" first.`);
    process.exit(1);
  }
  const days = Number(options.days ?? 10);
  const startIn = Number(options['start-in-days'] ?? 0);
  if (!Number.isFinite(days) || days <= 0 || !Number.isFinite(startIn) || startIn < 0) {
    console.error('--days must be > 0 and --start-in-days >= 0');
    process.exit(1);
  }
  const now = Math.floor(Date.now() / 1000);
  const nbf = now + Math.round(startIn * 86400);
  const payload = {
    jti: randomUUID(),
    iat: now,
    nbf,
    exp: nbf + Math.round(days * 86400),
    ...(options.label ? { label: options.label } : {}),
  };
  const unsigned = `${b64url(JSON.stringify({ alg: 'ES256', typ: 'JWT' }))}.${b64url(JSON.stringify(payload))}`;
  const signature = sign('sha256', Buffer.from(unsigned), {
    key: createPrivateKey(readFileSync(keyFile)),
    dsaEncoding: 'ieee-p1363',
  });
  console.error(`jti=${payload.jti} valid ${new Date(nbf * 1000).toISOString()} -> ${new Date(payload.exp * 1000).toISOString()}`);
  console.log(`${unsigned}.${b64url(signature)}`);
} else {
  console.error('Usage: demo-token.mjs keys | issue [--label text] [--days 10] [--start-in-days 0] [--key file]');
  process.exit(1);
}
