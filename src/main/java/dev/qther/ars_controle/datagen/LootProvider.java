package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistryWrapper;
import dev.qther.ars_controle.registry.ModRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class LootProvider extends LootTableProvider {
    public LootProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(pOutput, new ObjectOpenHashSet<>(), List.of(new LootTableProvider.SubProviderEntry(BlockLootTable::new, LootContextParamSets.BLOCK)), registries);
    }

    public static class BlockLootTable extends BlockLootSubProvider {
        public List<Block> list = new ArrayList<>();

        protected BlockLootTable(HolderLookup.Provider provider) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
        }

        @Override
        protected void generate() {
            registerDropSelf(ModRegistry.WARPING_SPELL_PRISM_BLOCK);
            registerDropSelf(ModRegistry.SCRYERS_LINKAGE_BLOCK);
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> p_249322_) {
            this.generate();
            var set = new ObjectOpenHashSet<>();

            for (Block block : list) {
                if (block.isEnabled(this.enabledFeatures)) {
                    ResourceKey<LootTable> resourcelocation = block.getLootTable();
                    if (resourcelocation != BuiltInLootTables.EMPTY && set.add(resourcelocation)) {
                        LootTable.Builder loottable$builder = this.map.remove(resourcelocation);
                        if (loottable$builder == null) {
                            continue;
                        }

                        p_249322_.accept(resourcelocation, loottable$builder);
                    }
                }
            }
        }

        public void registerDropSelf(BlockRegistryWrapper<? extends Block> block) {
            list.add(block.get());
            dropSelf(block.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return BuiltInRegistries.BLOCK.stream().filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(ArsNouveau.MODID)).collect(Collectors.toList());
        }
    }
}
