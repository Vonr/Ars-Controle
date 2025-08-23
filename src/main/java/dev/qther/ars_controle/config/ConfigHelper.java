package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class ConfigHelper {
    public static class CategoryBuilder {
        private final ModConfigSpec.Builder builder;
        private String category;

        public void push(String... comment) {
            builder.comment(comment).translation("ars_controle.config." + this.category).push(this.category);
        }

        public void pop() {
            builder.pop();
        }

        public CategoryBuilder child(String category) {
            return new CategoryBuilder(this.builder, category);
        }

        public <T> ModConfigSpec.ConfigValue<T> make(String name, T defaultValue, String... comment) {
            return builder.comment(comment).translation("ars_controle.config." + category + "." + name).define(name, defaultValue);
        }

        public ModConfigSpec.ConfigValue<List<? extends String>> makeStringList(String name, List<String> defaultValue, String defaultElement, final Predicate<String> valid, String... comment) {
            return builder.comment(comment).translation("ars_controle.config." + category + "." + name).defineListAllowEmpty(name, defaultValue, () -> defaultElement, (o) -> o instanceof String s && valid.test(s));
        }

        public <T extends Comparable<? super T>> ModConfigSpec.ConfigValue<T> makeBounded(String name, T defaultValue, T min, T max, Class<T> clazz, String... comment) {
            return builder.comment(comment).translation("ars_controle.config." + category + "." + name).defineInRange(name, defaultValue, min, max, clazz);
        }

        public ModConfigSpec.BooleanValue bool(String name, boolean defaultValue, String... comment) {
            return builder.comment(comment).translation("ars_controle.config." + category + "." + name).define(name, defaultValue);
        }

        public CategoryBuilder setCategory(@NotNull String category) {
            this.category = category;
            return this;
        }

        public String getCategory() {
            return this.category;
        }

        public CategoryBuilder(ModConfigSpec.Builder builder, String category) {
            this.builder = builder;
            this.category = category;
        }
    }
}
