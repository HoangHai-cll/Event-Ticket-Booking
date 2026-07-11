# Performance & Best Practices

Ensure smooth animations at 60fps (or higher).

## Hardware Acceleration
- Prioritize transform properties: `x`, `y`, `z`, `rotation`, `scale`, `skew`.
- Avoid animating layout-triggering properties like `width`, `height`, `top`, `left`, `margin`.
- Use `will-change: transform` sparingly on elements that animate frequently.

## GSAP Specific Optimizations
- Use `gsap.quickSetter()` for frequently updated properties (e.g., mouse follow).
- Reuse timelines and tweens instead of creating new ones in every frame.
- Clean up animations when components unmount (especially in React/Vue).

## Debugging
- Use `gsap.globalTimeline.timeScale(0.1)` to slow down everything for inspection.
- Check the "Layers" tab in Chrome DevTools to see which elements are promoted to their own compositor layer.
- Monitor the FPS using the Performance panel.
```javascript
// High performance mouse tracker
const xSetter = gsap.quickSetter(".ball", "x", "px");
const ySetter = gsap.quickSetter(".ball", "y", "px");

window.addEventListener("mousemove", e => {    
  xSetter(e.x);
  ySetter(e.y);
});
```
