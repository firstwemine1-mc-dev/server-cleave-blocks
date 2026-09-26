# Cleave — Fabric 1.21.11

A mod for Minecraft which allows to instantly mine ore veins

> **Modified version notice (GPLv3 §5a).**
> This branch is a modified version of [Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner)
> by Tourmi. It was ported from the Forge 1.21.6 sources to **Fabric, Minecraft 1.21.11** on
> **2026-09-22**. Like the original, it is distributed under the GNU General Public License v3 —
> see [LICENSE](LICENSE). The changes are listed under [Fabric port notes](#fabric-port-notes).

## Purpose of this fork

[Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner) by Tourmi is
published for **Forge only**. This fork exists to make the same mod available on the loaders and
Minecraft versions that are not covered there — Fabric and NeoForge. That is its only purpose.

It is not a rewrite and not a feature fork: behaviour, config keys and command syntax are kept as
close to the original as each loader allows, so a config from the original carries over. If you
are playing on Forge, use Tourmi's own releases — that is the supported build.

### Name

This fork is called **Cleave**. It was `svmm` while it was only a vein miner, and felling whole
trees made that name wrong. The command root is `/cleave`; the old `/svmm` root is still
registered as an alias, so existing command blocks and scripts keep working. The mod id, the
config file names and the config root section moved with it.

## Features so far
- When mining an ore block (the allowed blocks are configurable), all of its identical neighbors are also mined, recursively until there are no longer any blocks
- Configurable maximum amount of blocks to destroy at once
- Option to stop the vein mining if the tool is about to break
- The appropriate tool must be equipped for vein mining to trigger
- Vein mining applies all the enchantments of a tool, so Fortune, Silk Touch and Unbreaking all work properly
- Giant veins may be mined all at once, if an appropriate ore block is mined next to other giant vein blocks (ex: tuff, raw ore blocks, etc)
- Whole trees are felled when a log is broken with an axe, leaves included, on a block budget of its own
- Configurable durability multiplier, so breaking in bulk can be made to cost more than mining by hand
- Tunnelling, and a forced vein mine that ignores the ore whitelist for one block

## Installing

Requires **Fabric Loader 0.19.2+**, **Fabric API**, and **Java 21**, on Minecraft **1.21.11**.

Drop the jar in the server's `mods` folder. Clients do not need it. The mod also runs on a client
(single player, open to LAN), which is why it declares `"environment": "*"` rather than `"server"` —
a `server` mod would not be loaded by the client at all.

## Configuration

Same format and the same keys as the Forge version, but the file names follow the mod id, so a
config written by an `svmm` build is not picked up by a `cleave` build:

- `<world>/serverconfig/cleave-config.toml` — server settings
- `<world>/serverconfig/cleave-player-configs/<uuid>.toml` — per-player settings

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
| Config | `ForgeConfigSpec` + `ModConfig.Type.SERVER` | `io.github.firstwemine1.cleave.config.spec.ConfigSpec`, a small NightConfig-backed reimplementation, bound to the world's `serverconfig` directory on server start |
| Block tags | `ForgeRegistries.BLOCKS.tags().createTagKey(...)` | `TagKey.create(Registries.BLOCK, ...)` |
| Mined-block experience | `Block#popExperience` with a hand-computed Fortune / Silk Touch amount (`EnchantUtils`) | vanilla `playerDestroy` → `dropResources` → `spawnAfterBreak` already drops it, so `EnchantUtils` was removed. Forge suppresses that vanilla path, which is why the Forge branch needs its own |

NightConfig is bundled into the jar (jar-in-jar), because Fabric — unlike Forge — does not ship it.

Minecraft 1.21.11 also renamed `ResourceLocation` to `Identifier` and replaced
`CommandSourceStack#hasPermission(int)` with `PermissionCheck#check(PermissionSet)`; both are version
changes rather than loader ones.

## Building

```bash
./gradlew build      # jar lands in build/libs/
./gradlew runServer  # dev server
```
