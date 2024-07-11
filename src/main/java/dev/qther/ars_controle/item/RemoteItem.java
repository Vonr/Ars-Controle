package dev.qther.ars_controle.item;

import com.hollingsworth.arsnouveau.common.block.tile.StorageLecternTile;
import com.hollingsworth.arsnouveau.common.items.ModItem;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.qther.ars_controle.registry.ModRegistry;
import dev.qther.ars_controle.tile.WarpingSpellPrismTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class RemoteItem extends ModItem {
    public RemoteItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null && tag.contains("targetName")) {
            return Component.translatable("item.ars_controle.remote.with_target", Component.translatable(tag.getString("targetName")));
        }
        return Component.translatable("item.ars_controle.remote");
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getLevel().isClientSide() || ctx.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var player = ctx.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        var stack = ctx.getItemInHand();

        var tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        var blockPos = ctx.getClickedPos();
        var level = player.level();
        if (!tag.contains("target", 99)) {
            var block = level.getBlockState(blockPos);
            if (!block.hasBlockEntity()) {
                player.sendSystemMessage(Component.translatable("ars_controle.target.get.none"));
                return InteractionResult.FAIL;
            }
            tag.putLong("target", blockPos.asLong());
            tag.putString("dimension", level.dimension().location().toString());
            player.sendSystemMessage(Component.translatable("ars_controle.remote.set_target", blockPos.toShortString(), level.dimension().location().toString()));
            tag.putString("targetName", block.getBlock().getDescriptionId());
            return InteractionResult.SUCCESS;
        }

        var targetPos = BlockPos.of(tag.getLong("target"));
        var targetDim = tag.getString("dimension");

        if (level.getBlockState(targetPos).is(BlockRegistry.CRAFTING_LECTERN.get())) {
            if (!level.dimension().location().toString().equals(targetDim)) {
                player.sendSystemMessage(Component.translatable("ars_controle.remote.error.different_dimension"));
                return InteractionResult.FAIL;
            }

            var _tile = level.getBlockEntity(targetPos);
            if (_tile == null) {
                player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                return InteractionResult.FAIL;
            }

            if (!(_tile instanceof StorageLecternTile tile)) {
                player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                return InteractionResult.FAIL;
            }

            tile.onFinishedConnectionLast(blockPos, ctx.getClickedFace(), null, player);
            return InteractionResult.FAIL;
        }

        for (var l : level.getServer().getAllLevels()) {
            if (!targetDim.equals(l.dimension().location().toString())) {
                continue;
            }

            var b = l.getBlockState(targetPos);
            if (b.is(ModRegistry.WARPING_SPELL_PRISM_BLOCK.get())) {
                var _tile = l.getBlockEntity(targetPos);
                if (_tile == null) {
                    player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }
                if (!(_tile instanceof WarpingSpellPrismTile tile)) {
                    player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }

                tile.setBlock(level.dimension(), blockPos);

                player.sendSystemMessage(Component.translatable("ars_controle.target.set.block", blockPos.toShortString(), level.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }
        }

        player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));

        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        var level = player.level();
        if (level.isClientSide() || hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        var tag = stack.getTag();
        if (tag == null) {
            tag = new CompoundTag();
            stack.setTag(tag);
        }

        if (!tag.contains("target", 99)) {
            player.sendSystemMessage(Component.translatable("ars_controle.target.get.none"));
            return InteractionResult.FAIL;
        }

        var targetPos = BlockPos.of(tag.getLong("target"));
        var targetDim = tag.getString("dimension");

        for (var l : level.getServer().getAllLevels()) {
            if (!targetDim.equals(l.dimension().location().toString())) {
                continue;
            }

            var b = l.getBlockState(targetPos);
            if (b.is(ModRegistry.WARPING_SPELL_PRISM_BLOCK.get())) {
                var _tile = l.getBlockEntity(targetPos);
                if (_tile == null) {
                    player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }
                if (!(_tile instanceof WarpingSpellPrismTile tile)) {
                    player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));
                    return InteractionResult.FAIL;
                }

                tile.setEntityUUID(entity.getUUID());

                player.sendSystemMessage(Component.translatable("ars_controle.target.set.entity", entity.getDisplayName().getString(), level.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }
        }

        player.sendSystemMessage(Component.translatable("ars_controle.remote.error.invalid_target"));

        return InteractionResult.FAIL;
    }
}
