# Server-side Vein Miner

## Modified version notice (GPLv3 section 5a)

**This is a modified version of Tourmi's [Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner).**
It was **backported to Minecraft 1.20.1 / Forge 47.3.0** on **2026-09-22**, from the upstream
Minecraft 1.21.6 / Forge 56.0.9 sources (upstream commit `751bdad`).

It is **not** an official Tourmi release. Please report problems with this backport to whoever
distributed it, not to the upstream issue tracker.

This modified version remains licensed under the **GNU General Public License v3.0**, the same
license as the original; see [LICENSE](LICENSE).

### What this build targets

| | |
|---|---|
| Minecraft | 1.20.1 (`[1.20.1,1.21)`) |
| Mod loader | Forge 47.3.0 (`[47,)`) |
| Java | 17 |
| Side | Server-side only (clients do not need the mod) |

### What changed relative to upstream

- Build moved to ForgeGradle 6.0.16, Gradle 8.6, Java 17, with reobfuscation re-enabled
  (Forge 47 runs against obfuscated Minecraft, unlike Forge 56).
- `pack.mcmeta` uses the 1.20.1 pack formats, and `mods.toml` the 1.20.1 version ranges.
- Event handling uses the Forge 47 event bus (`MinecraftForge.EVENT_BUS`,
  `net.minecraftforge.eventbus.api.SubscribeEvent`, `event.setCanceled(true)`) instead of the
  Forge 56 bus-group API.
- Enchantment lookups use the pre-1.20.5 plain-object enchantment API.
- No gameplay, command, or configuration behaviour was intentionally changed. The server config is
  still written to `<world>/serverconfig/svmm-config.toml`, with per-player configs under
  `<world>/serverconfig/svmm-player-configs/`.

A mod for Minecraft which allows to instantly mine ore veins

## Purpose of this fork

[Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner) by Tourmi is
published for **Forge only**. This fork exists to make the same mod available on the loaders and
Minecraft versions that are not covered there — Fabric and NeoForge. That is its only purpose.

It is not a rewrite and not a feature fork: behaviour, config keys and command syntax are kept as
close to the original as each loader allows, so a config from the original carries over. If you
are playing on Forge, use Tourmi's own releases — that is the supported build.

## Features so far
- When mining an ore block (the allowed blocks are configurable), all of its identical neighbors are also mined, recursively until there are no longer any blocks
- Configurable maximum amount of blocks to destroy at once
- Option to stop the vein mining if the tool is about to break
- The appropriate tool must be equipped for vein mining to trigger
- Vein mining applies all the enchantments of a tool, so Fortune, Silk Touch and Unbreaking all work properly
- Giant veins may be mined all at once, if an appropriate ore block is mined next to other giant vein blocks (ex: tuff, raw ore blocks, etc)
