package dev.qther.ars_controle.item;

import com.hollingsworth.arsnouveau.api.item.IRadialProvider;
import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.api.util.ANEventBus;
import com.hollingsworth.arsnouveau.client.gui.radial_menu.GuiRadialMenu;
import com.hollingsworth.arsnouveau.client.gui.radial_menu.RadialMenu;
import com.hollingsworth.arsnouveau.client.gui.radial_menu.RadialMenuSlot;
import com.hollingsworth.arsnouveau.client.gui.utils.RenderUtils;
import com.hollingsworth.arsnouveau.common.items.ModItem;
import com.hollingsworth.arsnouveau.common.network.HighlightAreaPacket;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.qther.ars_controle.block.tile.IDimensionalHighlighter;
import dev.qther.ars_controle.packets.serverbound.PacketSetRemoteLockMode;
import dev.qther.ars_controle.packets.serverbound.PacketSetRemoteSelectionMode;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.Cached;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RemoteItem extends ModItem implements IRadialProvider {
    public RemoteItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var data = stack.get(ACRegistry.Components.REMOTE);
        if (data != null && data.targetName.getContents() != PlainTextContents.EMPTY) {
            return Component.translatable("item.ars_controle.remote.with_target", data.targetName);
        }
        return Component.translatable("item.ars_controle.remote");
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        var level = ctx.getLevel();
        if (ctx.getLevel().isClientSide() || ctx.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var player = ctx.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        var stack = ctx.getItemInHand();

        var blockPos = ctx.getClickedPos();
        var state = level.getBlockState(blockPos);
        var block = state.getBlock();

        if (ANEventBus.post(new BlockEvent.BreakEvent(level, blockPos, state, player))) {
            return InteractionResult.FAIL;
        }

        var data = RemoteData.fromItemStack(stack);
        if (data.isEmpty()) {
            if (player.isShiftKeyDown()) {
                RemoteData.fromBlock(block, GlobalPos.of(level.dimension(), blockPos), data.lockedFirst, data.multiple, data.firstCorner.orElse(null)).write(stack);
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.set_target", blockPos.toShortString(), level.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }

            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.none"));
            return InteractionResult.FAIL;
        }

        if (data.block.isPresent()) {
            var globalPos = data.block.get();
            var targetPos = globalPos.pos();
            var targetDim = globalPos.dimension();

            var targetLevel = Cached.getLevelByKey(targetDim);
            if (targetLevel == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return InteractionResult.FAIL;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                if (data.multiple) {
                    if (data.firstCorner.isEmpty() || !data.firstCorner.get().dimension().equals(level.dimension())) {
                        data.withFirstCorner(new GlobalPos(level.dimension(), blockPos)).write(stack);
                        return InteractionResult.SUCCESS;
                    }

                    for (var pos : BlockPos.betweenClosed(data.firstCorner.get().pos(), blockPos)) {
                        if (data.lockedFirst) {
                            wandable.onFirstConnection(new GlobalPos(level.dimension(), pos), ctx.getClickedFace(), null, player);
                        } else {
                            wandable.onLastConnection(new GlobalPos(level.dimension(), pos), ctx.getClickedFace(), null, player);
                        }
                    }

                    data.withFirstCorner(null).write(stack);
                } else {
                    if (data.lockedFirst) {
                        wandable.onFirstConnection(new GlobalPos(level.dimension(), blockPos), ctx.getClickedFace(), null, player);
                    } else {
                        wandable.onLastConnection(new GlobalPos(level.dimension(), blockPos), ctx.getClickedFace(), null, player);
                    }
                }

                return InteractionResult.CONSUME;
            } else {
                if (data.multiple) {
                    if (data.firstCorner.isEmpty() || !data.firstCorner.get().dimension().equals(level.dimension())) {
                        data.withFirstCorner(new GlobalPos(level.dimension(), blockPos)).write(stack);
                        return InteractionResult.SUCCESS;
                    }

                    for (var pos : BlockPos.betweenClosed(data.firstCorner.get().pos(), blockPos)) {
                        if (level.getBlockEntity(pos) instanceof IWandable wandable) {
                            if (data.lockedFirst) {
                                wandable.onLastConnection(globalPos, ctx.getClickedFace(), null, player);
                            } else {
                                wandable.onFirstConnection(globalPos, ctx.getClickedFace(), null, player);
                            }
                        }
                    }

                    data.withFirstCorner(null).write(stack);
                } else {
                    if (level.getBlockEntity(blockPos) instanceof IWandable wandable) {
                        if (data.lockedFirst) {
                            wandable.onLastConnection(globalPos, ctx.getClickedFace(), null, player);
                        } else {
                            wandable.onFirstConnection(globalPos, ctx.getClickedFace(), null, player);
                        }
                    }
                }

                return InteractionResult.CONSUME;
            }
        } else if (data.entity.isPresent()) {
            var targetEntity = Cached.getEntityByUUID(data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                if (data.multiple) {
                    if (data.firstCorner.isEmpty() || !data.firstCorner.get().dimension().equals(level.dimension())) {
                        data.withFirstCorner(new GlobalPos(level.dimension(), blockPos)).write(stack);
                        return InteractionResult.SUCCESS;
                    }

                    for (var pos : BlockPos.betweenClosed(data.firstCorner.get().pos(), blockPos)) {
                        if (data.lockedFirst) {
                            wandable.onFirstConnection(new GlobalPos(level.dimension(), pos), ctx.getClickedFace(), null, player);
                        } else {
                            wandable.onLastConnection(new GlobalPos(level.dimension(), pos), ctx.getClickedFace(), null, player);
                        }
                    }

                    data.withFirstCorner(null).write(stack);
                } else {
                    if (data.lockedFirst) {
                        wandable.onFirstConnection(new GlobalPos(level.dimension(), blockPos), ctx.getClickedFace(), null, player);
                    } else {
                        wandable.onLastConnection(new GlobalPos(level.dimension(), blockPos), ctx.getClickedFace(), null, player);
                    }
                }

                return InteractionResult.CONSUME;
            } else if (targetEntity instanceof LivingEntity le) {
                if (data.multiple) {
                    if (data.firstCorner.isEmpty() || !data.firstCorner.get().dimension().equals(level.dimension())) {
                        data.withFirstCorner(new GlobalPos(level.dimension(), blockPos)).write(stack);
                        return InteractionResult.SUCCESS;
                    }

                    for (var pos : BlockPos.betweenClosed(data.firstCorner.get().pos(), blockPos)) {
                        if (level.getBlockEntity(pos) instanceof IWandable wandable) {
                            if (data.lockedFirst) {
                                wandable.onLastConnection(null, ctx.getClickedFace(), le, player);
                            } else {
                                wandable.onFirstConnection(null, ctx.getClickedFace(), le, player);
                            }
                        }
                    }

                    data.withFirstCorner(null).write(stack);
                } else {
                    if (level.getBlockEntity(blockPos) instanceof IWandable wandable) {
                        if (data.lockedFirst) {
                            wandable.onLastConnection(null, ctx.getClickedFace(), le, player);
                        } else {
                            wandable.onFirstConnection(null, ctx.getClickedFace(), le, player);
                        }
                    }
                }

                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack _stack, Player player, LivingEntity entity, InteractionHand hand) {
        var level = player.level();
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var stack = player.getItemInHand(hand);
        var data = RemoteData.fromItemStack(stack);
        if (data.isEmpty()) {
            if (player.isShiftKeyDown()) {
                if (entity.isAlive()) {
                    RemoteData.fromEntity(entity, data.lockedFirst, data.multiple, data.firstCorner.orElse(null)).write(stack);
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.set_target", entity.getName(), level.dimension().location().toString()));

                    return InteractionResult.CONSUME;
                }

                return InteractionResult.PASS;
            }

            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.none"));
            return InteractionResult.FAIL;
        }

        if (data.block.isPresent()) {
            var globalPos = data.block.get();
            var targetPos = globalPos.pos();
            var targetDim = globalPos.dimension();

            var targetLevel = Cached.getLevelByKey(targetDim);
            if (targetLevel == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return InteractionResult.FAIL;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                if (data.lockedFirst) {
                    wandable.onLastConnection(null, null, entity, player);
                } else {
                    wandable.onFirstConnection(null, null, entity, player);
                }
                return InteractionResult.CONSUME;
            } else if (entity instanceof IWandable wandable) {
                if (data.lockedFirst) {
                    wandable.onFirstConnection(globalPos, null, null, player);
                } else {
                    wandable.onLastConnection(globalPos, null, null, player);
                }
                return InteractionResult.CONSUME;
            }
        } else if (data.entity.isPresent()) {
            var targetEntity = Cached.getEntityByUUID(data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                if (data.lockedFirst) {
                    wandable.onLastConnection(null, null, entity, player);
                } else {
                    wandable.onFirstConnection(null, null, entity, player);
                }
                return InteractionResult.CONSUME;
            } else if (targetEntity instanceof LivingEntity le && entity instanceof IWandable wandable) {
                if (data.lockedFirst) {
                    wandable.onFirstConnection(null, null, le, player);
                } else {
                    wandable.onLastConnection(null, null, le, player);
                }
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip2, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip2, flagIn);
        var data = RemoteData.fromItemStack(stack);
        tooltip2.add(Component.translatable("ars_controle.remote.lock_mode.tooltip", data.lockedFirst ? Component.translatable("ars_controle.remote.lock_mode.first") : Component.translatable("ars_controle.remote.lock_mode.last")));
        tooltip2.add(Component.translatable("ars_controle.remote.selection_mode.tooltip", data.multiple ? Component.translatable("ars_controle.remote.selection_mode.multiple") : Component.translatable("ars_controle.remote.selection_mode.single")));
    }

    @Override
    public void onRadialKeyPressed(ItemStack stack, Player player) {
        RadialMenu<String> menu;

        if (player.isShiftKeyDown()) {
            menu = new RadialMenu<>(
                    slot -> Networking.sendToServer(new PacketSetRemoteLockMode(LockModeSlot.VALUES[slot])),
                    List.of(LockModeSlot.LOCKED_FIRST.asSlot(), LockModeSlot.LOCKED_LAST.asSlot()),
                    RenderUtils::drawString,
                    0
            );
        } else {
            menu = new RadialMenu<>(
                    slot -> Networking.sendToServer(new PacketSetRemoteSelectionMode(SelectionModeSlot.VALUES[slot])),
                    List.of(SelectionModeSlot.SINGLE.asSlot(), SelectionModeSlot.MULTIPLE.asSlot()),
                    RenderUtils::drawString,
                    0
            );
        }

        Client.setScreen(menu);
    }

    private static class Client {
        public static void setScreen(RadialMenu<String> menu) {
            Minecraft.getInstance().setScreen(new GuiRadialMenu<>(menu));
        }
    }

    public enum LockModeSlot {
        LOCKED_FIRST("ars_controle.remote.lock_mode.first"),
        LOCKED_LAST("ars_controle.remote.lock_mode.last");

        public static final LockModeSlot[] VALUES = values();

        public final String key;

        LockModeSlot(String key) {
            this.key = key;
        }

        public Component translatable() {
            return Component.translatable("ars_controle.remote.lock_mode.radial", Component.translatable(key));
        }

        public RadialMenuSlot<String> asSlot() {
            return new RadialMenuSlot<>(this.translatable().getString(), this.key);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level pLevel, @NotNull Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(stack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pIsSelected || pLevel.isClientSide || pLevel.getGameTime() % 5 != 0 || !(pEntity instanceof ServerPlayer player)) {
            return;
        }
        var data = RemoteData.fromItemStack(stack);

        Object highlighter = null;

        if (data.block.isPresent()) {
            var level = Cached.getLevelByKey(data.block.get().dimension());
            if (level != null) {
                highlighter = level.getBlockEntity(data.block.get().pos());
            }
        } else if (data.entity.isPresent()) {
            if (player.getServer() != null) {
                highlighter = Cached.getEntityByUUID(data.entity.get());
            }
        }

        if (highlighter != null) {
            if (highlighter instanceof IDimensionalHighlighter dim) {
                Networking.sendToPlayerClient(new HighlightAreaPacket(dim.getWandHighlight(pLevel, new ArrayList<>()), 10), player);
            } else if (highlighter instanceof IWandable wandable) {
                Networking.sendToPlayerClient(new HighlightAreaPacket(wandable.getWandHighlight(new ArrayList<>()), 10), player);
            }
        }
    }

    public enum SelectionModeSlot {
        SINGLE("ars_controle.remote.selection_mode.single"),
        MULTIPLE("ars_controle.remote.selection_mode.multiple");

        public static final SelectionModeSlot[] VALUES = values();

        public final String key;

        SelectionModeSlot(String key) {
            this.key = key;
        }

        public Component translatable() {
            return Component.translatable(key);
        }

        public RadialMenuSlot<String> asSlot() {
            return new RadialMenuSlot<>(this.translatable().getString(), this.key);
        }
    }

    public record RemoteData(@NotNull Optional<GlobalPos> block, @NotNull Optional<UUID> entity,
                             boolean lockedFirst,
                             boolean multiple, @NotNull Optional<GlobalPos> firstCorner,
                             @NotNull Component targetName) {
        public static final Codec<RemoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.optionalFieldOf("block").forGetter(RemoteData::block),
                UUIDUtil.CODEC.optionalFieldOf("entity").forGetter(RemoteData::entity),
                Codec.BOOL.fieldOf("locked_first").forGetter(RemoteData::lockedFirst),
                Codec.BOOL.fieldOf("multiple").forGetter(RemoteData::multiple),
                GlobalPos.CODEC.optionalFieldOf("first_corner").forGetter(RemoteData::firstCorner),
                ComponentSerialization.CODEC.fieldOf("target_name").forGetter(RemoteData::targetName)
        ).apply(instance, RemoteData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, RemoteData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), RemoteData::block,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), RemoteData::entity,
                ByteBufCodecs.BOOL, RemoteData::lockedFirst,
                ByteBufCodecs.BOOL, RemoteData::multiple,
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), RemoteData::firstCorner,
                ComponentSerialization.STREAM_CODEC, RemoteData::targetName,
                RemoteData::new
        );

        public static RemoteData empty() {
            return new RemoteData(Optional.empty(), Optional.empty(), true, false, Optional.empty(), Component.empty());
        }

        public static RemoteData fromItemStack(@NotNull ItemStack stack) {
            return stack.getOrDefault(ACRegistry.Components.REMOTE, RemoteData.empty());
        }

        public static RemoteData fromBlock(@NotNull Block block, @NotNull GlobalPos pos, boolean lockedFirst, boolean multiple, @Nullable GlobalPos firstCorner) {
            return new RemoteData(Optional.of(pos), Optional.empty(), lockedFirst, multiple, Optional.ofNullable(firstCorner), block.getName());
        }

        public static RemoteData fromEntity(@NotNull Entity entity, boolean lockedFirst, boolean multiple, @Nullable GlobalPos firstCorner) {
            return new RemoteData(Optional.empty(), Optional.of(entity.getUUID()), lockedFirst, multiple, Optional.ofNullable(firstCorner), entity.getName());
        }

        public RemoteData withBlock(@Nullable GlobalPos pos) {
            return new RemoteData(Optional.ofNullable(pos), this.entity, this.lockedFirst, this.multiple, this.firstCorner, this.targetName);
        }

        public RemoteData withEntity(@Nullable Entity entity) {
            return new RemoteData(this.block, Optional.ofNullable(entity).map(Entity::getUUID), this.lockedFirst, this.multiple, this.firstCorner, this.targetName);
        }

        public RemoteData withLockingMode(LockModeSlot slot) {
            return new RemoteData(this.block, this.entity, slot == LockModeSlot.LOCKED_FIRST, this.multiple, this.firstCorner, this.targetName);
        }

        public RemoteData withSelectionMode(SelectionModeSlot slot) {
            return new RemoteData(this.block, this.entity, this.lockedFirst, slot == SelectionModeSlot.MULTIPLE, this.firstCorner, this.targetName);
        }

        public RemoteData withFirstCorner(@Nullable GlobalPos corner) {
            return new RemoteData(this.block, this.entity, this.lockedFirst, this.multiple, Optional.ofNullable(corner), this.targetName);
        }

        public RemoteData cleared() {
            return new RemoteData(Optional.empty(), Optional.empty(), this.lockedFirst, this.multiple, Optional.empty(), Component.empty());
        }

        public boolean isEmpty() {
            return this.block.isEmpty() && this.entity.isEmpty();
        }

        public RemoteData write(@NotNull ItemStack stack) {
            return stack.set(ACRegistry.Components.REMOTE, this);
        }
    }
}
