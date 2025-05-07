package dev.qther.ars_controle.datagen;

import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.ritual.*;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ACItemTagProvider extends IntrinsicHolderTagsProvider<Item> {
    public static final TagKey<Item> RITUAL_BLACKLIST = TagKey.create(Registries.ITEM, ArsControle.prefix("ritual_blacklist"));

    public ACItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, Registries.ITEM, future, ACItemTagProvider::extractKey, ArsControle.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        tag(RITUAL_BLACKLIST).add(
                RitualRegistry.getRitualItemMap().get(new RitualDig().getRegistryName()),
                RitualRegistry.getRitualItemMap().get(new RitualAwakening().getRegistryName()),
                RitualRegistry.getRitualItemMap().get(new RitualSunrise().getRegistryName()),
                RitualRegistry.getRitualItemMap().get(new RitualMoonfall().getRegistryName()),
                RitualRegistry.getRitualItemMap().get(new RitualPillagerRaid().getRegistryName())
        );

        tag(Tags.Items.TOOLS).add(
                ACRegistry.Items.REMOTE.get(),
                ACRegistry.Items.PORTABLE_BRAZIER_RELAY.get()
        );

        tag(Tags.Items.ENCHANTABLES).add(
                ACRegistry.Items.REMOTE.get(),
                ACRegistry.Items.PORTABLE_BRAZIER_RELAY.get()
        );
    }

    public static ResourceKey<Item> extractKey(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).get();
    }
}
