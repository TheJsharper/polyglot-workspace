/**
 * With Module Federation every entry chunk embeds its own runtime. In
 * dev-server builds Angular also emits a styles.js entry even though the
 * stylesheet is linked separately. Loading the entry installs a second
 * Webpack chunk handler, preventing the host from loading remote routes.
 */
export default function mfIndexHtml(_target: unknown, indexHtml: string): string {
  return indexHtml.replace(
    /<script src="(styles(?:\.[\w-]+)?\.js)" defer><\/script>/g,
    '',
  );
}
