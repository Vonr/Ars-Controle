package dev.qther.ars_controle.datagen;

import dev.qther.ars_controle.ArsControle;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class ACSetup {
    public static void gatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = event.getGenerator().getPackOutput();
        var provider = event.getLookupProvider();
        var fileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeClient(), new ACLangDatagen(output, ArsControle.MODID, "en_us"));

        gen.addProvider(event.includeServer(), new ACArsProviders.ImbuementProvider(gen));
        gen.addProvider(event.includeServer(), new ACArsProviders.GlyphProvider(gen));
        gen.addProvider(event.includeServer(), new ACArsProviders.EnchantingAppProvider(gen));
        gen.addProvider(event.includeServer(), new ACRecipeDatagen(output, provider));

        gen.addProvider(event.includeServer(), new ACBlockStateDatagen(output, fileHelper));
        gen.addProvider(event.includeServer(), new ACLootProvider(output, provider));
        gen.addProvider(event.includeServer(), new ACBlockTagProvider(output, provider, fileHelper));
        gen.addProvider(event.includeServer(), new ACItemTagProvider(output, provider, fileHelper));
    }
}
