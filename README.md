# Ars Controle

Addon for Ars Nouveau focused on increasing control.

## Content

### Warping Spell Prism ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/block/warping_spell_prism.png?raw=true)

The Warping Spell Prism allows you to warp a spell projectile to anywhere in the world, even across dimensions!

### Scryer's Linkage

The Scryer's Linkage links to another block, allowing machines to interact with its linked block from distance places.

It is capable of linking items, liquids, energy, redstone, and a few other things.

To configure it, use a Dominion Wand first on the block you want to link to, then the linkage.

# Temporal Stability Sensor ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/block/temporal_stability_sensor.png?raw=true)

The Temporal Stability Sensor shows how unstable the time of the world is. 

Using a comparator, you can find out how close the world is to lagging, 
with higher redstone output meaning the world is closer to lagging. 

You can use this to turn off farms automatically to prevent the server from lagging.

### Remote ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/remote.png?raw=true)

The Remote is an item that lets you remotely configure blocks such as the Warping Spell Prism and Storage Lectern.

To use it, first use it on the block you would like to configure.
Then, you can use it again on other blocks or entities depending on what the block you are configuring accepts.  
To clear its configuration target, left click on air while holding the remote.

The remote will allow for cross dimensional configuration of the Warping Spell Prism, unlike the Dominion Wand
which does not store dimensions. (Dominion wands can now configure cross-dimensionally in Ars Nouveau 5.3.2 and higher)

### Adaptive Filters

Adaptive filters adapt other filters to change or combine their behaviours in various ways.

#### Unary Filters
Filter: NOT ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_not.png?raw=true)
- Only resolves the spell if the result of the next Filter is false.

#### Binary Filters
Filter: OR ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_or.png?raw=true)
- Only resolves the spell if either of the next 2 Filters are true.

Filter: XOR ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_xor.png?raw=true)
- Only resolves the spell if only one of the next 2 Filters are true.

Filter: XNOR ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_xnor.png?raw=true)  
- Only resolves the spell if the result of both the next 2 Filters are equal.

### Other Glyphs

#### Y-Level Filters

Filter: Above ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_above.png?raw=true)
- Only resolves the spell above the caster.

Filter: Level ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_level.png?raw=true)
- Only resolves the spell at the caster's elevation.

Filter: Below ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_below.png?raw=true)
- Only resolves the spell below the caster.

Filter: Random ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_filter_random.png?raw=true)  
- Has a base 50% chance of resolving. If amplified overall, chance will be `(100% - 50% / (2 ^ Amplification))`. If dampened overall, chance will be `(50% / (2 ^ Dampening))`

#### Effect Glyphs

Precise Delay ![](https://github.com/Vonr/Ars-Controle/blob/1.21/src/main/resources/assets/ars_controle/textures/item/glyph_precise_delay.png?raw=true)  
- Delays the remainder of the spell by `(2 ^ Extend Time Augments)` ticks

### Spell Book Control Improvements

#### Search Bar
- Auto-Focus
  - Clear Search Bar on Auto-Focus

#### Spell Crafting
- Swap Glyphs with Number Keys
