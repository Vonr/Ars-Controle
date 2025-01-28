package dev.qther.ars_controle.util;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class RenderUtil {
    public static void renderBlockOutline(RenderLevelStageEvent event, BlockPos pos) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) {
            return;
        }
        var state = level.getBlockState(pos);
        var shape = state.getShape(level, pos);

        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        var color = ParticleColor.defaultParticleColor();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        LevelRenderer.renderVoxelShape(poseStack, lines, shape, 0, 0, 0, color.getRed(), color.getGreen(), color.getBlue(), 1.0F, false);
        poseStack.popPose();

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }
}
