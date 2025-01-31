package dev.qther.ars_controle.registry;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.setup.registry.BlockEntityTypeRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistryWrapper;
import com.hollingsworth.arsnouveau.setup.registry.ItemRegistryWrapper;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.block.ScryersLinkageBlock;
import dev.qther.ars_controle.block.TemporalStabilitySensorBlock;
import dev.qther.ars_controle.block.WarpingSpellPrismBlock;
import dev.qther.ars_controle.block.tile.ScryersLinkageTile;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import dev.qther.ars_controle.item.PortableBrazierRelayItem;
import dev.qther.ars_controle.item.RemoteItem;
import dev.qther.ars_controle.spell.effect.EffectPreciseDelay;
import dev.qther.ars_controle.spell.filter.FilterBinary;
import dev.qther.ars_controle.spell.filter.FilterRandom;
import dev.qther.ars_controle.spell.filter.FilterUnary;
import dev.qther.ars_controle.spell.filter.FilterYLevel;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.getDefaultBlockItem;
import static dev.qther.ars_controle.ArsControle.MODID;

public class ACRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArsControle.MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static void register(IEventBus bus) {
        Blocks.register(bus);
        Tiles.register(bus);
        Items.register(bus);
        Tabs.register(bus);
        Components.register(bus);
        Attachments.register(bus);

        Glyphs.registerAll();
    }

    public static class Attachments {
        public static final Supplier<AttachmentType<UUID>> RELAY_UUID = ATTACHMENT_TYPES.register(
                "relay_uuid", () -> AttachmentType.builder(() -> new UUID(0, 0)).serialize(UUIDUtil.CODEC).build()
        );

        public static final Supplier<AttachmentType<UUID>> ASSOCIATION = ATTACHMENT_TYPES.register(
                "association", () -> AttachmentType.builder(() -> new UUID(0, 0)).serialize(UUIDUtil.CODEC).build()
        );

        public static void register(IEventBus bus) {
            ATTACHMENT_TYPES.register(bus);
        }
    }

    public static class Blocks {
        public static BlockRegistryWrapper<WarpingSpellPrismBlock> WARPING_SPELL_PRISM = new BlockRegistryWrapper<>(BLOCKS.register(ACNames.WARPING_SPELL_PRISM, (block) -> new WarpingSpellPrismBlock()));
        public static BlockRegistryWrapper<ScryersLinkageBlock> SCRYERS_LINKAGE = new BlockRegistryWrapper<>(BLOCKS.register(ACNames.SCRYERS_LINKAGE, (block) -> new ScryersLinkageBlock()));
        public static BlockRegistryWrapper<TemporalStabilitySensorBlock> TEMPORAL_STABILITY_SENSOR = new BlockRegistryWrapper<>(BLOCKS.register(ACNames.TEMPORAL_STABILITY_SENSOR, (block) -> new TemporalStabilitySensorBlock()));

        public static void register(IEventBus bus) {
            BLOCKS.register(bus);
        }
    }

    public static class Components {
        public static final DeferredHolder<DataComponentType<?>, DataComponentType<RemoteItem.RemoteData>> REMOTE = DATA.register("remote_data",
                () -> DataComponentType.<RemoteItem.RemoteData>builder().persistent(RemoteItem.RemoteData.CODEC).networkSynchronized(RemoteItem.RemoteData.STREAM_CODEC).build()
        );

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<PortableBrazierRelayItem.PortableBrazierRelayData>> PORTABLE_BRAZIER_RELAY = DATA.register("portable_brazier",
                () -> DataComponentType.<PortableBrazierRelayItem.PortableBrazierRelayData>builder().persistent(PortableBrazierRelayItem.PortableBrazierRelayData.CODEC).networkSynchronized(PortableBrazierRelayItem.PortableBrazierRelayData.STREAM_CODEC).build()
        );

        public static void register(IEventBus bus) {
            DATA.register(bus);
        }
    }

    public static class Items {
        public static ItemRegistryWrapper<Item> WARPING_SPELL_PRISM = new ItemRegistryWrapper<>(ITEMS.register(ACNames.WARPING_SPELL_PRISM, () -> getDefaultBlockItem(Blocks.WARPING_SPELL_PRISM.get())));
        public static ItemRegistryWrapper<Item> SCRYERS_LINKAGE = new ItemRegistryWrapper<>(ITEMS.register(ACNames.SCRYERS_LINKAGE, () -> getDefaultBlockItem(Blocks.SCRYERS_LINKAGE.get())));
        public static ItemRegistryWrapper<Item> TEMPORAL_STABILITY_SENSOR = new ItemRegistryWrapper<>(ITEMS.register(ACNames.TEMPORAL_STABILITY_SENSOR, () -> getDefaultBlockItem(Blocks.TEMPORAL_STABILITY_SENSOR.get())));
        public static ItemRegistryWrapper<Item> REMOTE = new ItemRegistryWrapper<>(ITEMS.register(ACNames.REMOTE, RemoteItem::new));
        public static ItemRegistryWrapper<Item> PORTABLE_BRAZIER_RELAY = new ItemRegistryWrapper<>(ITEMS.register(ACNames.PORTABLE_BRAZIER_RELAY, PortableBrazierRelayItem::new));

        public static void register(IEventBus bus) {
            ITEMS.register(bus);
        }
    }

    public static class Tiles {
        public static BlockEntityTypeRegistryWrapper<WarpingSpellPrismTile> WARPING_SPELL_PRISM = new BlockEntityTypeRegistryWrapper<>(TILES.register(ACNames.WARPING_SPELL_PRISM, () -> BlockEntityType.Builder.of(WarpingSpellPrismTile::new, Blocks.WARPING_SPELL_PRISM.get()).build(null)));
        public static BlockEntityTypeRegistryWrapper<ScryersLinkageTile> SCRYERS_LINKAGE = new BlockEntityTypeRegistryWrapper<>(TILES.register(ACNames.SCRYERS_LINKAGE, () -> BlockEntityType.Builder.of(ScryersLinkageTile::new, Blocks.SCRYERS_LINKAGE.get()).build(null)));

        public static void register(IEventBus bus) {
            TILES.register(bus);
        }
    }

    public static class Tabs {
        public static DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("general", () -> CreativeModeTab.builder()
                .title(Component.literal("Ars Controle"))
                .icon(() -> Items.REMOTE.get().getDefaultInstance())
                .displayItems((params, output) -> {
                    for (var entry : ITEMS.getEntries()) {
                        output.accept(entry.get().getDefaultInstance());
                    }
                })
                .build());

        public static void register(IEventBus bus) {
            TABS.register(bus);
        }
    }

    public static class Glyphs {
        public static List<AbstractSpellPart> registeredSpells = new ArrayList<>();

        public static class Effects {
            public static AbstractSpellPart PRECISE_DELAY;
        }
        
        public static class Filters {
            public static AbstractSpellPart ABOVE;
            public static AbstractSpellPart BELOW;
            public static AbstractSpellPart LEVEL;
            public static AbstractSpellPart OR;
            public static AbstractSpellPart XOR;
            public static AbstractSpellPart XNOR;
            public static AbstractSpellPart NOT;
            public static AbstractSpellPart RANDOM;
        }

        public static void registerAll() {
            // Effects
            Effects.PRECISE_DELAY = register(EffectPreciseDelay.INSTANCE);

            // Filters
            Filters.ABOVE = register(FilterYLevel.ABOVE);
            Filters.BELOW = register(FilterYLevel.BELOW);
            Filters.LEVEL = register(FilterYLevel.LEVEL);
            Filters.OR = register(FilterBinary.OR);
            Filters.XOR = register(FilterBinary.XOR);
            Filters.XNOR = register(FilterBinary.XNOR);
            Filters.NOT = register(FilterUnary.NOT);
            Filters.RANDOM = register(FilterRandom.INSTANCE);
        }

        public static AbstractSpellPart register(AbstractSpellPart spellPart) {
            GlyphRegistry.registerSpell(spellPart);
            registeredSpells.add(spellPart);
            return spellPart;
        }
    }
}
