# Changelog

All notable changes to Zeta Forge Event Remap Perf Fix are documented in this file.

## [1.0.0]

### Fixes
- Narrow reflection-call optimization for Zeta's Forge-event compatibility layer (`org.violetmoon.zetaimplforge.event.ForgeEventsRemapper`, used by Quark and other VazkiiMods mods in this pack). Confirmed via spark profiler (present in every Render-thread capture taken so far, consistently, independent of location - base, field, and the full multi-thread capture all showed it) and verified against the real deployed `Zeta-1.1-40.jar` bytecode via `javap`: unlike the ScorchedGuns case, this is **not** a missing-cache bug - `findForgeWrapper` (the expensive class/constructor resolution) already runs exactly once per event type, at mod-init registration, cached in `zetaToForgeMap`. What still runs on every single event firing is the registered wrapper's `lambda$findForgeWrapper$4`, which calls `Constructor.newInstance(new Object[]{event})` - constructing a fresh wrapper object, which is architecturally necessary per event (the object itself can't be reused across different events). This mod redirects that one `Constructor.newInstance` call to a cached `java.lang.invoke.MethodHandle` for the same constructor instead, avoiding some of classic reflection's own per-call overhead (access check, `InvocationTargetException` wrapping). Built on explicit request after flagging that the expected gain here is modest, not dramatic - this is a smaller, lower-risk optimization than a "missing cache" fix, not a guaranteed large FPS win.
