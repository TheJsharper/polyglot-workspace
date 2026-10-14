/**
 * With Module Federation every entry chunk embeds the federation runtime, which
 * uses `import.meta`. In dev-server builds Angular keeps a `styles.js` entry
 * stub and injects it as a classic script, which throws and triggers an endless
 * live-reload loop. Loading it as an ES module fixes that.
 */
export default function mfIndexHtml(_target: unknown, indexHtml: string): string {
  return indexHtml.replace(
    /<script src="(styles(?:\.[\w-]+)?\.js)" defer><\/script>/g,
    '<script src="$1" type="module"></script>',
  );
}
