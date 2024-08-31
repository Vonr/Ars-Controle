package dev.qther.ars_controle.datagen;

import net.neoforged.neoforge.data.event.GatherDataEvent;

public class Setup {
    public static void gatherData(GatherDataEvent event) {
        var gen = event.getGenerator();
        var output = event.getGenerator().getPackOutput();
        var provider = event.getLookupProvider();
        var fileHelper = event.getExistingFileHelper();

        gen.addProvider(event.includeServer(), new ArsProviders.ImbuementProvider(gen));
        gen.addProvider(event.includeServer(), new ArsProviders.GlyphProvider(gen));
        gen.addProvider(event.includeServer(), new ArsProviders.EnchantingAppProvider(gen));
        gen.addProvider(event.includeServer(), new ArsProviders.PatchouliProvider(gen));

        gen.addProvider(event.includeServer(), new BlockTagProvider(output, provider, fileHelper));
        gen.addProvider(event.includeServer(), new LootProvider(output, provider));
    }
}
