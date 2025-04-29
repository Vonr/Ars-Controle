package dev.qther.ars_controle;

import dev.qther.ars_controle.config.ACClientConfig;
import dev.qther.ars_controle.config.ConfigScreenFactory;
import dev.qther.ars_controle.item.RemoteItem;
import dev.qther.ars_controle.packets.ACNetworking;
import dev.qther.ars_controle.packets.serverbound.PacketClearRemote;
import dev.qther.ars_controle.registry.ACRegistry;
import dev.qther.ars_controle.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(value = ArsControle.MODID, dist = Dist.CLIENT)
public class ArsControleClient {
    public ArsControleClient(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ACClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, new ConfigScreenFactory());

        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        var stack = event.getItemStack();
        if (stack.getItem() == ACRegistry.Items.REMOTE.get()) {
            ACNetworking.sendToServer(new PacketClearRemote());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        var mc = Minecraft.getInstance();
        var level = Minecraft.getInstance().level;
        var player = Minecraft.getInstance().player;
        if (level == null || player == null) {
            return;
        }

        var remote = player.getMainHandItem();
        if (!remote.is(ACRegistry.Items.REMOTE.get())) {
            return;
        }

        var data = RemoteItem.RemoteData.fromItemStack(remote);

        if (data.block().isPresent() && data.block().get().dimension().equals(level.dimension())) {
            RenderUtil.renderBlockOutline(event, data.block().get().pos());
        }

        if (!data.multiple() || data.firstCorner().isEmpty() || !data.firstCorner().get().dimension().equals(level.dimension()) || !(mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != HitResult.Type.MISS)) {
            return;
        }

        RenderUtil.renderAABBOutline(event, AABB.encapsulatingFullBlocks(data.firstCorner().get().pos(), bhr.getBlockPos()));
    }
}
