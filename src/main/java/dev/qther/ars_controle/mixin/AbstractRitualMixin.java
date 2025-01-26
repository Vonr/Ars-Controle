package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.qther.ars_controle.item.PortableBrazierRelayItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractRitual.class)
public class AbstractRitualMixin {
    @Shadow public RitualBrazierTile tile;

    @ModifyReturnValue(method = "getWorld", at = @At("RETURN"))
    public Level getWorld(Level original) {
        if (original instanceof ServerLevel serverLevel) {
            var rituals = PortableBrazierRelayItem.getRelayedRituals();
            var player = rituals.get((AbstractRitual) (Object) this);
            if (player != null) {
                return player.level();
            }
        }
        return original;
    }

    @ModifyReturnValue(method = "getPos", at = @At("RETURN"))
    public BlockPos getPos(BlockPos original) {
        if (this.tile.getLevel() instanceof ServerLevel serverLevel) {
            var rituals = PortableBrazierRelayItem.getRelayedRituals();
            var player = rituals.get((AbstractRitual) (Object) this);
            if (player != null) {
                return player.blockPosition();
            }
        }
        return original;
    }
}
