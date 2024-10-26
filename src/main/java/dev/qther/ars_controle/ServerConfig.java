package dev.qther.ars_controle;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_MAX_SOURCE_COST;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_MIN_DISTANCE;
    public final ModConfigSpec.ConfigValue<Double> WARPING_SPELL_PRISM_COST_PER_BLOCK;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_DIMENSION;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_LOAD_TIME;

    ServerConfig(ModConfigSpec.Builder builder) {
        builder.comment("Config for Warping Spell Prism").push("warping_spell_prism");
        WARPING_SPELL_PRISM_MAX_SOURCE_COST = builder.comment("Max Source cost of Warping Spell Prism (1 Source Jar = 10000 Source)").define("max_cost", -1);
        WARPING_SPELL_PRISM_COST_MIN_DISTANCE = builder.comment("The minimum distance before Warping Spell Prism costs Source").define("cost_min_distance", 1024);
        WARPING_SPELL_PRISM_COST_PER_BLOCK = builder.comment("Source Cost per block of Warping Spell Prism").define("cost_per_block", 0.03125D);
        WARPING_SPELL_PRISM_COST_DIMENSION = builder.comment("Source Cost when crossing dimensions of Warping Spell Prism").define("dimension_cost", 2000);
        WARPING_SPELL_PRISM_LOAD_TIME = builder.comment("How long the Warping Spell Prism will load chunks after a teleport in ticks").define("load_time", 600);
        builder.pop();
    }

    public static final ServerConfig SERVER;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = pair.getLeft();
        SPEC = pair.getRight();
    }
}
