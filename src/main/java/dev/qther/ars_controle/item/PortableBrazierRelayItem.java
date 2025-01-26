package dev.qther.ars_controle.item;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import com.hollingsworth.arsnouveau.common.items.ModItem;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.qther.ars_controle.Cached;
import dev.qther.ars_controle.mixin.AbstractRitualInvoker;
import dev.qther.ars_controle.packets.clientbound.PacketSyncAssociation;
import dev.qther.ars_controle.registry.AttachmentRegistry;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PortableBrazierRelayItem extends ModItem {
    private static long LAST_UPDATE = -60;
    private static final Map<AbstractRitual, Player> rituals = new WeakHashMap<>();

    public static void clearCache() {
        LAST_UPDATE = -60;
        rituals.clear();
    }

    public static Map<AbstractRitual, Player> getRelayedRituals() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return Collections.emptyMap();
        }

        if (LAST_UPDATE + 60 >= server.getTickCount()) {
            return rituals;
        }
        LAST_UPDATE = server.getTickCount();

        rituals.clear();
        for (var player : server.getPlayerList().getPlayers()) {
            for (var stack : player.getInventory().items) {
                if (stack.is(ModRegistry.PORTABLE_BRAZIER_RELAY.asItem())) {
                    var data = PortableBrazierRelayData.fromItemStack(stack);
                    if (data.pos.isPresent()) {
                        var level = Cached.getLevelByName(server.getAllLevels(), data.pos.get().dimension().location().toString());
                        if (level == null) {
                            continue;
                        }

                        var brazier = getBrazier(level, null, stack);
                        if (brazier == null) {
                            continue;
                        }

                        rituals.put(brazier.ritual, player);
                    }
                }
            }
        }

        return rituals;
    }

    AbstractRitual cachedRitual;

    public PortableBrazierRelayItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var data = stack.get(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
        if (data != null && !data.ritualName.isEmpty()) {
            return Component.translatable("item.ars_controle.portable_brazier_relay.with_ritual", Component.translatable(data.ritualName));
        }
        return Component.translatable("item.ars_controle.portable_brazier_relay");
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return super.useOn(context);
        }

        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return super.useOn(context);
        }

        var pos = context.getClickedPos();

        var be = level.getBlockEntity(pos);
        if (!(be instanceof RitualBrazierTile brazier)) {
            return super.useOn(context);
        }

        var stack = context.getItemInHand();
        var relayData = PortableBrazierRelayData.fromItemStack(stack);

        var currentBrazier = getBrazier(level, player, stack);
        if (currentBrazier != null) {
            currentBrazier.removeData(AttachmentRegistry.RELAY_UUID);
            currentBrazier.removeData(AttachmentRegistry.ASSOCIATION);
            PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(currentBrazier.getBlockPos()), new PacketSyncAssociation(relayData.pos.get(), null));
        }

        var existingData = be.getExistingData(AttachmentRegistry.RELAY_UUID);
        if (existingData.isPresent() && relayData.uuid.isPresent() && existingData.get().equals(relayData.uuid.get())) {
            stack.remove(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
            brazier.removeData(AttachmentRegistry.RELAY_UUID);
            brazier.removeData(AttachmentRegistry.ASSOCIATION);
            PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(brazier.getBlockPos()), new PacketSyncAssociation(new GlobalPos(level.dimension(), pos), null));

            PortUtil.sendMessageNoSpam(player, Component.translatable("ars_controle.target.set.none"));
        } else {
            var data = PortableBrazierRelayData.of(new GlobalPos(level.dimension(), pos), brazier.ritual);
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            var uuid = data.uuid.get();
            data.write(stack);
            brazier.setData(AttachmentRegistry.RELAY_UUID, uuid);
            brazier.setData(AttachmentRegistry.ASSOCIATION, player.getUUID());
            PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(brazier.getBlockPos()), new PacketSyncAssociation(new GlobalPos(level.dimension(), pos), player.getUUID()));

            PortUtil.sendMessageNoSpam(player, Component.translatable("ars_controle.target.set.block", pos.toShortString(), level.dimension().location().toString()));
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameTime() % 20 == 0) {
            var brazier = getBrazier(serverLevel, entity, stack);
            if (brazier == null) {
                return;
            }

            if (brazier.isRitualDone()) {
                return;
            }

            var regName = brazier.ritual.getRegistryName();
            var data = stack.get(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
            if (data != null) {
                var ritualName = "item." + regName.getNamespace() + "." + regName.getPath();
                if (!data.ritualName.equals(ritualName)) {
                    stack.set(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA, PortableBrazierRelayData.of(data.pos().get(), ritualName));
                }

                var brazierAssoc = brazier.getExistingData(AttachmentRegistry.ASSOCIATION);
                if (brazierAssoc.isEmpty() || !brazierAssoc.get().equals(entity.getUUID())) {
                    brazier.setData(AttachmentRegistry.ASSOCIATION, entity.getUUID());
                    PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(brazier.getBlockPos()), new PacketSyncAssociation(new GlobalPos(serverLevel.dimension(), brazier.getBlockPos()), entity.getUUID()));
                }
            }

            this.cachedRitual = brazier.ritual;
        }

        var ritual = this.cachedRitual;
        if (ritual == null || !ritual.isRunning()) {
            return;
        }

        ((AbstractRitualInvoker) ritual).invokeTick();
    }

    public static RitualBrazierTile getBrazier(ServerLevel level, @Nullable Entity entity, ItemStack stack) {
        var data = PortableBrazierRelayData.fromItemStack(stack);
        if (data.pos.isEmpty() || data.uuid.isEmpty()) {
            return null;
        }

        var target = data.pos.get();
        var targetDim = target.dimension();
        var targetPos = target.pos();

        var targetLevel = Cached.getLevelByName(level.getServer().getAllLevels(), targetDim.location().toString());
        if (targetLevel == null) {
            PortUtil.sendMessageNoSpam(entity, Component.translatable("ars_controle.remote.error.invalid_dimension"));
            stack.remove(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
            return null;
        }

        if (!targetLevel.isLoaded(targetPos)) {
            return null;
        }

        var be = targetLevel.getBlockEntity(targetPos);
        if (!(be instanceof RitualBrazierTile brazier)) {
            PortUtil.sendMessageNoSpam(entity, Component.translatable("ars_controle.remote.error.invalid_target"));
            stack.remove(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
            return null;
        }

        if (brazier.ritual == null || (entity != null && !brazier.getExistingData(AttachmentRegistry.RELAY_UUID).map(u -> data.uuid.get().equals(u)).orElse(false))) {
            stack.remove(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA);
            return null;
        }

        return brazier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip2, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip2, flagIn);

        var data = PortableBrazierRelayData.fromItemStack(stack);
        if (data.pos.isEmpty()) {
            tooltip2.add(Component.translatable("ars_controle.target.get.none"));
        } else {
            var pos = data.pos.get();
            tooltip2.add(Component.translatable("ars_controle.target.get.block", pos.pos().toShortString(), pos.dimension().location().toString()));
        }
    }

    public record PortableBrazierRelayData(@NotNull Optional<GlobalPos> pos, @NotNull Optional<UUID> uuid, String ritualName) {
        public static final Codec<PortableBrazierRelayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.optionalFieldOf("pos").forGetter(PortableBrazierRelayData::pos),
                UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(PortableBrazierRelayData::uuid),
                Codec.STRING.fieldOf("target_name").forGetter(PortableBrazierRelayData::ritualName)
        ).apply(instance, PortableBrazierRelayData::new));

        public static final StreamCodec<FriendlyByteBuf, PortableBrazierRelayData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), PortableBrazierRelayData::pos,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), PortableBrazierRelayData::uuid,
                ByteBufCodecs.STRING_UTF8, PortableBrazierRelayData::ritualName,
                PortableBrazierRelayData::new
        );

        public static PortableBrazierRelayData empty() {
            return new PortableBrazierRelayData(Optional.empty(), Optional.empty(), "");
        }

        public static PortableBrazierRelayData of(GlobalPos pos, UUID uuid, String ritualName) {
            return new PortableBrazierRelayData(Optional.of(pos), Optional.of(uuid), ritualName);
        }

        public static PortableBrazierRelayData of(GlobalPos pos, UUID uuid, AbstractRitual ritual) {
            var regName = ritual.getRegistryName();
            return PortableBrazierRelayData.of(pos, uuid, "item." + regName.getNamespace() + "." + regName.getPath());
        }

        public static PortableBrazierRelayData of(GlobalPos pos, String ritualName) {
            return PortableBrazierRelayData.of(pos, UUID.randomUUID(), ritualName);
        }

        public static PortableBrazierRelayData of(GlobalPos pos, AbstractRitual ritual) {
            return PortableBrazierRelayData.of(pos, UUID.randomUUID(), ritual);
        }

        public static PortableBrazierRelayData fromItemStack(@NotNull ItemStack stack) {
            return stack.getOrDefault(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA.get(), PortableBrazierRelayData.empty());
        }

        public PortableBrazierRelayData write(@NotNull ItemStack stack) {
            return stack.set(ModRegistry.PORTABLE_BRAZIER_RELAY_DATA, this);
        }
    }
}
