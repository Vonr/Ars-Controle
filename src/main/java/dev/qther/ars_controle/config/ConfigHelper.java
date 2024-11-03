package dev.qther.ars_controle.config;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static final class ConfigScreenFactory implements IConfigScreenFactory {
        @Override
        public @NotNull Screen createScreen(@NotNull ModContainer modContainer, @NotNull Screen screen) {
            return new ConfigurationScreen(modContainer, screen, (a, b, c, d) -> new ConfigurationScreen.ConfigurationSectionScreen(a, b, c, d, new ConfigFilter()));
        }

        public static final class ConfigFilter implements ConfigurationScreen.ConfigurationSectionScreen.Filter {
            @Override
            public ConfigurationScreen.ConfigurationSectionScreen.Element filterEntry(ConfigurationScreen.ConfigurationSectionScreen.Context context, String s, ConfigurationScreen.ConfigurationSectionScreen.Element element) {
                // this doesn't do what i want it to
//                if (context.modConfig().getFileName().contains("glyph_")) {
//                    return null;
//                }

                return element;
            }
        }
    }
}
