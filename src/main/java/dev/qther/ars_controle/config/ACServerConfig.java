package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ACServerConfig {
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_MAX_SOURCE_COST;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_MIN_DISTANCE;
    public final ModConfigSpec.ConfigValue<Double> WARPING_SPELL_PRISM_COST_PER_BLOCK;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_COST_DIMENSION;
    public final ModConfigSpec.ConfigValue<Integer> WARPING_SPELL_PRISM_LOAD_TIME;

    public final ModConfigSpec.ConfigValue<Integer> SCROLL_HOLDER_SOURCE_COST;

    public final ModConfigSpec.ConfigValue<Integer> SCRYERS_LINKAGE_LOAD_TIME;
    public final ModConfigSpec.ConfigValue<Boolean> WARPING_SPELL_PRISM_ALLOW_LINKING_OTHER_PLAYERS;

    ACServerConfig(ModConfigSpec.Builder builder) {
        var warpingSpellPrism = new ConfigHelper.CategoryBuilder(builder, "warping_spell_prism");
        warpingSpellPrism.push("Config for Warping Spell Prism");
        WARPING_SPELL_PRISM_MAX_SOURCE_COST = warpingSpellPrism.make("max_cost", -1, "Max Source cost of Warping Spell Prism (1 Source Jar = 10000 Source)");
        WARPING_SPELL_PRISM_COST_MIN_DISTANCE = warpingSpellPrism.make("cost_min_distance", 1024, "The minimum distance before Warping Spell Prism costs Source");
        WARPING_SPELL_PRISM_COST_PER_BLOCK = warpingSpellPrism.make("cost_per_block", 0.03125D, "Source Cost per block of Warping Spell Prism");
        WARPING_SPELL_PRISM_COST_DIMENSION = warpingSpellPrism.make("dimension_cost", 2000, "Source Cost when crossing dimensions of Warping Spell Prism");
        WARPING_SPELL_PRISM_LOAD_TIME = warpingSpellPrism.make("load_time", 600, "How long the Warping Spell Prism will load chunks (in ticks) after a teleport");
        WARPING_SPELL_PRISM_ALLOW_LINKING_OTHER_PLAYERS = warpingSpellPrism.bool("allow_linking_other_players", false, "Whether to allow linking Warping Spell Prisms to other players");
        warpingSpellPrism.pop();

        var scrollHolder = new ConfigHelper.CategoryBuilder(builder, "scroll_holder");
        scrollHolder.push("Config for Warp Scroll Holder");
        SCROLL_HOLDER_SOURCE_COST = scrollHolder.makeBounded("cost", 1000, 0, Integer.MAX_VALUE, Integer.class, "Cost in source for a Warp Scroll Holder to form a portal (1 Source Jar = 10000 Source)");
        scrollHolder.pop();

        var scryersLinkage = new ConfigHelper.CategoryBuilder(builder, "scryers_linkage");
        scryersLinkage.push("Config for Warp Scroll Holder");
        SCRYERS_LINKAGE_LOAD_TIME = scryersLinkage.make("load_time", 600, "How long the Scryer's Linkage will load chunks (in ticks) after its target is requested");
        scryersLinkage.pop();
    }

    public static final ACServerConfig SERVER;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ACServerConfig::new);
        SERVER = pair.getLeft();
        SPEC = pair.getRight();
    }
}
