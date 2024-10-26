package dev.qther.ars_controle.mixin;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.api.event.ITimedEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@EventBusSubscriber(modid = ArsNouveau.MODID)
@Mixin(value = EventQueue.class, remap = false)
public class EventQueueMixin {
    @Unique
    private static boolean ars_controle$stepping = false;

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true)
    private static void serverTick(ServerTickEvent.Post e, CallbackInfo ci) {
        var trm = e.getServer().tickRateManager();
        if (trm.isFrozen() && !ars_controle$stepping) {
            ci.cancel();
        }
    }

    @Unique
    @SubscribeEvent
    private static void ars_controle$serverTickPre(ServerTickEvent.Pre e) {
        ars_controle$stepping = e.getServer().tickRateManager().isSteppingForward();
    }

    @Shadow
    @NotNull
    List<ITimedEvent> events;

    @Unique
    private static final List<ITimedEvent> ars_Controle$stale = new ObjectArrayList<>();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(ServerTickEvent.Post e, CallbackInfo ci) {
        if (this.events.isEmpty()) {
            ci.cancel();
        }

        ars_Controle$stale.clear();
        // Enhanced-for or iterator will cause a concurrent modification.
        int size = events.size();
        for (int i = 0; i < size; i++) {
            ITimedEvent event = events.get(i);
            if (event.isExpired()) {
                ars_Controle$stale.add(event);
            } else {
                try {
                    if (e == null)
                        event.tick(false);
                    else
                        event.tick(e);
                } catch (Throwable ignored) {}
            }
        }
        this.events.removeAll(ars_Controle$stale);
        ci.cancel();
    }
}
