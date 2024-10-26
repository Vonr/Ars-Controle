package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import dev.qther.ars_controle.ArsControle;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class BlockStateDatagen extends BlockStateProvider {
    private final ExistingFileHelper fileHelper;

    public BlockStateDatagen(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ArsControle.MODID, exFileHelper);
        this.fileHelper = exFileHelper;
    }

    @Override
    protected void registerStatesAndModels() {
        for (Supplier<Glyph> i : GlyphRegistry.getGlyphItemMap().values()) {
            ResourceLocation spellPart = i.get().spellPart.getRegistryName();
            if (!spellPart.getNamespace().equals(ArsControle.MODID)) continue;
            itemModels().basicItem(spellPart);
        }
    }

    public ResourceLocation key(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }
}