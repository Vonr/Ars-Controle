package dev.qther.ars_controle.util;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.mixin.LevelRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class RenderUtil {
    private static final RenderType BLOCK_OUTLINE = RenderType.create(ArsControle.MODID + "_block_outline",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.DEBUG_LINES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(OUTLINE_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false));

    public static void renderBlockOutline(RenderLevelStageEvent event, BlockPos pos) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) {
            return;
        }

        var state = level.getBlockState(pos);
        var shape = state.getShape(level, pos);

        Vec3 projectedView = mc.gameRenderer.getMainCamera().getPosition();

        var poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        OutlineBufferSource buffer = mc.renderBuffers().outlineBufferSource();
        var builder = buffer.getBuffer(BLOCK_OUTLINE);

        var color = ParticleColor.defaultParticleColor();
        LevelRendererInvoker.invokeRenderShape(poseStack, builder, shape, 0, 0, 0, color.getRed(), color.getGreen(), color.getBlue(), 1.0F);
        poseStack.popPose();
    }
}
