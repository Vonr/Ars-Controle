package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ACClientConfig {

    ACClientConfig(ModConfigSpec.Builder builder) {
    }

    public static final ACClientConfig CLIENT;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ACClientConfig::new);
        CLIENT = pair.getLeft();
        SPEC = pair.getRight();
    }
}
