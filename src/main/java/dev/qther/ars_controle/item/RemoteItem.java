package dev.qther.ars_controle.item;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.api.util.ANEventBus;
import com.hollingsworth.arsnouveau.common.items.ModItem;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.Cached;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class RemoteItem extends ModItem {
    public RemoteItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var data = stack.get(ACRegistry.Components.REMOTE);
        if (data != null && !data.targetName.isEmpty()) {
            return Component.translatable("item.ars_controle.remote.with_target", Component.translatable(data.targetName));
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
                if (level.getBlockEntity(blockPos) instanceof IWandable) {
                    RemoteData.fromBlock(block, GlobalPos.of(level.dimension(), blockPos)).write(stack);
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.set_target", blockPos.toShortString(), level.dimension().location().toString()));

                    return InteractionResult.SUCCESS;
                }

                return InteractionResult.PASS;
            }

            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.none"));
            return InteractionResult.FAIL;
        }

        var server = level.getServer();
        if (data.block.isPresent()) {
            var globalPos = data.block.get();
            var targetPos = globalPos.pos();
            var targetDim = globalPos.dimension();

            var targetLevel = Cached.getLevelByName(server.getAllLevels(), targetDim.location().toString());
            if (targetLevel == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return InteractionResult.FAIL;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(new GlobalPos(level.dimension(), blockPos), null, null, player);
                return InteractionResult.CONSUME;
            }
        } else if (data.entity.isPresent()) {
            var targetEntity = Cached.getEntityByUUID(server.getAllLevels(), data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(new GlobalPos(level.dimension(), blockPos), null, null, player);
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
                if (entity.isAlive() && entity instanceof IWandable) {
                    RemoteData.fromEntity(entity).write(stack);
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.set_target", entity.getDisplayName(), level.dimension().location().toString()));

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

            var targetLevel = Cached.getLevelByName(level.getServer().getAllLevels(), targetDim.location().toString());
            if (targetLevel == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return InteractionResult.FAIL;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast((GlobalPos) null, null, entity, player);
                return InteractionResult.CONSUME;
            }
        } else if (data.entity.isPresent()) {
            var server = level.getServer();
            var targetEntity = Cached.getEntityByUUID(server.getAllLevels(), data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast((GlobalPos) null, null, entity, player);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    public record RemoteData(@NotNull Optional<GlobalPos> block, @NotNull Optional<UUID> entity,
                             @NotNull String targetName) {
        public static final Codec<RemoteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.optionalFieldOf("block").forGetter(RemoteData::block),
                UUIDUtil.CODEC.optionalFieldOf("entity").forGetter(RemoteData::entity),
                Codec.STRING.fieldOf("target_name").forGetter(RemoteData::targetName)
        ).apply(instance, RemoteData::new));

        public static final StreamCodec<FriendlyByteBuf, RemoteData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(GlobalPos.STREAM_CODEC), RemoteData::block,
                ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), RemoteData::entity,
                ByteBufCodecs.STRING_UTF8, RemoteData::targetName,
                RemoteData::new
        );

        public static RemoteData empty() {
            return new RemoteData(Optional.empty(), Optional.empty(), "");
        }

        public static RemoteData fromItemStack(@NotNull ItemStack stack) {
            return stack.getOrDefault(ACRegistry.Components.REMOTE.get(), RemoteData.empty());
        }

        public static RemoteData fromBlock(@NotNull Block block, @NotNull GlobalPos pos) {
            return new RemoteData(Optional.of(pos), Optional.empty(), block.getDescriptionId());
        }

        public static RemoteData fromEntity(@NotNull Entity entity) {
            return new RemoteData(Optional.empty(), Optional.of(entity.getUUID()), entity.getType().getDescriptionId());
        }

        public boolean isEmpty() {
            return this.block.isEmpty() && this.entity.isEmpty();
        }

        public RemoteData write(@NotNull ItemStack stack) {
            return stack.set(ACRegistry.Components.REMOTE, this);
        }
    }
}
