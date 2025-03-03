package dev.qther.ars_controle.registry;

import com.hollingsworth.arsnouveau.api.documentation.DocCategory;
import com.hollingsworth.arsnouveau.api.documentation.ReloadDocumentationEvent;
import com.hollingsworth.arsnouveau.api.documentation.builder.DocEntryBuilder;
import com.hollingsworth.arsnouveau.api.documentation.entry.DocEntry;
import com.hollingsworth.arsnouveau.api.registry.DocumentationRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.setup.registry.*;
import dev.qther.ars_controle.ArsControle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static com.hollingsworth.arsnouveau.api.registry.DocumentationRegistry.*;

@EventBusSubscriber
public class ACDocumentation {
    @SubscribeEvent
    public static void addPages(ReloadDocumentationEvent.AddEntries event) {
        for (var glyph : ACRegistry.Glyphs.registeredSpells) {
            var entry = addPage(EntryBuilder.of(glyph)
                    .withName("ars_controle.glyph_name." + glyph.getRegistryName().getPath())
                    .withIcon(glyph.glyphItem)
                    .withCraftingPages(glyph.glyphItem));

            entry.withSearchTag(Component.translatable("ars_nouveau.keyword.glyph"));

            for (SpellSchool school : glyph.spellSchools){
                entry.withSearchTag(school.getTextComponent());
                for (SpellSchool subschool : school.getSubSchools()) {
                    entry.withSearchTag(subschool.getTextComponent());
                }
            }
        }

        addPage(EntryBuilder.of(CRAFTING, ACRegistry.Blocks.WARPING_SPELL_PRISM)
                        .withIcon(ACRegistry.Blocks.WARPING_SPELL_PRISM)
                        .withTextPage("ars_controle.page1.warping_spell_prism")
                        .withCraftingPages(ACRegistry.Blocks.WARPING_SPELL_PRISM))
                .withRelation(block(BlockRegistry.SPELL_PRISM));

        addPage(EntryBuilder.of(CRAFTING, ACRegistry.Blocks.SCRYERS_LINKAGE)
                        .withIcon(ACRegistry.Blocks.SCRYERS_LINKAGE)
                        .withTextPage("ars_controle.page1.scryers_linkage")
                        .withCraftingPages(ACRegistry.Blocks.SCRYERS_LINKAGE));


        addPage(EntryBuilder.of(CRAFTING, ACRegistry.Blocks.TEMPORAL_STABILITY_SENSOR)
                        .withIcon(ACRegistry.Blocks.TEMPORAL_STABILITY_SENSOR)
                        .withTextPage("ars_controle.page1.temporal_stability_sensor")
                        .withCraftingPages(ACRegistry.Blocks.TEMPORAL_STABILITY_SENSOR));

        addPage(EntryBuilder.of(CRAFTING, ACRegistry.Items.REMOTE)
                        .withIcon(ACRegistry.Items.REMOTE)
                        .withTextPage("ars_controle.page1.remote")
                        .withTextPage("ars_controle.page2.remote")
                        .withCraftingPages(ACRegistry.Items.REMOTE))
                .withRelation(item(ItemsRegistry.DOMINION_ROD));

        addPage(EntryBuilder.of(ITEMS, ACRegistry.Items.PORTABLE_BRAZIER_RELAY)
                .withIcon(ACRegistry.Items.PORTABLE_BRAZIER_RELAY)
                .withTextPage("ars_controle.page1.portable_brazier_relay")
                .withCraftingPages(ACRegistry.Items.PORTABLE_BRAZIER_RELAY))
                .withRelations(
                        block(BlockRegistry.RITUAL_BLOCK),
                        block(BlockRegistry.BRAZIER_RELAY)
                )
                .withSearchTag(Component.translatable("ars_nouveau.keyword.ritual"));
    }

    @SubscribeEvent
    public static void editPages(ReloadDocumentationEvent.Post event) {
        block(BlockRegistry.SPELL_PRISM)
                .withRelation(block(ACRegistry.Blocks.WARPING_SPELL_PRISM));

        item(ItemsRegistry.DOMINION_ROD)
                .withRelation(item(ACRegistry.Items.REMOTE));

        block(BlockRegistry.RITUAL_BLOCK)
                .withRelation(item(ACRegistry.Items.PORTABLE_BRAZIER_RELAY));

        block(BlockRegistry.BRAZIER_RELAY)
                .withRelation(item(ACRegistry.Items.PORTABLE_BRAZIER_RELAY));
    }

    private static DocEntry block(BlockRegistryWrapper<? extends Block> block) {
        return getEntry(block.getResourceLocation().withPath(block.get().getDescriptionId()));
    }

    private static DocEntry item(ItemRegistryWrapper<? extends Item> item) {
        return getEntry(item.getResourceLocation().withPath(item.get().getDescriptionId()));
    }

    private static DocEntry addPage(DocEntryBuilder builder) {
        return DocumentationRegistry.registerEntry(builder.category, builder.build());
    }

    static class EntryBuilder extends DocEntryBuilder {
        public static EntryBuilder of(DocCategory category, String name) {
            return of(category, name, ArsControle.prefix(name));
        }

        public static EntryBuilder of(DocCategory category, String name, ResourceLocation entryId) {
            return new EntryBuilder(category, name.contains(".") ? name : ArsControle.MODID + ".page." + name, entryId);
        }

        public static EntryBuilder of(DocCategory category, ItemRegistryWrapper<? extends Item> item) {
            return of(category, item.get().getDescriptionId());
        }

        public static EntryBuilder of(AbstractSpellPart glyph) {
            return of(Documentation.glyphCategory(glyph.getConfigTier()), glyph.getRegistryName().getPath());
        }

        public static EntryBuilder of(DocCategory category, BlockRegistryWrapper<? extends Block> block) {
            return of(category, block.get().getDescriptionId());
        }

        private EntryBuilder(DocCategory category, String name, ResourceLocation entryId) {
            super(category, name.contains(".") ? name : ArsControle.MODID + ".page." + name, entryId);
        }

        private EntryBuilder(DocCategory category, ItemLike itemLike) {
            super(category, itemLike);
        }
    }
}
