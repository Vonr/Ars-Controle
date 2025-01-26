package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.api.ritual.AbstractRitual;
import com.hollingsworth.arsnouveau.common.block.tile.RitualBrazierTile;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.qther.ars_controle.registry.AttachmentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = RitualBrazierTile.class, remap = false)
public abstract class RitualBrazierTileMixin extends BlockEntity {
    @Shadow public abstract void tick();

    @Shadow public AbstractRitual ritual;

    public RitualBrazierTileMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/hollingsworth/arsnouveau/api/ritual/AbstractRitual;tryTick(Lcom/hollingsworth/arsnouveau/common/block/tile/RitualBrazierTile;)V"))
    public void tryTick(AbstractRitual instance, RitualBrazierTile tickingTile, Operation<Void> original) {
        if (this.level == null || this.level.isClientSide) {
            original.call(instance, tickingTile);
            return;
        }

        if (this.hasData(AttachmentRegistry.RELAY_UUID)) {
            return;
        }

        original.call(instance, tickingTile);
    }

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void addTooltip(List<Component> tooltips, CallbackInfo ci) {
        if (this.ritual == null) {
            return;
        }

        var tile = this.ritual.tile;
        if (tile == null) {
            return;
        }

        var data = tile.getExistingData(AttachmentRegistry.ASSOCIATION);
        if (data.isPresent()) {
            try {
                var uuid = data.get();
                var profile = SkullBlockEntity.fetchGameProfile(uuid).get();
                tooltips.add(Component.translatable("ars_controle.portable_brazier_relay.relayed_to", profile.isPresent() ? profile.get().getName() : uuid.toString()).withStyle(ChatFormatting.GOLD));
            } catch (Exception ignored) {}
        }
    }
}
