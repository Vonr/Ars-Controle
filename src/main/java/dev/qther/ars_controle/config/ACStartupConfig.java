package dev.qther.ars_controle.config;

import com.hollingsworth.arsnouveau.common.block.tile.LecternInvWrapper;
import dev.qther.ars_controle.ArsControle;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class ACStartupConfig {
    public final ModConfigSpec.ConfigValue<List<? extends String>> SCRYERS_LINKAGE_BLACKLISTED_CAPABILITIES;

    private final List<String> DEFAULT_CLASS_BLACKLIST = List.of(
            LecternInvWrapper.class.getCanonicalName(),
            "appeng.api.networking.IInWorldGridNodeHost"
    );
    public final List<Class<?>> SCRYERS_LINKAGE_BLACKLISTED_CLASSES = new ArrayList<>();

    ACStartupConfig(ModConfigSpec.Builder builder) {
        var scryersLinkage = new ConfigHelper.CategoryBuilder(builder, "scryers_linkage");
        scryersLinkage.push("Config for Scryer's Linkage");
        SCRYERS_LINKAGE_BLACKLISTED_CAPABILITIES = scryersLinkage.makeStringList(
                "blacklisted_capabilities",
                DEFAULT_CLASS_BLACKLIST,
                LecternInvWrapper.class.getCanonicalName(),
                s -> {
                    try {
                        Class.forName(s);
                        return true;
                    } catch (ClassNotFoundException e) {
                        return false;
                    }
                },
                "List of class paths of blacklisted capabilities for Scryer's Linkage"
        );
        scryersLinkage.pop();
    }

    public static final ACStartupConfig STARTUP;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ACStartupConfig::new);
        STARTUP = pair.getLeft();
        SPEC = pair.getRight();
    }

    public static void onLoad(ModConfigEvent.Loading event) {
        if (!event.getConfig().getSpec().equals(SPEC)) {
            return;
        }

        STARTUP.SCRYERS_LINKAGE_BLACKLISTED_CLASSES.clear();
        for (String s : STARTUP.SCRYERS_LINKAGE_BLACKLISTED_CAPABILITIES.get()) {
            try {
                var clazz = Class.forName(s);
                STARTUP.SCRYERS_LINKAGE_BLACKLISTED_CLASSES.add(clazz);
            } catch (ClassNotFoundException e) {
                if (!STARTUP.DEFAULT_CLASS_BLACKLIST.contains(s)) {
                    ArsControle.LOGGER.warn("Could not find blacklisted capability class: {}", s);
                }
            }
        }
    }
}
