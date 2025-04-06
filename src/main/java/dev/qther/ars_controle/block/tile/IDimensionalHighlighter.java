package dev.qther.ars_controle.block.tile;

import com.hollingsworth.arsnouveau.api.item.IWandable;
import com.hollingsworth.arsnouveau.client.particle.ColorPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IDimensionalHighlighter extends IWandable {
    List<ColorPos> getWandHighlight(Level level, List<ColorPos> list);
}
