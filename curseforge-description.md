# Zeta Forge Event Remap Perf Fix

A small, narrow optimization for [Zeta](https://www.curseforge.com/minecraft/mc-mods/zeta)'s Forge-event compatibility layer (used by Quark and other VazkiiMods mods).

## Background

`ForgeEventsRemapper` wraps every NeoForge event in a Forge-style object for mods still using Zeta's old Forge-event API. The expensive part (finding the right wrapper class/constructor) is already cached once at mod-init - confirmed against Zeta's real bytecode, not guessed. What's left is `Constructor.newInstance(...)` running on every single event fire to build the wrapper object.

## What this does

Replaces that one `Constructor.newInstance` call with a cached `java.lang.invoke.MethodHandle` for the same constructor, skipping some of reflection's own per-call overhead.

**Expected gain is modest, not dramatic.** This isn't a missing-cache bug like the ScorchedGuns one - it's a smaller reflection-call swap.

## Requirements

- Minecraft 1.21.1, NeoForge
- Zeta 1.1-40
- **Client-side only.**

## Compatibility

One `@Redirect` mixin on one specific call. No other interactions with Zeta.
