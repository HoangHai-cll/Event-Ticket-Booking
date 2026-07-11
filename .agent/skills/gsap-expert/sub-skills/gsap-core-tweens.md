# GSAP Core & Tweens

Master the fundamental building blocks of GSAP.

## Key Concepts
- `gsap.to()`: Animate from current state to a target state.
- `gsap.from()`: Animate from a starting state to current state.
- `gsap.fromTo()`: Explicitly define both start and end states.
- `gsap.set()`: Immediately set properties (zero-duration tween).

## Best Practices
- Use `x` and `y` instead of `left` and `top` for hardware-accelerated transforms.
- Leverage `scale`, `rotation`, and `skew` for efficient GPU rendering.
- Always specify a duration (default is 0.5s).
- Use `ease` to control the feel of the animation (e.g., `power2.inOut`).

## Examples
```javascript
// A simple tween
gsap.to(".box", { 
  x: 200, 
  rotation: 360, 
  duration: 1, 
  ease: "back.out(1.7)" 
});
```
