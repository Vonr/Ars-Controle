package dev.qther.ars_controle.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

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
