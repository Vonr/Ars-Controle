# Warping Spell Prism: Ars Nouveau 5.13.1 callback compatibility

## Affected version and cause

This fix is based on Ars Controle **1.6.15** (tag commit `ecbb83b`), for
Minecraft **1.21.1**. The reported installation uses NeoForge **21.1.248**,
Ars Nouveau **5.13.1**, and Ars Controle **1.6.15**, in single-player.
This is separate from the earlier 1.20.1 compatibility-validation branch.

Inspection of the installed release JARs established:

- `EntityProjectileSpell.redirect` invokes
  `IPrismaticBlock.onHit(Level, BlockState, BlockPos, EntityProjectileSpell)`.
- That interface method has an empty default implementation.
- Controle's Warping Prism implements only the older
  `onHit(ServerLevel, BlockPos, EntityProjectileSpell)` overload.
- The deprecated interface overload forwards **old to new**, not new to old.
  It therefore does not adapt the legacy implementation for the new caller.
- Nouveau's ordinary prism implements the new overload, explaining why it works
  while the Warping Prism silently does nothing. No linkage exception is expected.

The corresponding `IPrismaticBlock`, `EntityProjectileSpell`, and `SpellPrismBlock`
class files in Maven **5.13.1.1400** are byte-identical to those in the installed
5.13.1 JAR. This is an actual callback mismatch, not a collision-shape hypothesis.

## Change

Add the new callback and delegate to the existing handler only for a
`ServerLevel`. Keep the old callback for legacy callers. The new API also calls
prisms on the client; authoritative target resolution, Source use, and teleportation
must remain server-side.

The original handler body and tile implementation are unchanged: block/entity
selection, entity UUID tracking, cross-dimensional handling, Source charging,
chunk loading, event cancellation, and invalid-target failure behavior are not
rewritten. No collision fallback, mixin, reflection, or global projectile hook is added.

The compile dependency is updated from AN 5.10.6.1245 to 5.13.1.1400; no other
runtime dependency or loader version is upgraded. The build remains on the tag's
NeoForge 21.1.217. The mod is labeled **1.6.15+prismfix.1** to distinguish it from
the original release. JUnit/Mockito are test-only and are not bundled.

## Build and verification

Use Java 21 and the checked-in wrapper:

```sh
./gradlew clean build --rerun-tasks -I gradle/compatibility-repositories.init.gradle
```

The optional init script works around unavailable upstream repositories without
substituting dependency versions: NeoForge's mirror supplies Parchment; the
expired Octo Studios Maven is excluded; the exact original Curios Continuation
9.0.12+1.21 release is fetched from its immutable Modrinth version URL. Its verified
SHA-512 is:

```
c729f009d5c0376fa7aea147f823586f4b2604d4956d65ca049ef07e80a5868185bec2761c8ec1a72555f1a63ed80700e6dbf1884ae0669453d561cab6cd53eb
```

The clean build with `--rerun-tasks` exits successfully; six callback tests pass.
Removing only the new production callback makes exactly
three new-server tests fail, reproducing the silent non-dispatch; restoring it
makes all six pass. Tests cover exact-once delegation, client exclusion, the
legacy entry point, null-resolver removal, and no-target particles/removal.
See `src/test/README.md` for details and test reports.

Output: `build/libs/ars_controle-1.21.1-1.6.15+prismfix.1.jar`.
ZIP integrity, generated NeoForge metadata, both compiled callback signatures,
and exclusion of test classes/frameworks were verified. Existing deprecation
warnings remain; the legacy prism overload is deliberately retained.

Built JAR SHA-256:
`3659778c33732c56929f6fe2637af384605bf6df3ba7d5559b5342eeb42695f8`.

## Limits and safe in-game check

The callback tests use mocked worlds. They are not an end-to-end test of the
reported modpack, moving ships/contraptions, client visuals, real Source use,
entity tracking, or cross-dimensional gameplay. The original Modrinth profile,
mods, configurations, and world have not been modified.

Test only in a **duplicate profile/world**, replacing its original Controle JAR
with this JAR (do not keep both). First use ordinary terrain: compare
`Projectile → Light` on an ordinary prism and a Warping Prism linked with the
Remote to a nearby isolated block. Then check moving entity targets, invalid
and unavailable targets, Source-cost boundaries, cross-dimensional targets, and
any ship/contraption setup used in the original reproduction.

Do not use the earlier Minecraft 1.20.1 validation JAR in this profile.
