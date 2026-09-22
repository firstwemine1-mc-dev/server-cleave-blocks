# Server-side Vein Miner — Fabric 26.3

A mod for Minecraft which allows to instantly mine ore veins

> **Modified version notice (GPLv3 §5a).**
> This branch is a modified version of [Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner)
> by Tourmi. It was ported from the Forge 1.21.6 sources to **Fabric, Minecraft 1.21.11**, then
> bumped to **Fabric, Minecraft 26.1.2** (both on **2026-09-22**), and then bumped again to
> **Fabric, Minecraft 26.2** and on to **Fabric, Minecraft 26.3** on **2026-09-23**.
> Like the original, it is distributed under the GNU General Public License v3 —
> see [LICENSE](LICENSE). The changes are listed under [Fabric port notes](#fabric-port-notes).

## Features so far
- When mining an ore block (the allowed blocks are configurable), all of its identical neighbors are also mined, recursively until there are no longer any blocks
- Configurable maximum amount of blocks to destroy at once
- Option to stop the vein mining if the tool is about to break
- The appropriate tool must be equipped for vein mining to trigger
- Vein mining applies all the enchantments of a tool, so Fortune, Silk Touch and Unbreaking all work properly
- Giant veins may be mined all at once, if an appropriate ore block is mined next to other giant vein blocks (ex: tuff, raw ore blocks, etc)

## Installing

Requires **Fabric Loader 0.19.5+**, **Fabric API**, and **Java 25**, on Minecraft **26.3**.

Drop the jar in the server's `mods` folder. Clients do not need it. The mod also runs on a client
(single player, open to LAN), which is why it declares `"environment": "*"` rather than `"server"` —
a `server` mod would not be loaded by the client at all.

## Configuration

Same files, same format and same keys as the Forge version, so an existing config carries over:

- `<world>/serverconfig/svmm-config.toml` — server settings
- `<world>/serverconfig/svmm-player-configs/<uuid>.toml` — per-player settings

One default differs : the vein-mining whitelist ships as `#c:ores` instead of `#forge:ores`, since
`c:` is the convention tag namespace on Fabric.

## Fabric port notes

Where the port had to diverge from the Forge branch:

| Concern | Forge | This branch |
|---|---|---|
| Entrypoint | `@Mod` class with `@SubscribeEvent` methods | `ModInitializer` registering Fabric API events |
| Block break | `BlockEvent.BreakEvent` (cancel to take over) | `PlayerBlockBreakEvents.BEFORE` (return `false` to take over) |
| Mined face, for tunneling | `PlayerInteractEvent.LeftClickBlock` | `AttackBlockCallback` |
| Login / logout | `PlayerEvent.PlayerLoggedIn/OutEvent` | `ServerPlayConnectionEvents.JOIN` / `DISCONNECT` |
| Item drop pickup | `EntityEvent.EntityConstructing` | `ServerEntityEvents.ENTITY_LOAD` |
| Commands | `RegisterCommandsEvent` | `CommandRegistrationCallback` |
| Config | `ForgeConfigSpec` + `ModConfig.Type.SERVER` | `dev.tourmi.svmm.config.spec.ConfigSpec`, a small NightConfig-backed reimplementation, bound to the world's `serverconfig` directory on server start |
| Block tags | `ForgeRegistries.BLOCKS.tags().createTagKey(...)` | `TagKey.create(Registries.BLOCK, ...)` |
| Mined-block experience | `Block#popExperience` with a hand-computed Fortune / Silk Touch amount (`EnchantUtils`) | vanilla `playerDestroy` → `dropResources` → `spawnAfterBreak` already drops it, so `EnchantUtils` was removed. Forge suppresses that vanilla path, which is why the Forge branch needs its own |

NightConfig is bundled into the jar (jar-in-jar), because Fabric — unlike Forge — does not ship it.

Minecraft 1.21.11 also renamed `ResourceLocation` to `Identifier` and replaced
`CommandSourceStack#hasPermission(int)` with `PermissionCheck#check(PermissionSet)`; both are version
changes rather than loader ones.

### 1.21.11 -> 26.1.2

This bump was build-side only — **no Java source file changed**. Minecraft 26.x ships
unobfuscated, so:

| Concern | 1.21.11 | 26.1.2 |
|---|---|---|
| Loom plugin | `net.fabricmc.fabric-loom-remap` | `net.fabricmc.fabric-loom` (same `1.16-SNAPSHOT`) |
| Mappings | `mappings loom.officialMojangMappings()` | no `mappings` line at all — the shipped names *are* the Mojang names |
| Mod dependencies | `modImplementation` (remapped) | plain `implementation`, since there is nothing left to remap |
| Java | 21 | 25 (`java_version` in `gradle.properties`, which also drives `fabric.mod.json` and the CI JDK) |
| Fabric API | `0.141.4+1.21.11` | `0.149.0+26.1.2` |

The 26.1.2 vanilla renames that usually bite a port — `ChunkPos` becoming a record with `x()`/`z()`
accessors, `ChunkPos.asLong` becoming `ChunkPos.pack`, the `RenderType` / `RenderTypes` move,
`LightTexture.pack` becoming `LightCoordsUtil.pack` — do not touch this mod: it is server-side only
and uses none of those APIs. Fabric Loader stayed at `0.19.2` and the jar-in-jar'd NightConfig
stayed at `3.8.1`.

### 26.1.2 -> 26.2

Build-side only again — **no Java source file changed**, and no `fabric.mod.json` change beyond the
templated versions.

| Concern | 26.1.2 | 26.2 |
|---|---|---|
| `minecraft_version` | 26.1.2 | 26.2 |
| Fabric Loader | 0.19.2 | 0.19.3 |
| Fabric API | 0.149.0+26.1.2 | 0.153.0+26.2 |
| Loom plugin | `net.fabricmc.fabric-loom` 1.16-SNAPSHOT | unchanged |
| Gradle wrapper | 9.4.1 | unchanged |
| Java | 25 | unchanged |
| NightConfig | 3.8.1 | unchanged |

The headline 26.2 change upstream is the move from immediate-mode `Tesselator` / `MeshData` to
`StagedVertexBuffer`. That is a client render-pipeline change and this mod has no client code, no
rendering and no mixins, so none of it applies. The Fabric API event classes the mod subscribes to
(`PlayerBlockBreakEvents.BEFORE`, `AttackBlockCallback`, `ServerPlayConnectionEvents`,
`ServerEntityEvents.ENTITY_LOAD`, `CommandRegistrationCallback`, `ServerLifecycleEvents`) all kept
their packages and signatures, and the vanilla types the mod touches (`BlockPos`, `Level` /
`ServerLevel`, `ItemStack`, `Player` / `ServerPlayer`, `TagKey`, `CommandSourceStack`) are unchanged.

### 26.2 -> 26.3

Build-side only once more — **no Java source file changed**. Unlike the 26.1.2 -> 26.2 hop, this one
also moves the build toolchain: the Loom plugin and the Gradle wrapper both step up.

| Concern | 26.2 | 26.3 |
|---|---|---|
| `minecraft_version` | 26.2 | 26.3 |
| Fabric Loader | 0.19.3 | 0.19.5 |
| Fabric API | 0.153.0+26.2 | 0.160.5+26.3 |
| Loom plugin | `net.fabricmc.fabric-loom` 1.16-SNAPSHOT | `net.fabricmc.fabric-loom` **1.17-SNAPSHOT** |
| Gradle wrapper | 9.4.1 | **9.6.0** |
| Java | 25 | unchanged |
| NightConfig | 3.8.1 | unchanged |

26.3's own headline change upstream is the render submission model again — `StagedVertexBuffer`
being replaced by the submit/collect model (`LevelRenderEvents.COLLECT_SUBMITS`,
`SubmitNodeCollector`). As with 26.2, that is all client rendering and this mod has none. The two
non-render 26.3 renames worth checking, `Axis` rotation helpers and `new BlockPos(pos)` becoming
`pos.immutable()`, do not occur in this source tree: the mod only ever constructs `BlockPos` from
coordinates or calls `relative`/`offset`, never from a mutable copy. All six Fabric API events the
mod subscribes to still resolve at their existing packages.

## Building

```bash
./gradlew build      # jar lands in build/libs/
./gradlew runServer  # dev server
```
