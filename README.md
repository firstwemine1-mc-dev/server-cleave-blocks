# Server-side Vein Miner
A mod for Minecraft which allows to instantly mine ore veins

## Purpose of this fork

This repository is a fork of [Server-side Vein Miner](https://github.com/Tourmi/mc-server-side-vein-miner)
by Tourmi. This `main` branch is the original work, unmodified apart from this section.

Tourmi publishes the mod for **Forge only**. This fork exists to make it available on the loaders
and Minecraft versions that are not covered there — Fabric and NeoForge. Those ports live on the
`fabric/*` and `neoforge/*` branches, each carrying its own modified-version notice. If you are
playing on Forge, use Tourmi's own releases — that is the supported build.

## Features so far
- When mining an ore block (the allowed blocks are configurable), all of its identical neighbors are also mined, recursively until there are no longer any blocks
- Configurable maximum amount of blocks to destroy at once
- Option to stop the vein mining if the tool is about to break
- The appropriate tool must be equipped for vein mining to trigger
- Vein mining applies all the enchantments of a tool, so Fortune, Silk Touch and Unbreaking all work properly
- Giant veins may be mined all at once, if an appropriate ore block is mined next to other giant vein blocks (ex: tuff, raw ore blocks, etc)
