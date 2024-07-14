package dev.qther.ars_controle;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public final ForgeConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_MAX_SOURCE_COST;
    public final ForgeConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_MIN_DISTANCE;
    public final ForgeConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_PER_BLOCK;
    public final ForgeConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_DIMENSION;

    ServerConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Config for Warping Spell Prism").push("warping_spell_prism");
        WARPING_SPELL_PRISM_MAX_SOURCE_COST = builder.comment("Max Source cost of Warping Spell Prism (1 Source Jar = 10000 Source)").define("max_cost", 5000);
        WARPING_SPELL_PRISM_COST_MIN_DISTANCE = builder.comment("The minimum distance before Warping Spell Prism costs Source").define("cost_min_distance", 128);
        WARPING_SPELL_PRISM_COST_PER_BLOCK = builder.comment("Source Cost per block of Warping Spell Prism").define("cost_per_block", 1);
        WARPING_SPELL_PRISM_COST_DIMENSION = builder.comment("Source Cost when crossing dimensions of Warping Spell Prism").define("dimension_cost", 1000);
        builder.pop();
    }

    public static final ServerConfig SERVER;
    public static final ForgeConfigSpec SPEC;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = pair.getLeft();
        SPEC = pair.getRight();
    }
}
