package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public final ModConfigSpec.BooleanValue AUTOFOCUS_SEARCH;
    public final ModConfigSpec.BooleanValue CLEAR_SEARCH_ON_AUTOFOCUS;
    public final ModConfigSpec.BooleanValue ESCAPE_TO_CLEAR_SEARCH;

    public final ModConfigSpec.BooleanValue SWAP_GLYPHS_WITH_NUMBER_KEYS;

    ClientConfig(ModConfigSpec.Builder builder) {
        var improvedControls = new ConfigHelper.CategoryBuilder(builder, "enhanced_spellbook_controls");
        improvedControls.push("Config for Enhanced Spell Book controls");

        var searchBar = improvedControls.child("search_bar");
        searchBar.push("Search bar tweaks");
        AUTOFOCUS_SEARCH = searchBar.bool("enable_auto_focus", false, "Enable auto-focus");
        CLEAR_SEARCH_ON_AUTOFOCUS = searchBar.bool("clear_on_auto_focus", true, "Clear search on auto-focus");
        ESCAPE_TO_CLEAR_SEARCH = searchBar.bool("escape_to_clear", false, "Escape to clear search");
        searchBar.pop();

        var spellCrafting = improvedControls.child("spell_crafting");
        spellCrafting.push("Spell crafting tweaks");
        SWAP_GLYPHS_WITH_NUMBER_KEYS = spellCrafting.bool("swap_glyphs_with_number_keys", false, "Swap glyphs with number keys");

        improvedControls.pop();
    }

    public static final ClientConfig CLIENT;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = pair.getLeft();
        SPEC = pair.getRight();
    }
}
