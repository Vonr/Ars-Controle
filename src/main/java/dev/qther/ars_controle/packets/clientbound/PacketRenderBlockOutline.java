package dev.qther.ars_controle.packets.clientbound;

import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.util.RenderQueue;
import dev.qther.ars_controle.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class PacketRenderBlockOutline extends AbstractPacket {
    public static final Type<PacketRenderBlockOutline> TYPE = new Type<>(ArsControle.prefix("enqueue_render_task"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRenderBlockOutline> CODEC = StreamCodec.ofMember(PacketRenderBlockOutline::toBytes, PacketRenderBlockOutline::new);

    private final BlockPos pos;
    private final long duration;

    public PacketRenderBlockOutline(BlockPos pos, long duration) {
        this.pos = pos;
        this.duration = duration;
    }

    public PacketRenderBlockOutline(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.duration = buf.readVarLong();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeVarLong(this.duration);
    }

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        var task = RenderQueue.RenderTask.ofDuration((event) -> {
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
                RenderUtil.renderBlockOutline(event, this.pos);
            }
        }, this.duration);
        if (task != null) {
            RenderQueue.enqueue(task);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
