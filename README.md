# Wind Blower 1.2.1 — Ultra Realistic Water compatibility — Minecraft Java 26.3 / Fabric

A source project for the Wind Blower mod with a 2D item icon, dedicated creative tab, hold-right-click wind force for movable entities, adjustable `/windblow` commands, and persistent settings.

## No JDK/Gradle installed?

Read [BUILD-ONLINE.md](BUILD-ONLINE.md). The included GitHub Actions workflow builds the project using JDK 25 and Gradle on a hosted runner, then makes the JAR available as a downloadable artifact. A ZIP-to-JAR converter cannot compile mod source code.

## Build locally (optional)

Requirements: JDK 25+, internet access on first build. Run `build.bat`. Only install `build/libs/windblower-26.3-1.2.1.jar` if Gradle reports `BUILD SUCCEEDED`.

## Notes

- This is the source, not a prebuilt JAR.
- Build with the real Minecraft 26.3 and Fabric dependencies. Do not patch class-file bytecode by hand.
- Wind affects movable entities. Normal Minecraft particles are visual effects, and stationary blocks do not move unless a supported physics mechanism turns them into entities.
- Optional Ultra Realistic Water 1.2.2 integration uses that mod's existing `WaterWorld.onExplosion(Vec3, float)` simulation hook as a small repeated gust when the aim ray hits its simulated water or realistic sand. This is a radial impulse positioned behind the hit point, not a native directional-force API. It does not require Claude sign-in, and Wind Blower has no compile-time dependency on the water mod.
- `/windblow set waterforce 0.45` controls the impulse radius (0.15–1.10); `/windblow set waterpulse 3` controls the use-tick interval (1–10). Larger pulse values mean less frequent pulses. The Realistic Water `tntReactions` setting must be enabled for its existing impulse hook to run.
- This project has not yet been compiled or launched in this environment. The online build workflow can reveal source/API compile errors; successful compilation still needs an in-game test in a disposable copy of your instance.
