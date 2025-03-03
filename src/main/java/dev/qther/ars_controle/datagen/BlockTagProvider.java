package dev.qther.ars_controle.datagen;

import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends IntrinsicHolderTagsProvider<Block> {
    public BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), ArsControle.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ACRegistry.Blocks.WARPING_SPELL_PRISM.get(),
                ACRegistry.Blocks.SCRYERS_LINKAGE.get(),
                ACRegistry.Blocks.TEMPORAL_STABILITY_SENSOR.get()
        );
    }

    public @NotNull String getName() {
        return "Ars Controle tags";
    }
}
