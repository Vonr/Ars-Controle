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

public class PacketSetRemoteLockMode extends AbstractPacket {
    public static final Type<PacketSetRemoteLockMode> TYPE = new Type<>(ArsControle.prefix("set_remote_lock_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetRemoteLockMode> CODEC = StreamCodec.ofMember(PacketSetRemoteLockMode::toBytes, PacketSetRemoteLockMode::new);

    public RemoteItem.LockModeSlot slot;

    public PacketSetRemoteLockMode(RemoteItem.LockModeSlot slot) {
        this.slot = slot;
    }

    public PacketSetRemoteLockMode(FriendlyByteBuf buf) {
        this.slot = buf.readEnum(RemoteItem.LockModeSlot.class);
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
            RemoteItem.RemoteData.fromItemStack(stack).withLockingMode(slot).write(stack);
            PortUtil.sendMessage(player, Component.translatable("ars_controle.remote.lock_mode.set", slot.translatable()));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
