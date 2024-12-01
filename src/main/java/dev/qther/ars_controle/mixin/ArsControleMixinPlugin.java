package dev.qther.ars_controle.mixin;

import dev.qther.ars_controle.ArsControle;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ArsControleMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "dev.qther.ars_controle.mixin.";

    @Override
    public void onLoad(String mixinPackage) {
        ArsControle.LOGGER.warn("Mixin plugin loaded");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(PACKAGE)) {
            return false;
        }

        var mods = FMLLoader.getLoadingModList().getMods();
//        if (mixinClassName.endsWith("EventQueueMixin")) {
//            var version = mods.stream().filter(m -> m.getModId().equals("ars_nouveau")).map(ModInfo::getVersion).findAny();
//            if (version.isEmpty()) {
//                return false;
//            }
//            try {
//                if (!VersionRange.createFromVersionSpec("(,5.2.0]").containsVersion(version.get())) {
//                    ArsControle.LOGGER.info("Skipping EventQueue fixes for ars_nouveau < 5.2.0");
//                    return false;
//                } else {
//                    ArsControle.LOGGER.info("Applying EventQueue fixes for ars_nouveau < 5.2.0");
//                }
//            } catch (InvalidVersionSpecificationException e) {
//                throw new RuntimeException(e);
//            }
//        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
