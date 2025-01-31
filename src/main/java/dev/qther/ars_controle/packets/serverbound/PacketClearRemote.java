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

public class PacketClearRemote extends AbstractPacket {
    public static final Type<PacketClearRemote> TYPE = new Type<>(ArsControle.prefix("clear_remote"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketClearRemote> CODEC = StreamCodec.ofMember(PacketClearRemote::toBytes, PacketClearRemote::new);

    public PacketClearRemote() {
    }

    public PacketClearRemote(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() == ACRegistry.Items.REMOTE.get()) {
            stack.set(ACRegistry.Components.REMOTE, RemoteItem.RemoteData.empty());
            PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.none"));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
