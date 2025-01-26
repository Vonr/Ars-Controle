package dev.qther.ars_controle.registry;

import dev.qther.ars_controle.ArsControle;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.UUID;
import java.util.function.Supplier;

public class AttachmentRegistry {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArsControle.MODID);

    public static final Supplier<AttachmentType<UUID>> RELAY_UUID = ATTACHMENT_TYPES.register(
            "relay_uuid", () -> AttachmentType.builder(() -> new UUID(0, 0)).serialize(UUIDUtil.CODEC).build()
    );

    public static final Supplier<AttachmentType<UUID>> ASSOCIATION = ATTACHMENT_TYPES.register(
            "association", () -> AttachmentType.builder(() -> new UUID(0, 0)).serialize(UUIDUtil.CODEC).build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
