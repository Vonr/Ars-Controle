package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.api.familiar.AbstractFamiliarHolder;
import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.api.spell.AbstractCastMethod;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.common.datagen.ApparatusRecipeProvider;
import com.hollingsworth.arsnouveau.common.datagen.GlyphRecipeProvider;
import com.hollingsworth.arsnouveau.common.datagen.ImbuementRecipeProvider;
import com.hollingsworth.arsnouveau.common.datagen.patchouli.*;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.mojang.serialization.JsonOps;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ArsNouveauRegistry;
import dev.qther.ars_controle.registry.ModNames;
import dev.qther.ars_controle.registry.ModRegistry;
import dev.qther.ars_controle.spell.effect.EffectPreciseDelay;
import dev.qther.ars_controle.spell.filter.FilterBinary;
import dev.qther.ars_controle.spell.filter.FilterYLevel;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.nio.file.Path;

import static com.hollingsworth.arsnouveau.setup.registry.RegistryHelper.getRegistryName;

public class ArsProviders {

    static String root = ArsControle.MODID;

    public static class GlyphProvider extends GlyphRecipeProvider {
        public GlyphProvider(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        public void collectJsons(CachedOutput cache) {
            var output = this.generator.getPackOutput().getOutputFolder();

            recipes.add(get(EffectPreciseDelay.INSTANCE).withItem(ItemsRegistry.MANIPULATION_ESSENCE).withItem(Items.CLOCK).withItem(Items.COMPARATOR));
            recipes.add(get(FilterYLevel.ABOVE).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(Items.FEATHER));
            recipes.add(get(FilterYLevel.BELOW).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(Items.COBBLED_DEEPSLATE));
            recipes.add(get(FilterYLevel.LEVEL).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(Items.SHORT_GRASS));
            recipes.add(get(FilterBinary.OR).withItem(Items.COMPARATOR).withItem(Items.REDSTONE));
            recipes.add(get(FilterBinary.XOR).withItem(Items.COMPARATOR).withItem(Items.REDSTONE_TORCH));
            recipes.add(get(FilterBinary.XNOR).withItem(Items.COMPARATOR).withItem(Items.REDSTONE).withItem(Items.REDSTONE_TORCH));

            for (var recipe : recipes) {
                var path = getScribeGlyphPath(output, recipe.output.getItem());
                saveStable(cache, GlyphRecipe.CODEC.encodeStart(JsonOps.INSTANCE, recipe).getOrThrow(), path);
            }
        }

        protected static Path getScribeGlyphPath(Path pathIn, Item glyph) {
            return pathIn.resolve("data/" + root + "/recipe/" + getRegistryName(glyph).getPath() + ".json");
        }

        @Override
        public String getName() {
            return "Ars Controle Glyph Recipes";
        }
    }

    public static class EnchantingAppProvider extends ApparatusRecipeProvider {

        public EnchantingAppProvider(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        public void collectJsons(CachedOutput cache) {
            addRecipe(builder()
                    .withResult(ModRegistry.WARPING_SPELL_PRISM_ITEM)
                    .withReagent(BlockRegistry.SPELL_PRISM)
                    .withPedestalItem(4, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(4, Ingredient.of(Items.POPPED_CHORUS_FRUIT))
                    .build());

            addRecipe(builder()
                    .withResult(ModRegistry.REMOTE)
                    .withReagent(ItemsRegistry.DOMINION_ROD)
                    .withPedestalItem(4, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(4, Ingredient.of(Items.POPPED_CHORUS_FRUIT))
                    .build());

            var output = this.generator.getPackOutput().getOutputFolder();
            for (var g : recipes) {
                if (g != null) {
                    Path path = getRecipePath(output, g.id().getPath());
                    saveStable(cache, EnchantingApparatusRecipe.CODEC.encodeStart(JsonOps.INSTANCE, g.recipe()).getOrThrow(), path);
                }
            }

        }

        protected static Path getRecipePath(Path pathIn, String str) {
            return pathIn.resolve("data/" + root + "/recipe/" + str + ".json");
        }

        @Override
        public String getName() {
            return "Ars Controle Apparatus";
        }
    }

    public static class ImbuementProvider extends ImbuementRecipeProvider {
        public ImbuementProvider(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        public void collectJsons(CachedOutput cache) {

            /*
            recipes.add(new ImbuementRecipe("example_focus", Ingredient.of(Items.AMETHYST_SHARD), new ItemStack(ItemsRegistry.SUMMONING_FOCUS, 1), 5000)
                    .withPedestalItem(ItemsRegistry.WILDEN_TRIBUTE)
            );
            */

            var output = generator.getPackOutput().getOutputFolder();
            for (var g : recipes) {
                var path = getRecipePath(output, g.id.getPath());
                saveStable(cache, ImbuementRecipe.CODEC.encodeStart(JsonOps.INSTANCE, g).getOrThrow(), path);
            }

        }

        protected Path getRecipePath(Path pathIn, String str) {
            return pathIn.resolve("data/" + root + "/recipe/" + str + ".json");
        }

        @Override
        public String getName() {
            return "Ars Controle Imbuement";
        }
    }

    public static class PatchouliProvider extends com.hollingsworth.arsnouveau.common.datagen.PatchouliProvider {
        public PatchouliProvider(DataGenerator generatorIn) {
            super(generatorIn);
        }

        @Override
        public void collectJsons(CachedOutput cache) {
            for (var spell : ArsNouveauRegistry.registeredSpells) {
                addGlyphPage(spell);
            }

            addPage(new PatchouliBuilder(AUTOMATION, ModRegistry.WARPING_SPELL_PRISM_ITEM.get())
                            .withName("ars_controle.page.warping_spell_prism")
                            .withIcon(ModRegistry.WARPING_SPELL_PRISM_ITEM.get())
                            .withTextPage("ars_controle.page1.warping_spell_prism")
                            .withPage(new ApparatusPage(ModRegistry.WARPING_SPELL_PRISM_ITEM))
                            .withPage(new RelationsPage().withEntry(AUTOMATION, "spell_prism")),
                    getPath(AUTOMATION, ModNames.WARPING_SPELL_PRISM));

            addPage(new PatchouliBuilder(AUTOMATION, ModRegistry.REMOTE.get())
                            .withName("ars_controle.page.remote")
                            .withIcon(ModRegistry.REMOTE.get())
                            .withTextPage("ars_controle.page1.remote")
                            .withTextPage("ars_controle.page2.remote")
                            .withPage(new ApparatusPage(ModRegistry.REMOTE))
                            .withPage(new RelationsPage().withEntry(AUTOMATION, "dominion_wand")),
                    getPath(AUTOMATION, ModNames.REMOTE));

            System.out.println(this.pages);

            for (var page : this.pages) {
                saveStable(cache, page.build(), page.path());
            }
        }

        @Override
        public PatchouliPage addBasicItem(ItemLike item, ResourceLocation category, IPatchouliPage recipePage) {
            PatchouliBuilder builder = new PatchouliBuilder(category, item.asItem().getDescriptionId())
                    .withIcon(item.asItem())
                    .withPage(new TextPage(root + ".page." + getRegistryName(item.asItem()).getPath()));
            if (recipePage != null) {
                builder = builder.withPage(recipePage);
            }
            PatchouliPage page = new PatchouliPage(builder, getPath(category, getRegistryName(item.asItem()).getPath()));
            this.pages.add(page);
            return page;
        }

        public void addFamiliarPage(AbstractFamiliarHolder familiarHolder) {
            PatchouliBuilder builder = new PatchouliBuilder(FAMILIARS, "entity." + root + "." + familiarHolder.getRegistryName().getPath())
                    .withIcon(root + ":" + familiarHolder.getRegistryName().getPath())
                    .withTextPage(root + ".familiar_desc." + familiarHolder.getRegistryName().getPath())
                    .withPage(new EntityPage(familiarHolder.getRegistryName().toString()));
            this.pages.add(new PatchouliPage(builder, getPath(FAMILIARS, familiarHolder.getRegistryName().getPath())));
        }

        public void addRitualPage(AbstractRitual ritual) {
            PatchouliBuilder builder = new PatchouliBuilder(RITUALS, "item." + root + "." + ritual.getRegistryName().getPath())
                    .withIcon(ritual.getRegistryName().toString())
                    .withTextPage(ritual.getDescriptionKey())
                    .withPage(new CraftingPage(root + ":tablet_" + ritual.getRegistryName().getPath()));

            this.pages.add(new PatchouliPage(builder, getPath(RITUALS, ritual.getRegistryName().getPath())));
        }

        public void addGlyphPage(AbstractSpellPart spellPart) {
            ResourceLocation category = switch (spellPart.defaultTier().value) {
                case 1 -> GLYPHS_1;
                case 2 -> GLYPHS_2;
                default -> GLYPHS_3;
            };
            PatchouliBuilder builder = new PatchouliBuilder(category, spellPart.getName())
                    .withName(root + ".glyph_name." + spellPart.getRegistryName().getPath())
                    .withIcon(spellPart.getRegistryName().toString())
                    .withSortNum(spellPart instanceof AbstractCastMethod ? 1 : spellPart instanceof AbstractEffect ? 2 : 3)
                    .withPage(new TextPage(root + ".glyph_desc." + spellPart.getRegistryName().getPath()))
                    .withPage(new GlyphScribePage(spellPart));
            this.pages.add(new PatchouliPage(builder, getPath(category, spellPart.getRegistryName().getPath())));
        }
    }
}
