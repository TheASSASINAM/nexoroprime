# NexoroPrime (Fabric, Minecraft 1.21.11)

Original client mod (no code or assets from any other client). Orange + blue theme.

- **Main screen**: custom title screen (Singleplayer, Multiplayer, Client Menu, Options, Quit)
- **Client menu**: press **Right Shift** (in game or on the main screen). Sidebar, module cards, favorites (star), per-module settings with sliders/toggles
- **FPS Boost**: lowers render/simulation distance, particles, clouds, shadows, biome blend, entity distance, graphics mode and removes the FPS limit. 3 levels. Your original settings are saved and restored when you switch it off
- **HUD**: FPS, Coordinates, CPS, Keystrokes, Clock (each can be moved to any screen corner)
- **Others**: Zoom (hold C), Fullbright, Toggle Sprint
- Settings are saved in `config/nexoroprime.json`

Requires: Fabric Loader, Fabric API, Java 21.

## Build without installing anything (GitHub)
1. Create a free GitHub account and a new empty repository.
2. Upload ALL files of this folder (including the `.github` folder) to it.
3. Open the **Actions** tab -> the `build` workflow runs by itself.
4. When it is green, open the run and download **NexoroPrime-jar** (it contains the `.jar`).
5. Put the jar in `.minecraft/mods` together with Fabric API.

## Build locally
Install JDK 21 and Gradle, then run `gradle build`. The jar is in `build/libs/`.

## If the build fails
- *"Could not find ... version"*: open https://fabricmc.net/develop , pick Minecraft 1.21.11 and copy the
  `loader_version`, `loom_version` and `fabric_version` numbers into `gradle.properties`.
- *"cannot find symbol" / "method ... cannot be applied"*: a Minecraft API name differs on your version.
  Copy the error text and fix that single line (the places most likely to differ are marked in
  `ui/Ui.java` -> `big(...)` and `NexoroPrime.java` -> HUD callback).
