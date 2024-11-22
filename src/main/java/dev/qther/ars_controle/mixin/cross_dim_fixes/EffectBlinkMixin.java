package dev.qther.ars_controle.mixin.cross_dim_fixes;

import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketWarpPosition;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectBlink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(EffectBlink.class)
public class EffectBlinkMixin {
    @WrapOperation(method = "onResolveEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;teleportTo(DDD)V"))
    private void entityCrossDimensionalBlink(LivingEntity shooter, double x, double y, double z, Operation<Void> original, @Local(argsOnly = true) Level world) {
        if (shooter.level() == world) {
            original.call(shooter, x, y, z);
            return;
        }

        if (world instanceof ServerLevel level) {
            shooter.teleportTo(level, x, y, z, Set.of(), shooter.getYRot(), shooter.getXRot());
        }
    }

    @WrapOperation(method = "onResolveBlock", at = @At(value = "INVOKE", target = "Lcom/hollingsworth/arsnouveau/common/spell/effect/EffectBlink;warpEntity(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)V"))
    private void blockCrossDimensionalBlink(Entity entity, BlockPos warpPos, Operation<Void> original, @Local(argsOnly = true) Level world) {
        if (entity == null) {
            return;
        }

        if (entity.level() == world) {
            original.call(entity, warpPos);
            return;
        }

        if (!(entity.level() instanceof ServerLevel eLevel)) {
            return;
        }
        if (!(world instanceof ServerLevel level)) {
            return;
        }

        if (entity instanceof LivingEntity living) {
            EntityTeleportEvent.EnderEntity event = EventHooks.onEnderTeleport(living, warpPos.getX(), warpPos.getY(), warpPos.getZ());
            if (event.isCanceled()) return;
        }

        eLevel.sendParticles(ParticleTypes.PORTAL, entity.getX(), entity.getY() + 1, entity.getZ(), 4, (level.random.nextDouble() - 0.5D) * 2.0D, -level.random.nextDouble(), (level.random.nextDouble() - 0.5D) * 2.0D, 0.1f);

        entity.teleportTo(level, warpPos.getX() + 0.5, warpPos.getY(), warpPos.getZ() + 0.5, Set.of(), entity.getYRot(), entity.getXRot());
        Networking.sendToNearbyClient(level, entity, new PacketWarpPosition(entity.getId(), entity.getX(), entity.getY(), entity.getZ(), entity.getXRot(), entity.getYRot()));

        level.playSound(null, entity.blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.NEUTRAL, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.PORTAL, entity.blockPosition().getX() + 0.5, entity.blockPosition().getY() + 1.0, entity.blockPosition().getZ() + 0.5, 4, (level.random.nextDouble() - 0.5D) * 2.0D, -level.random.nextDouble(), (level.random.nextDouble() - 0.5D) * 2.0D, 0.1f);
    }
}
