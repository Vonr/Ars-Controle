package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import dev.qther.ars_controle.ArsControle;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ACLangDatagen extends LanguageProvider {
    private final Map<String, String> data = new TreeMap<>();

    public ACLangDatagen(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    @Override
    protected void addTranslations() {
        for (var spellPart : GlyphRegistry.getSpellpartMap().values()) {
            var registryName = spellPart.getRegistryName();
            if (registryName.getNamespace().equals(ArsControle.MODID)) {
                add("ars_controle.glyph_desc." + registryName.getPath(), spellPart.getBookDescription());
                add("ars_controle.glyph_name." + registryName.getPath(), spellPart.getName());

                Map<AbstractAugment, String> augmentDescriptions = new HashMap<>();
                spellPart.addAugmentDescriptions(augmentDescriptions);

                for (AbstractAugment augment : augmentDescriptions.keySet()) {
                    add("ars_nouveau.augment_desc." + registryName.getPath() + "_" + augment.getRegistryName().getPath(), augmentDescriptions.get(augment));
                }
            }
        }

        add("block.ars_controle.warping_spell_prism", "Warping Spell Prism");
        add("block.ars_controle.scryers_linkage", "Scryer's Linkage");
        add("block.ars_controle.temporal_stability_sensor", "Temporal Stability Sensor");
        add("block.ars_controle.scroll_holder", "Warp Scroll Holder");

        add("item.ars_controle.remote", "Remote");
        add("item.ars_controle.portable_brazier_relay", "Portable Brazier Relay");
        add("item.ars_controle.portable_brazier_relay.with_ritual", "Portable Brazier Relay (%s)");
        add("item.ars_controle.remote.with_target", "Remote (%s)");

        add("ars_controle.target.set.none", "Cleared target.");
        add("ars_controle.target.set.self", "Set target to yourself.");
        add("ars_controle.target.set.entity", "Set target to %s in %s.");
        add("ars_controle.target.set.block", "Set target to block at %s in %s.");
        add("ars_controle.target.set.fail.other_player", "Linking other players is not allowed.");
        add("ars_controle.target.get.none", "No target found.");
        add("ars_controle.target.get.entity", "Target entity: %s in %s.");
        add("ars_controle.target.get.block", "Target block: %s in %s.");
        add("ars_controle.remote.error.no_target", "No target stored in remote.");
        add("ars_controle.remote.error.invalid_dimension", "Target is in invalid dimension.");
        add("ars_controle.remote.error.different_dimension", "Target cannot be in different dimension.");
        add("ars_controle.remote.error.invalid_target", "Target is invalid.");
        add("ars_controle.portable_brazier_relay.relayed_to", "Relayed to: %s");
        add("ars_controle.portable_brazier_relay.blacklisted_ritual", "Ritual of %s can not be portably relayed.");

        add("ars_controle.remote.set_target", "Set remote target to %s in %s.");

        add("ars_controle.remote.lock_mode.first", "First");
        add("ars_controle.remote.lock_mode.last", "Last");
        add("ars_controle.remote.lock_mode.tooltip", "Locking %s");
        add("ars_controle.remote.lock_mode.radial", "Lock %s");
        add("ars_controle.remote.lock_mode.set", "Set locking mode to %s.");

        add("ars_controle.remote.selection_mode.single", "Single");
        add("ars_controle.remote.selection_mode.multiple", "Multiple");
        add("ars_controle.remote.selection_mode.tooltip", "%s Selection");
        add("ars_controle.remote.selection_mode.set", "Set selection mode to %s.");

        add("ars_nouveau.page1.block.ars_controle.warping_spell_prism", "The Warping Spell Prism allows you to warp a spell projectile to anywhere in the world, even across dimensions!");
        add("ars_nouveau.page1.block.ars_controle.scryers_linkage", "The Scryer's Linkage links to another block, allowing machines to interact with its linked block from distance places. It is capable of linking items, liquids, energy, redstone, and a few other things. To configure it, use a Dominion Wand first on the block you want to link to, then on the linkage.");
        add("ars_nouveau.page1.block.ars_controle.temporal_stability_sensor", "The Temporal Stability Sensor shows how unstable the time of the world is. Using a comparator, you can find out how close the world is to lagging, with higher redstone output meaning the world is closer to lagging. You can use this to turn off farms automatically to prevent the server from lagging.");
        add("ars_nouveau.page1.block.ars_controle.scroll_holder", "The Warp Scroll Holder allows you to easily open and close warp portals to your desired destinations. Simply replace one of your portal frames with a Warp Scroll Holder in an appropriate direction and insert a Warp Scroll while having some Source within 10 blocks.");

        add("ars_nouveau.page1.item.ars_controle.remote", "The Remote lets you remotely configure blocks and entities that the Dominion Wand can.\nIt has two radial menus, one for configuring which end of the connection is locked (first or second) accessible when holding Shift, and another for the mode of selection (single or multiple) accessible when not holding Shift.\nTo use it, first crouch and use it on the thing you would like to configure.");
        add("ars_nouveau.page2.item.ars_controle.remote", "Then, you can use it again on other blocks or entities depending on what the thing you are configuring accepts.\nFor example, selecting a Storage Lectern with Locking Last allows you to link inventories with ease.\nSelecting a Starbuncle with Locking First would let you select many inventories to deposit items to.\nWhen you are finished, left clicking air will clear the Remote's target.");

        add("ars_nouveau.page1.item.ars_controle.portable_brazier_relay", "The Portable Brazier Relay allows you to carry the effects of a ritual on you. This requires the ritual brazier to stay loaded and have a supply of source.");

        add("ars_nouveau.spell.validation.exists.binary_filters.next_two_not_filters", "%s requires the next two glyphs to be Filters.");
        add("ars_nouveau.spell.validation.adding.binary_filters.next_two_not_filters", "%s requires the next two glyphs to be Filters.");
        add("ars_nouveau.spell.validation.exists.binary_filters.no_chaining", "%s can not be part of another adaptive Filter.");
        add("ars_nouveau.spell.validation.adding.binary_filters.no_chaining", "%s can not be part of another adaptive Filter.");
        add("ars_nouveau.spell.validation.exists.unary_filters.next_not_filter", "%s requires the next glyph to be a Filter.");
        add("ars_nouveau.spell.validation.adding.unary_filters.next_not_filter", "%s requires the next glyph to be a Filter.");
        add("ars_nouveau.spell.validation.exists.unary_filters.no_chaining", "%s can not adapt another adaptive Filter.");
        add("ars_nouveau.spell.validation.adding.unary_filters.no_chaining", "%s can not adapt another adaptive Filter.");

        add("ars_controle.glyph.error.generic.error_at_position", "%s at position %d encountered an error.");

        add("ars_controle.spellbook.copied_to_clipboard", "Copied spell to clipboard.");

        add("ars_controle.config.warping_spell_prism", "Warping Spell Prism");
        add("ars_controle.config.warping_spell_prism.max_cost", "Max Cost");
        add("ars_controle.config.warping_spell_prism.cost_min_distance", "Cost Min. Distance");
        add("ars_controle.config.warping_spell_prism.cost_per_block", "Cost Per Block");
        add("ars_controle.config.warping_spell_prism.dimension_cost", "Dimension Cost");
        add("ars_controle.config.warping_spell_prism.load_time", "Load Time");

        add("ars_controle.config.enhanced_spellbook_controls", "Enhanced Spell Book Controls");
        add("ars_controle.config.search_bar", "Search Bar");
        add("ars_controle.config.search_bar.enable_auto_focus", "Enable Auto-Focus");
        add("ars_controle.config.search_bar.clear_on_auto_focus", "Clear on Auto-Focus");
        add("ars_controle.config.search_bar.escape_to_clear", "Escape to Clear");
        add("ars_controle.config.spell_crafting", "Spell Crafting");
        add("ars_controle.config.spell_crafting.swap_glyphs_with_number_keys", "Swap Glyphs With Number Keys");
    }

    @Override
    public void add(@NotNull Item key, @NotNull String name) {
        super.add(key, name);
    }

    @Override
    public void add(@NotNull String key, @NotNull String value) {
        super.add(key, value);
        data.put(key, value);
    }
}
