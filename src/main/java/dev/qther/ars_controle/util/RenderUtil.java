package dev.qther.ars_controle.util;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.qther.ars_controle.ArsControle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalDouble;

public class RenderUtil {
    static RenderType BLOCK_OUTLINE = RenderType.create(ArsControle.MODID + "_block_outline", DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES, 256, false, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(1.0)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    public static void renderBlockOutline(PoseStack poseStack, BlockPos pos) {
        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) {
            return;
        }
        var state = mc.level.getBlockState(pos);
        var shape = state.getShape(level, pos);

        poseStack.pushPose();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        OutlineBufferSource buffer = mc.renderBuffers().outlineBufferSource();
        VertexConsumer lines = buffer.getBuffer(BLOCK_OUTLINE);
        var color = ParticleColor.defaultParticleColor();
        LevelRenderer.renderVoxelShape(poseStack, lines, shape, 0, 0, 0, color.getRed(), color.getGreen(), color.getBlue(), 1.0F, false);

        poseStack.popPose();
    }
}
