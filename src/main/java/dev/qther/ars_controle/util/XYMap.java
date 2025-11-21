package dev.qther.ars_controle.util;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XYMap<V> implements Map<String, V> {
    private static final ImmutableSet<String> KEYS = ImmutableSet.of("x", "y");

    V x;
    V y;

    XYMap(V x, V y) {
        this.x = x;
        this.y = y;
    }

    public static <V> XYMap<V> of(V x, V y, V z) {
        return new XYMap<>(x, y);
    }

    public static XYMap<Float> of(Vec2 vec) {
        return new XYMap<>(vec.x, vec.y);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && (key.equals("x") || key.equals("y"));
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        if (key instanceof String s) {
            return switch (s) {
                case "x" -> this.x;
                case "y" -> this.y;
                default -> throw new UnsupportedOperationException();
            };
        }

        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public V put(String key, V value) {
        switch (key) {
            case "x" -> x = value;
            case "y" -> y = value;
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
        return List.of(x, y);
    }

    @NotNull
    @Override
    public Set<Entry<String, V>> entrySet() {
        return Set.of(new XEntry<>(this), new YEntry<>(this));
    }

    public V getX() {
        return x;
    }

    public V getY() {
        return y;
    }

    public void setX(V x) {
        this.x = x;
    }

    public void setY(V y) {
        this.y = y;
    }

    public static class XEntry<V> implements Entry<String, V> {
        XYMap<V> map;

        public XEntry(XYMap<V> map) {
            this.map = map;
        }

        @Override
        public String getKey() {
            return "x";
        }

        @Override
        public V getValue() {
            return map.getX();
        }

        @Override
        public V setValue(V value) {
            map.setX(value);
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof XEntry other && other.map.equals(this.map);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode();
        }
    }

    public static class YEntry<V> implements Entry<String, V> {
        XYMap<V> map;

        public YEntry(XYMap<V> map) {
            this.map = map;
        }

        @Override
        public String getKey() {
            return "y";
        }

        @Override
        public V getValue() {
            return map.getY();
        }

        @Override
        public V setValue(V value) {
            map.setY(value);
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof YEntry other && other.map.equals(this.map);
        }

        @Override
        public int hashCode() {
            return this.map.hashCode() + 1;
        }
    }
}