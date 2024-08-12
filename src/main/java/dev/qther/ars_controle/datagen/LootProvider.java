package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.ArsNouveau;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class LootProvider extends LootTableProvider {
    public LootProvider(PackOutput pOutput) {
        super(pOutput, new HashSet<>(), List.of(new LootTableProvider.SubProviderEntry(BlockLootTable::new, LootContextParamSets.BLOCK)));
    }

    public static class BlockLootTable extends BlockLootSubProvider {
        public List<Block> list = new ArrayList<>();

        protected BlockLootTable() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), new HashMap<>());
        }

        @Override
        protected void generate() {
            registerDropSelf(ModRegistry.WARPING_SPELL_PRISM_BLOCK);
        }

        @Override
        public void generate(BiConsumer<ResourceLocation, LootTable.Builder> p_249322_) {
            this.generate();
            Set<ResourceLocation> set = new HashSet<>();

            for (Block block : list) {
                if (block.isEnabled(this.enabledFeatures)) {
                    ResourceLocation resourcelocation = block.getLootTable();
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

        public void registerDropSelf(RegistryObject<? extends Block> block) {
            list.add(block.get());
            dropSelf(block.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ForgeRegistries.BLOCKS.getValues().stream().filter(block -> ForgeRegistries.BLOCKS.getKey(block).getNamespace().equals(ArsNouveau.MODID)).collect(Collectors.toList());
        }
    }
}
