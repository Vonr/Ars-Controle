package dev.qther.ars_controle.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

@EventBusSubscriber
public class RenderQueue {
    private static final Deque<RenderTask> QUEUE = new ArrayDeque<>();
    private static long LAST_TIME = -1;

    public static void enqueue(RenderTask task) {
        if (task.until < LAST_TIME) {
            return;
        }
        QUEUE.addLast(task);
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Pre event) {
        if (QUEUE.isEmpty()) {
            return;
        }

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) {
            return;
        }

        LAST_TIME = level.getGameTime();
        var iter = QUEUE.iterator();
        //noinspection Java8CollectionRemoveIf
        while (iter.hasNext()) {
            var task = iter.next();
            if (task.until < LAST_TIME) {
                iter.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onWorldChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        QUEUE.clear();
        LAST_TIME = -1;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRender(RenderLevelStageEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        LocalPlayer player = Minecraft.getInstance().player;

        if (level == null || player == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        for (var task : QUEUE) {
            task.fn.accept(event.getPoseStack());
        }
    }

    public record RenderTask(Consumer<PoseStack> fn, long until) {
        public static RenderTask until(Consumer<PoseStack> fn, long until) {
            return new RenderTask(fn, until);
        }

        public static @Nullable RenderTask ofDuration(Consumer<PoseStack> fn, long length) {
            var mc = Minecraft.getInstance();
            var level = mc.level;
            if (level == null) {
                return null;
            }
            return new RenderTask(fn, level.getGameTime() + length);
        }
    }
}
