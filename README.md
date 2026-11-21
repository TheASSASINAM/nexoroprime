# NexoroPrime (Fabric, Minecraft 1.21.11)

Original client mod (no code or assets from any other client). Orange + blue theme.

- **Main screen**: custom title screen with animated snow (Singleplayer, Multiplayer, Client Menu, Options, Quit)
- **Client menu** (Right Shift): module cards with name pills, filter chips (ALL / FAVORITE / HUD / VISUAL / PERFORMANCE / UTILITY), search box, star favorites, per-module settings (right-click a card or click "...")
- **HUD editor**: drag every HUD widget anywhere ("Edit HUD" in the menu)
- **HUD**: FPS, Coordinates, Direction, Speed, Ping, CPS, Keystrokes, Clock, Armor HUD, Item Counter
- **FPS Boost**: render/simulation distance caps, entity distance, minimal particles, clouds/shadows/biome blend off, no view bob, unlock max FPS, VSync off. Each part switchable. Original settings are restored when you turn it off
- **Visual**: Zoom (hold C), Fullbright, No View Bob, No FOV Effects, No Nausea Wobble, No Particles
- **Utility**: Auto Sprint, Auto Respawn
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
