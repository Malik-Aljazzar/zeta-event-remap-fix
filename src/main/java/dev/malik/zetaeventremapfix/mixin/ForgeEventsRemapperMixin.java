package dev.malik.zetaeventremapfix.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.violetmoon.zetaimplforge.event.ForgeEventsRemapper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Narrow reflection-call optimization for Zeta's Forge-event compatibility layer - see
 * specification/SPEC.md for the verified root cause and why this is NOT a missing-cache bug like
 * the ScorchedGuns one.
 *
 * {@code ForgeEventsRemapper} resolves which wrapper class/constructor to use for a given event
 * type exactly once, at mod-init registration time ({@code findForgeWrapper}, cached in
 * {@code zetaToForgeMap}) - confirmed via javap, not guessed. The only thing that still runs on
 * every single event firing is {@code lambda$findForgeWrapper$4}'s
 * {@code Constructor.newInstance(new Object[]{event})} call - the actual construction of a fresh
 * wrapper instance, which is architecturally necessary per event (each event needs its own
 * wrapper object; the object itself can't be cached/reused across different events).
 *
 * This redirects just that one {@code Constructor.newInstance} call to a cached
 * {@link MethodHandle} for the same constructor instead of classic reflection. A MethodHandle,
 * once resolved, skips some of {@code Constructor.newInstance}'s own per-call overhead (an access
 * check and {@code InvocationTargetException} wrapping that classic reflection does every time).
 * The constructor set here is small and fixed (one per Zeta event type that has a registered
 * Forge-style wrapper), so the cache is tiny and the lookup is a one-time cost per constructor,
 * same shape as the existing {@code zetaToForgeMap} cache this mirrors.
 */
@Mixin(ForgeEventsRemapper.class)
public abstract class ForgeEventsRemapperMixin {

    private static final Map<Constructor<?>, MethodHandle> dev$handleCache = new ConcurrentHashMap<>();

    @Redirect(
            method = "lambda$findForgeWrapper$4(Ljava/lang/reflect/Constructor;Ljava/lang/Class;Lnet/neoforged/bus/api/Event;)Ljava/lang/Object;",
            at = @At(value = "INVOKE", target = "Ljava/lang/reflect/Constructor;newInstance([Ljava/lang/Object;)Ljava/lang/Object;"),
            remap = false
    )
    private static Object dev$fastNewInstance(Constructor<?> ctor, Object[] args) throws Throwable {
        MethodHandle handle = dev$handleCache.get(ctor);
        if (handle == null) {
            ctor.setAccessible(true);
            handle = MethodHandles.lookup().unreflectConstructor(ctor);
            dev$handleCache.put(ctor, handle);
        }
        return handle.invokeWithArguments(args);
    }
}
