package dev.qther.ars_controle.spell.effect;

import com.hollingsworth.arsnouveau.api.event.DelayedSpellEvent;
import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.network.Networking;
import com.hollingsworth.arsnouveau.common.network.PacketClientDelayEffect;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.ACNames;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class EffectPreciseDelay extends AbstractEffect {
    public static EffectPreciseDelay INSTANCE = new EffectPreciseDelay();

    private EffectPreciseDelay() {
        super(ArsControle.prefix(ACNames.GLYPH_PRECISE_DELAY), "PreciseDelay");
    }

    public void sendPacket(Level world, HitResult rayTraceResult, @Nullable LivingEntity shooter, SpellContext spellContext, SpellStats spellStats, BlockHitResult blockResult, Entity hitEntity, SpellResolver spellResolver) {
        if (spellContext.getCurrentIndex() >= spellContext.getSpell().size()) {
            return;
        }

        int duration = 1 << spellStats.getBuffCount(AugmentExtendTime.INSTANCE);

        var delayEvent = new DelayedSpellEvent(spellContext.getDelayedSpellEvent() == null ? duration : duration + 1, rayTraceResult, world, spellResolver);
        spellContext.delay(delayEvent);

        EventQueue.getServerInstance().addEvent(delayEvent);
        Networking.sendToNearbyClient(world, BlockPos.containing(safelyGetHitPos(rayTraceResult)),
                new PacketClientDelayEffect(duration, shooter, spellContext.getSpell(), spellContext, blockResult, hitEntity));
    }


    @Override
    public void onResolveBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        sendPacket(world, rayTraceResult, shooter, spellContext, spellStats, rayTraceResult, null, resolver);
    }

    @Override
    public void onResolveEntity(EntityHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver) {
        sendPacket(world, rayTraceResult, shooter, spellContext, spellStats, null, rayTraceResult.getEntity(), resolver);
    }

    @Override
    protected int getDefaultManaCost() {
        return 0;
    }

    @NotNull
    @Override
    public Set<AbstractAugment> getCompatibleAugments() {
        return augmentSetOf(AugmentExtendTime.INSTANCE);
    }

    @Override
    protected void addDefaultAugmentLimits(Map<ResourceLocation, Integer> defaults) {
        super.addDefaultAugmentLimits(defaults);

        // Surely no one needs this much delay...
        defaults.put(AugmentExtendTime.INSTANCE.getRegistryName(), 20);
    }

    @Override
    public String getBookDescription() {
        return "Delays the remainder of the spell by (2 ^ Extend Time Augments) ticks";
    }

    @Override
    public SpellTier defaultTier() {
        return SpellTier.TWO;
    }

    @NotNull
    @Override
    public Set<SpellSchool> getSchools() {
        return setOf(SpellSchools.MANIPULATION);
    }
}
