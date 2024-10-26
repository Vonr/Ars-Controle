package dev.qther.ars_controle.registry;

import com.hollingsworth.arsnouveau.setup.registry.BlockEntityTypeRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.ItemRegistryWrapper;
import dev.qther.ars_controle.block.WarpingSpellPrismBlock;
import dev.qther.ars_controle.item.RemoteItem;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.getDefaultBlockItem;
import static dev.qther.ars_controle.ArsControle.MODID;

public class ModRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);

    public static void registerRegistries(IEventBus bus) {
        BLOCKS.register(bus);
        TILES.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        TABS.register(bus);
        DATA.register(bus);
    }

    public static BlockRegistryWrapper<WarpingSpellPrismBlock> WARPING_SPELL_PRISM_BLOCK = new BlockRegistryWrapper<>(BLOCKS.register(ModNames.WARPING_SPELL_PRISM, (block) -> new WarpingSpellPrismBlock()));
    public static BlockEntityTypeRegistryWrapper<WarpingSpellPrismTile> WARPING_SPELL_PRISM_TILE = new BlockEntityTypeRegistryWrapper<>(TILES.register(ModNames.WARPING_SPELL_PRISM, () -> BlockEntityType.Builder.of(WarpingSpellPrismTile::new, WARPING_SPELL_PRISM_BLOCK.get()).build(null)));
    public static ItemRegistryWrapper<Item> WARPING_SPELL_PRISM_ITEM = new ItemRegistryWrapper<>(ITEMS.register(ModNames.WARPING_SPELL_PRISM, () -> getDefaultBlockItem(WARPING_SPELL_PRISM_BLOCK.get())));

    public static ItemRegistryWrapper<Item> REMOTE = new ItemRegistryWrapper<>(ITEMS.register(ModNames.REMOTE, RemoteItem::new));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteItem.RemoteData>> REMOTE_DATA = DATA.register("remote_data",
            () -> DataComponentType.<RemoteItem.RemoteData>builder().persistent(RemoteItem.RemoteData.CODEC).networkSynchronized(RemoteItem.RemoteData.STREAM_CODEC).build()
    );

    public static DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB = TABS.register("general", () -> CreativeModeTab.builder()
            .title(Component.literal("Ars Controle"))
            .icon(() -> REMOTE.get().getDefaultInstance())
            .displayItems((params, output) -> {
                for (var entry : ITEMS.getEntries()) {
                    output.accept(entry.get().getDefaultInstance());
                }
            })
            .build());
}