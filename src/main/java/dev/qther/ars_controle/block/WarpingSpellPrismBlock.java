package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.api.block.IPrismaticBlock;
import com.hollingsworth.arsnouveau.api.event.SpellProjectileHitEvent;
import com.hollingsworth.arsnouveau.api.util.BlockUtil;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;
import com.hollingsworth.arsnouveau.common.advancement.ANCriteriaTriggers;
import com.hollingsworth.arsnouveau.common.block.ModBlock;
import com.hollingsworth.arsnouveau.common.entity.EntityFollowProjectile;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.config.ServerConfig;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

public class WarpingSpellPrismBlock extends ModBlock implements IPrismaticBlock, EntityBlock {
    public WarpingSpellPrismBlock() {
        super();
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult _hr) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!state.canEntityDestroy(level, pos, player)) {
            return InteractionResult.FAIL;
        }

        var te = level.getBlockEntity(pos);
        if (te == null) {
            ArsControle.LOGGER.warn("No tile entity in warping spell prism.");
            return InteractionResult.FAIL;
        }

        if (!(te instanceof WarpingSpellPrismTile tile)) {
            ArsControle.LOGGER.warn("Wrong tile entity in warping spell prism.");
            return InteractionResult.FAIL;
        }

        if (player.isCrouching()) {
            tile.setEntityUUID(player.getUUID());
            tile.setChanged();
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.self"));
            return InteractionResult.SUCCESS;
        }

        var hitResult = tile.getHitResult();
        switch (hitResult) {
            case null -> {
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.none"));
                return InteractionResult.SUCCESS;
            }
            case EntityHitResult e -> {
                var entity = e.getEntity();
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.entity", entity.getDisplayName(), entity.level().dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }
            case BlockHitResult b -> {
                var dim = tile.getTargetLevel();
                PortUtil.sendMessage(player, Component.translatable("ars_controle.target.get.block", b.getBlockPos().toShortString(), dim == null ? "<invalid>" : dim.dimension().location().toString()));
                return InteractionResult.SUCCESS;
            }
            default -> {
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onHit(ServerLevel world, BlockPos pos, @NotNull EntityProjectileSpell spell) {
        if (spell.spellResolver == null) {
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        WarpingSpellPrismTile tile = (WarpingSpellPrismTile) world.getBlockEntity(pos);
        if (tile == null) {
            ArsControle.LOGGER.error("No tile entity in warping spell prism.");
            return;
        }

        var hit = tile.getHitResult();
        if (hit == null) {
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        var dim = tile.getTargetLevel();
        if (dim == null) {
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        var hitPos = BlockPos.containing(hit.getLocation());
        var chunk = dim.getChunkAt(hitPos);

        var loadTime = ServerConfig.SERVER.WARPING_SPELL_PRISM_LOAD_TIME.get();
        if (loadTime > 0) {
            dim.getChunkSource().addRegionTicket(TicketType.PORTAL, chunk.getPos(), 3, hitPos, true);
        } else if (!dim.isLoaded(hitPos)) {
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        var oldLevel = spell.level();
        if (hit instanceof BlockHitResult b) {
            if (oldLevel != dim) {
                var newSpell = changeSpellLevel(dim, spell);
                if (newSpell != null) {
                    spell = newSpell;
                }
            }

            hit = b.withDirection(spell.getDirection());
        }

        if (hit instanceof EntityHitResult e) {
            if (oldLevel != dim) {
                var newSpell = changeSpellLevel(dim, spell);
                if (newSpell != null) {
                    spell = newSpell;
                }
            }

            var entity = e.getEntity();
            if (!entity.isAlive()) {
                tile.setEntityUUID(null);
                world.sendParticles(ParticleTypes.ANGRY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0D, 0, 0D);
                spell.remove(RemovalReason.DISCARDED);
                return;
            }

            SpellProjectileHitEvent event = new SpellProjectileHitEvent(spell, hit);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return;
            }

            spell.spellResolver.onResolveEffect(spell.level(), new EntityHitResult(entity, hit.getLocation()));
            spell.remove(RemovalReason.DISCARDED);
            return;
        }

        var oldPos = spell.position();
        var newPos = hit.getLocation();

        if (newPos.equals(oldPos) && oldLevel == spell.level()) {
            return;
        }

        spell.setPos(newPos);

        int manaCost = tile.getSourceRequired(hit);
        if (manaCost > 0) {
            var providers = SourceUtil.canTakeSource(pos, dim, 19);
            var needed = manaCost;
            for (var provider : providers) {
                var source = Math.min(needed, provider.getSource().getSource());
                if (source > 0) {
                    needed -= source;
                }
                if (needed <= 0) {
                    break;
                }
            }

            if (needed > 0) {
                world.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, world.random.nextInt(4) + 1, 0, 0D, 0, 0D);
                spell.remove(RemovalReason.DISCARDED);
                return;
            }

            needed = manaCost;
            for (var provider : providers) {
                var source = Math.min(needed, provider.getSource().getSource());
                if (source > 0) {
                    provider.getSource().removeSource(source);
                    needed -= source;

                    EntityFollowProjectile aoeProjectile = new EntityFollowProjectile(dim, provider.getCurrentPos(), pos);
                    dim.addFreshEntity(aoeProjectile);
                }
                if (needed <= 0) {
                    break;
                }
            }
        }

        if (++spell.prismRedirect >= 3) {
            ANCriteriaTriggers.rewardNearbyPlayers(ANCriteriaTriggers.PRISMATIC.get(), world, pos, 10);
        }

        if (spell.level().getBlockCollisions(spell, spell.getBoundingBox()).iterator().hasNext()) {
            SpellProjectileHitEvent event = new SpellProjectileHitEvent(spell, hit);
            NeoForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return;
            }
            spell.spellResolver.onResolveEffect(spell.level(), hit);
            spell.remove(RemovalReason.DISCARDED);
        } else {
            var entities = spell.level().getEntities(spell, spell.getBoundingBox()).iterator();
            if (entities.hasNext()) {
                var e = entities.next();

                SpellProjectileHitEvent event = new SpellProjectileHitEvent(spell, hit);
                NeoForge.EVENT_BUS.post(event);
                if (event.isCanceled()) {
                    return;
                }
                spell.spellResolver.onResolveEffect(spell.level(), new EntityHitResult(e, spell.position()));
                spell.remove(RemovalReason.DISCARDED);
            } else {
                spell.setPos(spell.position().subtract(spell.getDeltaMovement()));
            }
        }

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
