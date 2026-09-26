# Cleave — NeoForge 26.1.2

A mod for Minecraft which allows to instantly mine ore veins

> **Modified version notice (GPLv3 §5a).**
> This branch is a **modified version** of [Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner)
> by Tourmi. It was ported from the mod's Forge 56.0.9 / Minecraft 1.21.6 sources (upstream commit
> `751bdad`) to **NeoForge, Minecraft 26.1.2**, on **2026-09-23**. It is **not an official release**
> and is not endorsed by the original author. Like the original, it is distributed under the
> GNU General Public License v3 — see [LICENSE](LICENSE), which is unchanged. The changes are listed
> under [NeoForge port notes](#neoforge-port-notes).

## Target versions

| | |
|---|---|
| Minecraft | 26.1.2 |
| Loader | NeoForge `26.1.2.68-beta` |
| Java | 25 |
| Build | ModDevGradle (`net.neoforged.moddev`) 2.0.141, Gradle 9.4.1 |

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

Requires **NeoForge 26.1.2.68-beta or newer** and **Java 25**, on Minecraft **26.1.2**.

Drop the jar in the server's `mods` folder. Clients do not need it.

## Configuration

Same format and same keys as the Forge version, but **NeoForge 26.1.2 keeps `SERVER`-type configs
somewhere else**, so the paths differ:

| | Forge 1.21.6 branch | This branch |
|---|---|---|
| Server settings | `<world>/serverconfig/cleave-config.toml` | `<instance>/config/cleave-config.toml` |
| Per-player settings | `<world>/serverconfig/cleave-player-configs/<uuid>.toml` | `<instance>/config/cleave-player-configs/<uuid>.toml` |

NeoForge's `ServerLifecycleHooks` now loads server configs from `<instance>/config/` and treats
`<world>/serverconfig/` purely as an *override* folder (NeoForge writes a `readme.txt` there saying
so). Dropping a copy of either file into `<world>/serverconfig/` still overrides the instance-wide
one for that world, so per-world configuration is still possible, it just is not the default
location any more. This is a platform change, not a choice made by this port.

One default differs: the vein-mining whitelist ships as `#c:ores` instead of `#forge:ores`, because
`forge:` tags do not exist on NeoForge — the convention namespace is `c:` (`data/c/tags/block/ores.json`
ships in the NeoForge jar).

## NeoForge port notes

Where the port had to diverge from the Forge branch:

| Concern | Forge 56 / 1.21.6 | This branch |
|---|---|---|
| Entrypoint | `@Mod` class with a no-arg constructor | `@Mod` class with `Cleave(IEventBus modEventBus, ModContainer modContainer)` |
| Listener registration | `BusGroup.DEFAULT.register(MethodHandles.lookup(), mod)` | `NeoForge.EVENT_BUS.register(mod)` — see [Which bus](#which-bus) below |
| Block break | `BlockEvent.BreakEvent`, listener **returns `boolean`** to cancel | `net.neoforged.neoforge.event.level.block.BreakBlockEvent` (the 26.1.2 home of Forge's `BlockEvent.BreakEvent`), cancelled with `event.setCanceled(true)` |
| Break event accessors | `event.getPlayer()/getState()/getPos()/getLevel()` | same, but `getLevel()` is a `LevelAccessor`, so `VeinMiner#onBlockBroken` now takes `(LevelAccessor, Player, BlockPos, BlockState)` and pattern-matches to `ServerLevel` / `ServerPlayer` |
| Item drop pickup | `EntityEvent.EntityConstructing` + `Entity#getServer()` | `EntityJoinLevelEvent` + `entity.level().getServer()` (`Entity#getServer()` no longer exists at 26.1.2) |
| Config spec | `net.minecraftforge.common.ForgeConfigSpec` | `net.neoforged.neoforge.common.ModConfigSpec` (identical builder shape) |
| Empty list default | `defineList("giant_vein_blacklist", List.of(), …)` | `defineListAllowEmpty(…)` — NeoForge's `defineList` rejects an empty default |
| Config registration | `ModLoadingContext.get().registerConfig(SERVER, spec, path)` | `modContainer.registerConfig(ModConfig.Type.SERVER, spec, path)`; the container is stashed in `ModUtils` because per-player configs are registered at login, not at construction |
| Per-player config load | `ConfigTracker.INSTANCE.loadConfigs(SERVER, <world>/serverconfig)` | `ConfigTracker.INSTANCE.loadConfigs(SERVER, FMLPaths.CONFIGDIR.get(), <world>/serverconfig)` — the 3-argument form NeoForge itself uses, so the world folder keeps working as an override |
| Block tags | `ForgeRegistries.BLOCKS.tags().createTagKey(ResourceLocation.parse(t))` | `TagKey.create(Registries.BLOCK, Identifier.parse(t))` — same as the Fabric branch |
| Mined-block experience | `Block#popExperience` with a hand-computed Fortune / Silk Touch amount (`EnchantUtils`) | `EnchantUtils` **deleted**. On NeoForge, `Block#playerDestroy` → `Block#dropResources` → `CommonHooks#handleBlockDrops` fires `BlockDropsEvent`, which pops `EnchantmentHelper.processBlockExperience(level, tool, state.getExpDrop(…))` itself. Popping it again would double the XP |
| Op permission check | `cs.hasPermission(Commands.LEVEL_GAMEMASTERS)` | `Commands.LEVEL_GAMEMASTERS.check(cs.permissions())` (a 1.21.11 vanilla change) |
| Identifiers | `net.minecraft.resources.ResourceLocation` | `net.minecraft.resources.Identifier` (a 26.x vanilla rename) |
| Metadata | `src/main/resources/META-INF/mods.toml`, expanded by `processResources` | `src/main/templates/META-INF/neoforge.mods.toml`, expanded by a `generateModMetadata` task wired into `sourceSets.main.resources` and `neoForge.ideSyncTask` |
| `pack.mcmeta` | present | **removed** — the mod ships no assets or data, and NeoForge does not require it |
| `displayTest="IGNORE_SERVER_VERSION"` | declared in `mods.toml` | **dropped** — NeoForge 26.1.2 has no `displayTest` concept at all (the string appears nowhere in the loader or in NeoForge). The server-side intent is carried by `side="SERVER"` on the `minecraft` dependency; a mod that registers no network payloads and no registry entries is not required on the client |
| Build | ForgeGradle 6, `mappings channel: 'official'`, Gradle 8.12, Java 21 | ModDevGradle 2.0.141, no mappings block (26.x ships unobfuscated), Gradle 9.4.1, Java 25 |

Nothing else in the mod's logic changed: the vein-mining, giant-vein, tunneling and item-teleport
code is untouched.

### Which bus

NeoForge has two event buses, and putting a listener on the wrong one fails silently — the listener
simply never fires, with no error. **Every** listener in this mod is on `NeoForge.EVENT_BUS`:

| Listener | Event | Bus |
|---|---|---|
| `onServerStarting` | `ServerStartingEvent` | `NeoForge.EVENT_BUS` |
| `onRegisterCommands` | `RegisterCommandsEvent` | `NeoForge.EVENT_BUS` |
| `onBlockBroken` | `BreakBlockEvent` | `NeoForge.EVENT_BUS` |
| `onPlayerMine` | `PlayerInteractEvent.LeftClickBlock` | `NeoForge.EVENT_BUS` |
| `onPlayerLogin` / `onPlayerLogout` | `PlayerEvent.PlayerLoggedIn/OutEvent` | `NeoForge.EVENT_BUS` |
| `onItemSpawn` | `EntityJoinLevelEvent` | `NeoForge.EVENT_BUS` |

The mod bus passed to the `@Mod` constructor is unused: it only carries the mod-loading lifecycle
(`DeferredRegister`, `ModConfigEvent`, the `FMLCommonSetupEvent` family), and this mod registers
nothing and takes no part in that lifecycle. The mod's config is registered directly through
`ModContainer#registerConfig`, which needs no listener.

## Building

```bash
./gradlew build      # jar lands in build/libs/
./gradlew runServer  # dev server, working directory ./run
```
