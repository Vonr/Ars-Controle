package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ServerConfig {
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_MAX_SOURCE_COST;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_MIN_DISTANCE;
    public final ModConfigSpec.ConfigValue<Double> WARPING_SPELL_PRISM_COST_PER_BLOCK;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_DIMENSION;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_LOAD_TIME;

    ServerConfig(ModConfigSpec.Builder builder) {
        var warpingSpellPrism = new ConfigHelper.CategoryBuilder(builder, "warping_spell_prism");
        warpingSpellPrism.push("Config for Warping Spell Prism");
        WARPING_SPELL_PRISM_MAX_SOURCE_COST = warpingSpellPrism.make("max_cost", -1, "Max Source cost of Warping Spell Prism (1 Source Jar = 10000 Source)");
        WARPING_SPELL_PRISM_COST_MIN_DISTANCE = warpingSpellPrism.make("cost_min_distance", 1024, "The minimum distance before Warping Spell Prism costs Source");
        WARPING_SPELL_PRISM_COST_PER_BLOCK = warpingSpellPrism.make("cost_per_block", 0.03125D, "Source Cost per block of Warping Spell Prism");
        WARPING_SPELL_PRISM_COST_DIMENSION = warpingSpellPrism.make("dimension_cost", 2000, "Source Cost when crossing dimensions of Warping Spell Prism");
        WARPING_SPELL_PRISM_LOAD_TIME = warpingSpellPrism.make("load_time", 600, "How long the Warping Spell Prism will load chunks after a teleport in ticks");
        warpingSpellPrism.pop();
    }

    public static final ServerConfig SERVER;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = pair.getLeft();
        SPEC = pair.getRight();
    }
}
