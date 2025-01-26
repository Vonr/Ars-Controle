package dev.qther.ars_controle.registry;

import com.hollingsworth.arsnouveau.setup.registry.BlockEntityTypeRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.ItemRegistryWrapper;
import dev.qther.ars_controle.block.ScryersLinkageBlock;
import dev.qther.ars_controle.block.TemporalStabilitySensorBlock;
import dev.qther.ars_controle.block.WarpingSpellPrismBlock;
import dev.qther.ars_controle.block.tile.ScryersLinkageTile;
import dev.qther.ars_controle.item.PortableBrazierRelayItem;
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

    public static BlockRegistryWrapper<ScryersLinkageBlock> SCRYERS_LINKAGE_BLOCK = new BlockRegistryWrapper<>(BLOCKS.register(ModNames.SCRYERS_LINKAGE, (block) -> new ScryersLinkageBlock()));
    public static BlockEntityTypeRegistryWrapper<ScryersLinkageTile> SCRYERS_LINKAGE_TILE = new BlockEntityTypeRegistryWrapper<>(TILES.register(ModNames.SCRYERS_LINKAGE, () -> BlockEntityType.Builder.of(ScryersLinkageTile::new, SCRYERS_LINKAGE_BLOCK.get()).build(null)));
    public static ItemRegistryWrapper<Item> SCRYERS_LINKAGE_ITEM = new ItemRegistryWrapper<>(ITEMS.register(ModNames.SCRYERS_LINKAGE, () -> getDefaultBlockItem(SCRYERS_LINKAGE_BLOCK.get())));

    public static BlockRegistryWrapper<TemporalStabilitySensorBlock> TEMPORAL_STABILITY_SENSOR = new BlockRegistryWrapper<>(BLOCKS.register(ModNames.TEMPORAL_STABILITY_SENSOR, (block) -> new TemporalStabilitySensorBlock()));
    public static ItemRegistryWrapper<Item> TEMPORAL_STABILITY_SENSOR_ITEM = new ItemRegistryWrapper<>(ITEMS.register(ModNames.TEMPORAL_STABILITY_SENSOR, () -> getDefaultBlockItem(TEMPORAL_STABILITY_SENSOR.get())));

    public static ItemRegistryWrapper<Item> REMOTE = new ItemRegistryWrapper<>(ITEMS.register(ModNames.REMOTE, RemoteItem::new));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteItem.RemoteData>> REMOTE_DATA = DATA.register("remote_data",
            () -> DataComponentType.<RemoteItem.RemoteData>builder().persistent(RemoteItem.RemoteData.CODEC).networkSynchronized(RemoteItem.RemoteData.STREAM_CODEC).build()
    );

    public static ItemRegistryWrapper<Item> PORTABLE_BRAZIER_RELAY = new ItemRegistryWrapper<>(ITEMS.register(ModNames.PORTABLE_BRAZIER_RELAY, PortableBrazierRelayItem::new));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PortableBrazierRelayItem.PortableBrazierRelayData>> PORTABLE_BRAZIER_RELAY_DATA = DATA.register("portable_brazier",
            () -> DataComponentType.<PortableBrazierRelayItem.PortableBrazierRelayData>builder().persistent(PortableBrazierRelayItem.PortableBrazierRelayData.CODEC).networkSynchronized(PortableBrazierRelayItem.PortableBrazierRelayData.STREAM_CODEC).build()
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