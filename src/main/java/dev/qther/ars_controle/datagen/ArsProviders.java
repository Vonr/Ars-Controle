package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.common.datagen.ApparatusRecipeProvider;
import com.hollingsworth.arsnouveau.common.datagen.GlyphRecipeProvider;
import com.hollingsworth.arsnouveau.common.datagen.ImbuementRecipeProvider;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentRandomize;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.mojang.serialization.JsonOps;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.spell.effect.EffectPreciseDelay;
import dev.qther.ars_controle.spell.filter.FilterBinary;
import dev.qther.ars_controle.spell.filter.FilterRandom;
import dev.qther.ars_controle.spell.filter.FilterUnary;
import dev.qther.ars_controle.spell.filter.FilterYLevel;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
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

            recipes.add(get(EffectPreciseDelay.INSTANCE).withItem(ItemsRegistry.MANIPULATION_ESSENCE).withItem(net.minecraft.world.item.Items.CLOCK).withItem(net.minecraft.world.item.Items.COMPARATOR));
            recipes.add(get(FilterYLevel.ABOVE).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(net.minecraft.world.item.Items.FEATHER));
            recipes.add(get(FilterYLevel.BELOW).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(net.minecraft.world.item.Items.COBBLED_DEEPSLATE));
            recipes.add(get(FilterYLevel.LEVEL).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(net.minecraft.world.item.Items.SHORT_GRASS));
            recipes.add(get(FilterBinary.OR).withItem(net.minecraft.world.item.Items.COMPARATOR).withItem(net.minecraft.world.item.Items.REDSTONE));
            recipes.add(get(FilterBinary.XOR).withItem(net.minecraft.world.item.Items.COMPARATOR).withItem(net.minecraft.world.item.Items.REDSTONE_TORCH));
            recipes.add(get(FilterBinary.XNOR).withItem(net.minecraft.world.item.Items.COMPARATOR).withItem(net.minecraft.world.item.Items.REDSTONE).withItem(net.minecraft.world.item.Items.REDSTONE_TORCH));
            recipes.add(get(FilterUnary.NOT).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(net.minecraft.world.item.Items.REDSTONE_TORCH));
            recipes.add(get(FilterRandom.INSTANCE).withItem(ItemsRegistry.ALLOW_ITEM_SCROLL).withItem(AugmentRandomize.INSTANCE.getGlyph()));

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
                    .withResult(ACRegistry.Items.WARPING_SPELL_PRISM)
                    .withReagent(BlockRegistry.SPELL_PRISM)
                    .withPedestalItem(4, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(4, Ingredient.of(net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT))
                    .build());

            addRecipe(builder()
                    .withResult(ACRegistry.Items.SCRYERS_LINKAGE)
                    .withReagent(BlockRegistry.SCRYERS_CRYSTAL)
                    .withPedestalItem(4, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(4, Ingredient.of(net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT))
                    .build());

            addRecipe(builder()
                    .withResult(ACRegistry.Items.TEMPORAL_STABILITY_SENSOR)
                    .withReagent(net.minecraft.world.item.Items.CLOCK)
                    .withPedestalItem(1, Ingredient.of(net.minecraft.world.item.Items.ENDER_EYE))
                    .withPedestalItem(1, Ingredient.of(BlockRegistry.SOURCE_GEM_BLOCK))
                    .build());

            addRecipe(builder()
                    .withResult(ACRegistry.Items.REMOTE)
                    .withReagent(ItemsRegistry.DOMINION_ROD)
                    .withPedestalItem(4, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(4, Ingredient.of(net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT))
                    .build());

            addRecipe(builder()
                    .withResult(ACRegistry.Items.PORTABLE_BRAZIER_RELAY)
                    .withReagent(BlockRegistry.BRAZIER_RELAY)
                    .withPedestalItem(2, Ingredient.of(Tags.Items.ENDER_PEARLS))
                    .withPedestalItem(2, Ingredient.of(net.minecraft.world.item.Items.POPPED_CHORUS_FRUIT))
                    .withPedestalItem(2, Ingredient.of(ItemsRegistry.MANIPULATION_ESSENCE))
                    .withPedestalItem(1, Ingredient.of(net.minecraft.world.item.Items.NETHER_STAR))
                    .withPedestalItem(1, Ingredient.of(ItemsRegistry.WILDEN_TRIBUTE))
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
}
