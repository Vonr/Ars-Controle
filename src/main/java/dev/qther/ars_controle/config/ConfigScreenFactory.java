package dev.qther.ars_controle.config;

import net.minecraft.client.gui.screens.Screen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public final class ConfigScreenFactory implements IConfigScreenFactory {
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
