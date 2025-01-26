package dev.qther.ars_controle;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

public class Cached {
    public static final Map<String, WeakReference<ServerLevel>> LEVELS_BY_NAME = new Object2ReferenceArrayMap<>(8);

    public static @Nullable ServerLevel getLevelByName(@NotNull Iterable<ServerLevel> levels, @NotNull String name) {
        if (LEVELS_BY_NAME.containsKey(name)) {
            var l = LEVELS_BY_NAME.get(name).get();
            if (l != null) {
                return l;
            }
        }

        for (var l : levels) {
            if (!name.equals(l.dimension().location().toString())) {
                continue;
            }

            LEVELS_BY_NAME.put(name, new WeakReference<>(l));
            return l;
        }

        return null;
    }

    public static final Cache<UUID, Entity> ENTITIES_BY_UUID = CacheBuilder.newBuilder().weakValues()
            .expireAfterAccess(Duration.ofMinutes(10)).initialCapacity(8).build();

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
}
