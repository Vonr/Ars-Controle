package dev.qther.ars_controle.packets.serverbound;

import com.hollingsworth.arsnouveau.common.network.AbstractPacket;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.ArsControle;
import dev.qther.ars_controle.item.RemoteItem;
import dev.qther.ars_controle.registry.ACRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PacketSetRemoteSelectionMode extends AbstractPacket {
    public static final Type<PacketSetRemoteSelectionMode> TYPE = new Type<>(ArsControle.prefix("set_remote_selection_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetRemoteSelectionMode> CODEC = StreamCodec.ofMember(PacketSetRemoteSelectionMode::toBytes, PacketSetRemoteSelectionMode::new);

    public RemoteItem.SelectionModeSlot slot;

    public PacketSetRemoteSelectionMode(RemoteItem.SelectionModeSlot slot) {
        this.slot = slot;
    }

    public PacketSetRemoteSelectionMode(FriendlyByteBuf buf) {
        this.slot = buf.readEnum(RemoteItem.SelectionModeSlot.class);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(slot);
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() == ACRegistry.Items.REMOTE.get()) {
            var data = RemoteItem.RemoteData.fromItemStack(stack);
            var corner = slot == RemoteItem.SelectionModeSlot.MULTIPLE ? data.firstCorner().orElse(null) : null;
            data.withSelectionMode(slot).withFirstCorner(corner).write(stack);
            PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.selection_mode.set", slot.translatable()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
