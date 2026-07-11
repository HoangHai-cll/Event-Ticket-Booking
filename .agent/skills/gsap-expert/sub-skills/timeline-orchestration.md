# Timeline Orchestration

Manage complex sequences of animations with `gsap.timeline()`.

## Advantages of Timelines
- Chain multiple tweens together.
- Control the entire sequence (play, pause, reverse, restart).
- Use labels and position parameters for precise timing.
- Easily re-order animations without manually recalculating delays.

## Position Parameter
- `"+=1"`: 1 second after the end of the timeline.
- `"-=0.5"`: 0.5 seconds overlap with the previous tween.
- `"<"`: Start at the same time as the previous tween.
- `"<0.5"`: 0.5 seconds after the start of the previous tween.
- `"label"`: Start at a specific label.

## Examples
```javascript
const tl = gsap.timeline({ repeat: -1, yoyo: true });

tl.to(".box1", { x: 100, duration: 1 })
  .to(".box2", { y: 100, duration: 1 }, "-=0.5") // Overlap
  .addLabel("rotate")
  .to(".box3", { rotation: 180, duration: 1 }, "rotate") // At label
```
