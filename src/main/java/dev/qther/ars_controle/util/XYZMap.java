package dev.qther.ars_controle.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XYZMap<V> extends XYMap<V> {
    private static final ImmutableSet<String> KEYS = ImmutableSet.of("x", "y", "z");
    private V z;

    XYZMap(V x, V y, V z) {
        super(x, y);
        this.z = z;
    }

    public static <V> XYZMap<V> of(V x, V y, V z) {
        return new XYZMap<>(x, y, z);
    }

    public static XYZMap<Double> of(Vec3 vec) {
        return new XYZMap<>(vec.x, vec.y, vec.z);
    }

    public static XYZMap<Integer> of(BlockPos pos) {
        return new XYZMap<>(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && (key.equals("x") || key.equals("y") || key.equals("z"));
    }

    @Override
    public V get(Object key) {
        if (key instanceof String s && s.equals("z")) {
            return this.z;
        }

        return super.get(key);
    }

    @Nullable
    @Override
    public V put(String key, V value) {
        switch (key) {
            case "x" -> x = value;
            case "y" -> y = value;
            case "z" -> z = value;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return KEYS;
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return List.of(x, y, z);
    }

    @NotNull
    @Override
    public Set<Entry<String, V>> entrySet() {
        return Set.of(new XEntry<>(this), new YEntry<>(this), new ZEntry<>(this));
    }

    public V getZ() {
        return z;
    }

    public void setZ(V z) {
        this.z = z;
    }

    public static class ZEntry<V> implements Map.Entry<String, V> {
        XYZMap<V> map;

        public ZEntry(XYZMap<V> map) {
            this.map = map;
        }

        @Override
        public String getKey() {
            return "z";
        }

        @Override
        public V getValue() {
            return map.getZ();
        }

        @Override
        public V setValue(V value) {
            map.setZ(value);
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ZEntry other && other.map.equals(this.map);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode() + 2;
        }
    }
}