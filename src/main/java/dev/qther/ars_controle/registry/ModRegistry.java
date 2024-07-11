package dev.qther.ars_controle.registry;

import dev.qther.ars_controle.block.WarpingSpellPrismBlock;
import dev.qther.ars_controle.item.RemoteItem;
import dev.qther.ars_controle.tile.WarpingSpellPrismTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.getDefaultBlockItem;
import static dev.qther.ars_controle.ArsControle.MODID;

public class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static void registerRegistries(IEventBus bus) {
        BLOCKS.register(bus);
        TILES.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        TABS.register(bus);
    }

    public static RegistryObject<WarpingSpellPrismBlock> WARPING_SPELL_PRISM_BLOCK = BLOCKS.register(ModNames.WARPING_SPELL_PRISM, WarpingSpellPrismBlock::new);
    public static RegistryObject<BlockEntityType<WarpingSpellPrismTile>> WARPING_SPELL_PRISM_TILE = TILES.register(ModNames.WARPING_SPELL_PRISM, () -> BlockEntityType.Builder.of(WarpingSpellPrismTile::new, WARPING_SPELL_PRISM_BLOCK.get()).build(null));
    public static RegistryObject<Item> WARPING_SPELL_PRISM_ITEM = ITEMS.register(ModNames.WARPING_SPELL_PRISM, () -> getDefaultBlockItem(WARPING_SPELL_PRISM_BLOCK.get()));

    public static RegistryObject<Item> REMOTE = ITEMS.register(ModNames.REMOTE, RemoteItem::new);

    public static RegistryObject<CreativeModeTab> CREATIVE_TAB = TABS.register("general", () -> CreativeModeTab.builder()
            .title(Component.literal("Ars Controle"))
            .icon(() -> REMOTE.get().getDefaultInstance())
            .displayItems((params, output) -> {
                for (var entry : ITEMS.getEntries()) {
                    output.accept(entry.get().getDefaultInstance());
                }
            })
            .build());
}