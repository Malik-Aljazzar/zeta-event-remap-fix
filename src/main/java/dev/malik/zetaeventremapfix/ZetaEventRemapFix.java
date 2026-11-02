package dev.malik.zetaeventremapfix;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ZetaEventRemapFix.MODID)
public class ZetaEventRemapFix {
    public static final String MODID = "zeta_event_remap_fix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZetaEventRemapFix(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Zeta Forge Event Remap Perf Fix loaded (caches MethodHandles for ForgeEventsRemapper's wrapper constructors)");
    }
}
