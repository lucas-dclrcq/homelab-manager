// jsdom n'implémente pas <canvas>. Stub minimal pour éviter un crash si un test monte,
// directement ou indirectement, un composant Chart.js. On ne teste pas le rendu des charts.
if (!HTMLCanvasElement.prototype.getContext) {
  HTMLCanvasElement.prototype.getContext = (() =>
    null) as unknown as typeof HTMLCanvasElement.prototype.getContext
}
