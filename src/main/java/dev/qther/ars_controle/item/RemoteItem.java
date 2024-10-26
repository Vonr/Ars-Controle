package dev.qther.ars_controle.item;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.common.items.ModItem;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.qther.ars_controle.Cached;
import dev.qther.ars_controle.registry.ModRegistry;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class RemoteItem extends ModItem {
    public RemoteItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var data = stack.get(ModRegistry.REMOTE_DATA);
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
        var block = level.getBlockState(blockPos).getBlock();

        var data = RemoteData.fromItemStack(stack);
        if (data.isEmpty()) {
            if (player.isCrouching()) {
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

        if (data.block.isPresent()) {
            var globalPos = data.block.get();
            var targetPos = globalPos.pos();
            var targetDim = globalPos.dimension();

            var targetLevel = Cached.getLevelByName(level.getServer().getAllLevels(), targetDim.location().toString());
            if (targetLevel == null) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_dimension"));
                return InteractionResult.FAIL;
            }

            var targetBlock = targetLevel.getBlockState(targetPos);
            if (targetBlock.is(ModRegistry.WARPING_SPELL_PRISM_BLOCK.get())) {
                var _tile = targetLevel.getBlockEntity(targetPos);
                if (_tile == null) {
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }
                if (!(_tile instanceof WarpingSpellPrismTile tile)) {
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }

                tile.setBlock(level.dimension(), blockPos);

                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", blockPos.toShortString(), level.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }

            if (!level.dimension().equals(data.block.get().dimension())) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.different_dimension"));
                return InteractionResult.SUCCESS;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(blockPos, null, null, player);
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", data.block.get().pos().toShortString(), data.block.get().dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }
        } else if (data.entity.isPresent()) {
            var server = level.getServer();
            var targetEntity = Cached.getEntityByUUID(server.getAllLevels(), data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(blockPos, null, null, player);
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", blockPos.toShortString(), level.dimension().location().toString()));
                return InteractionResult.SUCCESS;
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
            if (player.isCrouching()) {
                if (entity.isAlive() && entity instanceof IWandable) {
                    RemoteData.fromEntity(entity).write(stack);
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.set_target", entity.getDisplayName(), level.dimension().location().toString()));

                    return InteractionResult.SUCCESS;
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

            var targetBlock = targetLevel.getBlockState(targetPos);
            if (targetBlock.is(ModRegistry.WARPING_SPELL_PRISM_BLOCK.get())) {
                var _tile = targetLevel.getBlockEntity(targetPos);
                if (_tile == null) {
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }
                if (!(_tile instanceof WarpingSpellPrismTile tile)) {
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }

                tile.setEntityUUID(entity.getUUID());

                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", entity.getDisplayName().getString(), level.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }

            if (!level.dimension().equals(data.block.get().dimension())) {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.error.different_dimension"));
                return InteractionResult.FAIL;
            }

            var tile = targetLevel.getBlockEntity(targetPos);
            if (tile instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(null, null, entity, player);
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.block", data.block.get().pos().toShortString(), data.block.get().dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }
        } else if (data.entity.isPresent()) {
            var server = level.getServer();
            var targetEntity = Cached.getEntityByUUID(server.getAllLevels(), data.entity.get());
            if (targetEntity instanceof IWandable wandable) {
                wandable.onFinishedConnectionLast(null, null, entity, player);
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.entity", entity.getDisplayName().getString(), level.dimension().location().toString()));
                return InteractionResult.SUCCESS;
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
            return stack.getOrDefault(ModRegistry.REMOTE_DATA.get(), RemoteData.empty());
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
            return stack.set(ModRegistry.REMOTE_DATA, this);
        }
    }
}
