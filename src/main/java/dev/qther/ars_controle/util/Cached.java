package dev.qther.ars_controle.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Cached {
    public static @Nullable ServerLevel getLevelByName(@NotNull String name) {
        var loc = ResourceLocation.tryParse(name);
        if (loc == null) {
            return null;
        }

        return getLevelByLoc(loc);
    }

    public static @Nullable ServerLevel getLevelByLoc(@NotNull ResourceLocation loc) {
        return getLevelByKey(ResourceKey.create(Registries.DIMENSION, loc));
    }

    public static @Nullable ServerLevel getLevelByKey(@NotNull ResourceKey<Level> key) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }

        return server.getLevel(key);
    }

    public static final Cache<UUID, Entity> ENTITIES_BY_UUID = CacheBuilder.newBuilder().weakValues()
            .expireAfterAccess(Duration.ofMinutes(10)).initialCapacity(8).build();

    public static @Nullable Entity getEntityByUUID(@NotNull UUID uuid) {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? getEntityByUUID(server.getAllLevels(), uuid) : null;
    }

    public static @Nullable Entity getEntityByUUID(@NotNull Iterable<ServerLevel> levels, @NotNull UUID uuid) {
        var cached = ENTITIES_BY_UUID.getIfPresent(uuid);
        if (cached != null && cached.isAlive()) {
            return cached;
        }

        for (var l : levels) {
            var entity = l.getEntities().get(uuid);
            if (entity != null && entity.isAlive()) {
                ENTITIES_BY_UUID.put(uuid, entity);
                return entity;
            }
        }

        return null;
    }

    private static final Map<UUID, CompletableFuture<Optional<GameProfile>>> PLAYER_NAME_BY_UUID = new Object2ObjectOpenHashMap<>();

    public static CompletableFuture<Optional<GameProfile>> getGameProfileFromUUID(@NotNull UUID uuid) {
        return PLAYER_NAME_BY_UUID.computeIfAbsent(uuid, SkullBlockEntity::fetchGameProfile);
    }
}
