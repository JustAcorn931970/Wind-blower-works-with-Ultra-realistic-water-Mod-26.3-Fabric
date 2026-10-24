# Ultra Realistic Water 1.2.2 compatibility

Wind Blower does not need Claude sign-in or a local copy of the water mod's source. This optional bridge uses reflection against the public `me.skaffy.client.water.WaterWorld` API so the project can still build and run without Ultra Realistic Water installed.

## What it does

- On client use ticks, it raycasts forward including fluids.
- If the hit fluid is reported as realistic by `WaterWorld.isRealistic(FluidState)`, it queues a small impulse using `WaterWorld.get().onExplosion(Vec3, float)`.
- It also recognizes a realistic sand/lava/slime block by class name as a best-effort target.
- The impulse center is placed slightly behind the hit point; repeated pulses approximate a forward gust.
- `waterforce` controls impulse radius, and `waterpulse` sets the interval in item-use ticks.

## Limitations

This uses the water mod's existing radial explosion-reaction hook. It is not a true directional wind-force API and can disturb simulated material around the hit point rather than pushing every particle in a perfectly straight cone. The water mod's `tntReactions` setting must be enabled because its hook intentionally checks that setting. It applies only when the player is aiming at simulated material; regular items/entities continue using Wind Blower's normal server-side force code.

The integration is optional and uses reflection to avoid a compile dependency on Ultra Realistic Water. It must still be compiled with the real Minecraft 26.3 / Fabric dependencies and tested in a copied instance.
