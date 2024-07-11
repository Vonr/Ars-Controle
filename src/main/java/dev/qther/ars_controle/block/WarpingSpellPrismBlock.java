package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.api.block.IPrismaticBlock;
import com.hollingsworth.arsnouveau.api.event.SpellProjectileHitEvent;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.advancement.ANCriteriaTriggers;
import com.hollingsworth.arsnouveau.common.block.ModBlock;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.items.DominionWand;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDecelerate;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.tile.WarpingSpellPrismTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

public class WarpingSpellPrismBlock extends ModBlock implements IPrismaticBlock, EntityBlock {
    public WarpingSpellPrismBlock(Properties properties) {
        super(properties);
    }

    public WarpingSpellPrismBlock() {
        super();
    }

    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand handIn, @NotNull BlockHitResult hit) {
        if (handIn != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }

        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!state.canEntityDestroy(worldIn, pos, player)) {
            return InteractionResult.FAIL;
        }

        var te = worldIn.getBlockEntity(pos);
        if (te == null) {
            ArsControle.LOGGER.warn("No tile entity in scrying spell prism.");
            return InteractionResult.FAIL;
        }

        if (!(te instanceof WarpingSpellPrismTile tile)) {
            ArsControle.LOGGER.warn("Wrong tile entity in scrying spell prism.");
            return InteractionResult.FAIL;
        }

        var stack = player.getItemInHand(handIn);
        if (stack.isEmpty()) {
            if (player.isCrouching()) {
                tile.setEntityUUID(player.getUUID());
                tile.setChanged();
                player.sendSystemMessage(Component.translatable("ars_controle.target.set.self"));
                return InteractionResult.SUCCESS;
            }

            var hitResult = tile.getHitResult();
            if (hitResult == null) {
                player.sendSystemMessage(Component.translatable("ars_controle.target.get.none"));
                return InteractionResult.SUCCESS;
            }

            if (hitResult instanceof EntityHitResult e) {
                var entity = e.getEntity();
                player.sendSystemMessage(Component.translatable("ars_controle.target.get.entity", entity.getDisplayName(), entity.level().dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }

            if (hitResult instanceof BlockHitResult b) {
                var dim = tile.getTargetLevel();
                player.sendSystemMessage(Component.translatable("ars_controle.target.get.block", b.getBlockPos().toShortString(), dim == null ? "<invalid>" : dim.dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }
        }

        var item = stack.getItem();

        if (item instanceof DominionWand) {
            var data = new DominionWand.DominionData(stack);
            var entity = data.getEntity(player.level());
            if (entity != null) {
                tile.setEntityUUID(entity.getUUID());
                tile.setChanged();
                data.setStoredEntityID(-1);
                player.sendSystemMessage(Component.translatable("ars_controle.target.set.entity", entity.getDisplayName(), entity.level().dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }

            var blockPos = data.getStoredPos();
            if (blockPos != null) {
                tile.setBlock(worldIn.dimension(), blockPos);
                data.setStoredPos(null);
                data.setFacing(null);
                tile.setChanged();
                player.sendSystemMessage(Component.translatable("ars_controle.target.set.block", blockPos.toShortString(), worldIn.dimension().location().toString()));

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void onHit(ServerLevel world, BlockPos pos, @NotNull EntityProjectileSpell spell) {
        if (spell.spellResolver == null) {
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        WarpingSpellPrismTile tile = new BlockSourceImpl(world, pos).getEntity();
        var hit = tile.getHitResult();
        if (hit == null) {
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        if (hit instanceof BlockHitResult b) {
            var dim = tile.getTargetLevel();
            if (dim == null) {
                world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
                spell.remove(RemovalReason.DISCARDED);
                return;
            }

            if (spell.level() != dim) {
                var newSpell = changeSpellLevel(dim, spell);
                if (newSpell != null) {
                    spell = newSpell;
                }
            }

            hit = b.withDirection(spell.getDirection());
        }

        if (hit instanceof EntityHitResult e) {
            var dim = (ServerLevel) e.getEntity().level();
            if (spell.level() != dim) {
                var newSpell = changeSpellLevel(dim, spell);
                if (newSpell != null) {
                    spell = newSpell;
                }
            }
        }

        var oldPos = spell.position();
        var newPos = hit.getLocation();
        if (!newPos.equals(oldPos)) {
            spell.setPos(newPos);

            if (++spell.prismRedirect >= 3) {
                ANCriteriaTriggers.rewardNearbyPlayers(ANCriteriaTriggers.PRISMATIC, world, pos, 10);
            }

            int manaCost = tile.getSourceRequired(hit);
            if (manaCost > 0 && SourceUtil.takeSourceWithParticles(pos, world, 10, manaCost) == null) {
                world.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, world.random.nextInt(4) + 1, 0, 0D, 0, 0D);
                spell.remove(RemovalReason.DISCARDED);
                return;
            }

            var acceleration = (float) spell.spellResolver.spell.getBuffsAtIndex(0, null, AugmentAccelerate.INSTANCE) - (float) spell.spellResolver.spell.getBuffsAtIndex(0, null, AugmentDecelerate.INSTANCE) * 0.5F;
            var velocity = Math.max(0.1F, 0.5F + 0.1F * Math.min(2.0F, acceleration));
            var dir = newPos.subtract(oldPos).normalize();
            spell.shoot(dir.x, dir.y, dir.z, velocity, 0.0F);
        }

        SpellProjectileHitEvent event = new SpellProjectileHitEvent(spell, hit);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }
        spell.spellResolver.onResolveEffect(spell.level(), hit);
        spell.remove(RemovalReason.DISCARDED);

        BlockUtil.updateObservers(world, pos);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
        return new WarpingSpellPrismTile(pPos, pState);
    }

    public static EntityProjectileSpell changeSpellLevel(ServerLevel level, EntityProjectileSpell old) {
        EntityProjectileSpell spell = (EntityProjectileSpell) old.getType().create(level);
        if (spell != null) {
            spell.restoreFrom(old);
            spell.spellResolver = old.spellResolver;
            spell.prismRedirect = old.prismRedirect;
            spell.age = old.age;
            spell.pierceLeft = old.pierceLeft;
            spell.numSensitive = old.numSensitive;
            spell.setDeltaMovement(old.getDeltaMovement());
            level.addDuringTeleport(spell);
            old.remove(RemovalReason.CHANGED_DIMENSION);
        }
        return spell;
    }
}
