# Build Wind Blower online (no Java or Gradle installation on your PC)

This project is source code; a ZIP-to-JAR renamer/converter cannot compile it into an installable Minecraft mod. Use the included GitHub Actions workflow to build it on GitHub's hosted machine.

## Steps

1. Download and extract this ZIP on your computer. You do not need to install Java or Gradle just to extract it.
2. Open https://github.com/new and create a repository named `windblower-26.3-build`. For the simplest free build, a public repo can use GitHub-hosted runners at no charge; note that a public repository makes the source code visible to others. A private repository can also work within your account's Actions allowance.
3. In the new empty repository, choose **Add file → Upload files**.
4. Upload the *contents* of the extracted `WindBlower-26.3-Fabric-1.2.0` folder, including the `.github/workflows/build.yml` file and the `src`, `build.gradle`, `settings.gradle`, `gradle.properties`, and `LICENSE` entries. Do not upload only the ZIP itself.
5. Commit the upload to the default branch (`main`). The `Build Wind Blower JAR` workflow should start automatically. If it does not, open **Actions → Build Wind Blower JAR → Run workflow**.
6. Open the completed workflow run. At the bottom, under **Artifacts**, download `Wind-Blower-1.2.0-JAR`. The workflow is configured to offer the JAR directly, not wrapped in another ZIP.
7. Only use the downloaded JAR if the workflow shows a successful build. A successful compilation does not substitute for an in-game test; first test in a copied launcher instance/world.

## If the build fails

Open the workflow run, expand the **Build mod** step, and copy the first compiler/dependency error plus the last 30 lines. Do not install any artifact if the build failed.
