package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.common.lib.LibBlockNames;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.qther.ars_controle.registry.ACRegistry;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ACRecipeDatagen extends RecipeProvider {
    public ACRecipeDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput output) {
        super.buildRecipes(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ACRegistry.Blocks.SCROLL_HOLDER.get(), 1)
                .unlockedBy("has_sourcestone", InventoryChangeTrigger.TriggerInstance.hasItems(BlockRegistry.getBlock(LibBlockNames.SOURCESTONE)))
                .pattern("sss")
                .pattern("s s")
                .pattern("sss")
                .define('s', BlockRegistry.getBlock(LibBlockNames.SOURCESTONE + "_slab"))
                .save(output);
    }
}
