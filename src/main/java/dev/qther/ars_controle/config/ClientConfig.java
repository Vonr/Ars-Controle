package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    ClientConfig(ModConfigSpec.Builder builder) {
    }

    public static final ClientConfig CLIENT;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = pair.getLeft();
        SPEC = pair.getRight();
    }
}
