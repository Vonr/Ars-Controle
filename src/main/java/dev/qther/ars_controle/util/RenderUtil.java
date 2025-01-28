package dev.qther.ars_controle.util;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class RenderUtil {
    public static void renderBlockOutline(RenderLevelStageEvent event, BlockPos pos) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (level == null || player == null) {
            return;
        }
        var state = level.getBlockState(pos);
        var shape = state.getShape(level, pos, CollisionContext.of(player));

        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();

        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
        var tess = Tesselator.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();

        var builder = tess.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        var color = ParticleColor.defaultParticleColor();
        LevelRenderer.renderVoxelShape(poseStack, builder, shape, 0, 0, 0, color.getRed(), color.getGreen(), color.getBlue(), 1.0F, false);

        BufferUploader.drawWithShader(builder.buildOrThrow());

        poseStack.popPose();

        RenderSystem.enableDepthTest();
    }
}
