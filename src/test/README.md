# Warping prism callback regression tests

With Java 21:

```sh
./gradlew test --tests dev.qther.ars_controle.block.WarpingSpellPrismCallbackTest \
  -I gradle/compatibility-repositories.init.gradle
```

Uses [ModDevGradle's JUnit launcher](https://github.com/neoforged/ModDevGradle#unit-testing-with-junit)
to bootstrap Minecraft/mods without starting a server or opening a world.
Mockito's test-only subclass mock maker avoids Java-agent attachment.
Reports: `build/reports/tests/test/index.html` and `build/test-results/test/`.

Six tests use the dependency's actual `IPrismaticBlock` defaults and production
block methods: exact-once server delegation, client exclusion, null-resolver
removal, retained legacy dispatch, no-target particles/removal, and an old-only
implementation demonstrating the inherited no-op.

Negative control: in a disposable worktree, keep AN 5.13.1.1400 and remove only
the production four-argument callback. Three new-server tests fail; the other
three pass. Restoring the callback makes all six pass. Both outcomes were verified.

These are callback-boundary tests with mocked worlds, not gameplay GameTests.
They do not execute projectile ray tracing/ticks, real target lookup, teleportation,
Source consumption, cross-dimensional transfer, or client/server synchronization.
See [the investigation notes](../../docs/warping-prism-1.21.1.md).
