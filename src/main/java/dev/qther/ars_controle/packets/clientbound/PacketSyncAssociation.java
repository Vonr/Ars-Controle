package dev.qther.ars_controle.packets.clientbound;

import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.registry.AttachmentRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class PacketSyncAssociation extends AbstractPacket {
    public static final Type<PacketSyncAssociation> TYPE = new Type<>(ArsControle.prefix("sync_association"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSyncAssociation> CODEC = StreamCodec.ofMember(PacketSyncAssociation::toBytes, PacketSyncAssociation::new);

    private final GlobalPos pos;
    @Nullable
    private final UUID uuid;

    public PacketSyncAssociation(GlobalPos pos, @Nullable UUID uuid) {
        this.pos = pos;
        this.uuid = uuid;
    }

    public PacketSyncAssociation(FriendlyByteBuf buf) {
        this.pos = buf.readGlobalPos();
        if (buf.readBoolean()) {
            this.uuid = buf.readUUID();
        } else {
            this.uuid = null;
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeGlobalPos(this.pos);
        buf.writeBoolean(this.uuid != null);
        if (this.uuid != null) {
            buf.writeUUID(this.uuid);
        }
    }

    @Override
    public void onClientReceived(Minecraft minecraft, Player player) {
        if (minecraft.level == null || !minecraft.level.dimension().equals(pos.dimension())) {
            return;
        }

        var targetPos = this.pos.pos();

        var be = minecraft.level.getBlockEntity(targetPos);
        if (be == null) {
            return;
        }
        if (this.uuid != null) {
            be.setData(AttachmentRegistry.ASSOCIATION, this.uuid);
        } else {
            be.removeData(AttachmentRegistry.ASSOCIATION);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
