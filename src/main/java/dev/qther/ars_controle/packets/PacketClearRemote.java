package dev.qther.ars_controle.packets;

import com.hollingsworth.arsnouveau.common.util.PortUtil;
import dev.qther.ars_controle.registry.ModRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketClearRemote {
    public PacketClearRemote() {
    }

    public PacketClearRemote(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) {
                    return;
                }
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() == ModRegistry.REMOTE.get()) {
                    var tag = stack.getTag();
                    if (tag == null) {
                        stack.setTag(new CompoundTag());
                        PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.none"));
                        return;
                    }
                    tag.remove("target");
                    tag.remove("targetName");
                    tag.remove("dimension");
                    PortUtil.sendMessage(player, Component.translatable("ars_controle.target.set.none"));
                }
            });
        }
        ctx.get().setPacketHandled(true);
    }
}
