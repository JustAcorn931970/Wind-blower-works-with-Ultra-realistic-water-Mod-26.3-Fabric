# Wind Blower 1.2.0 — Minecraft Java 26.3 / Fabric

A source project for the Wind Blower mod with a 2D item icon, dedicated creative tab, hold-right-click wind force for movable entities, adjustable `/windblow` commands, and persistent settings.

## No JDK/Gradle installed?

Read [BUILD-ONLINE.md](BUILD-ONLINE.md). The included GitHub Actions workflow builds the project using JDK 25 and Gradle on a hosted runner, then makes the JAR available as a downloadable artifact. A ZIP-to-JAR converter cannot compile mod source code.

## Build locally (optional)

Requirements: JDK 25+, internet access on first build. Run `build.bat`. Only install `build/libs/windblower-26.3-1.2.0.jar` if Gradle reports `BUILD SUCCEEDED`.

## Notes

- This is the source, not a prebuilt JAR.
- Build with the real Minecraft 26.3 and Fabric dependencies. Do not patch class-file bytecode by hand.
- Wind affects movable entities. Normal particles are visual effects, and stationary blocks do not move unless a supported physics mechanism turns them into entities.
- This project has not yet been compiled or launched in this environment. The online build workflow can reveal source/API compile errors; successful compilation still needs an in-game test in a disposable copy of your instance.
