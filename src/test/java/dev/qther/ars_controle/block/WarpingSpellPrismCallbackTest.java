package dev.qther.ars_controle.block;

import com.hollingsworth.arsnouveau.api.block.IPrismaticBlock;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.qther.ars_controle.block.tile.WarpingSpellPrismTile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Uses the dependency's actual interface defaults and the production block's methods.
 * ModDevGradle bootstraps registries before JUnit; mocks avoid constructing a world.
 * Only the delegation test stubs the old handler. Neither overload is reimplemented
 * on the block under test, so removing the bridge exposes Ars Nouveau's real no-op.
 */
@SuppressWarnings("removal") // Retaining the deprecated server callback is intentional.
class WarpingSpellPrismCallbackTest {
    private final BlockPos pos = new BlockPos(7, 64, -3);
    // The bridge must forward the supplied position/projectile, not inspect the state.
    private final BlockState state = Blocks.AIR.defaultBlockState();
    private final WarpingSpellPrismBlock block = mock(WarpingSpellPrismBlock.class, CALLS_REAL_METHODS);
    private final IPrismaticBlock api = block;
    private final EntityProjectileSpell spell = mock(EntityProjectileSpell.class);

    @Test
    void newCallbackDelegatesToExistingServerHandlerExactlyOnce() {
        ServerLevel server = mock(ServerLevel.class);
        doNothing().when(block).onHit(same(server), same(pos), same(spell));

        // Same interface and overload used by AN 5.13.1 EntityProjectileSpell.redirect.
        api.onHit((Level) server, state, pos, spell);

        verify(block).onHit(same(server), same(state), same(pos), same(spell));
        verify(block, times(1)).onHit(same(server), same(pos), same(spell));
        verifyNoMoreInteractions(block);
        verifyNoInteractions(server, spell);
    }

    @Test
    void newCallbackOnClientDoesNotEnterServerHandlerOrTouchProjectile() {
        ClientLevel client = mock(ClientLevel.class);

        api.onHit(client, state, pos, spell);

        verify(block).onHit(same(client), same(state), same(pos), same(spell));
        verify(block, never()).onHit(any(ServerLevel.class), any(BlockPos.class), any(EntityProjectileSpell.class));
        verifyNoMoreInteractions(block);
        verifyNoInteractions(client, spell);
    }

    @Test
    void newCallbackExecutesRealServerHandlerForMissingResolver() {
        ServerLevel server = mock(ServerLevel.class);
        // Default mock resolver() is null: a real server-handler early exit.
        api.onHit((Level) server, state, pos, spell);

        verify(block, times(1)).onHit(same(server), same(pos), same(spell));
        verify(spell).resolver();
        verify(spell, times(1)).remove(RemovalReason.DISCARDED);
        verifyNoMoreInteractions(spell);
        verifyNoInteractions(server);
    }

    @Test
    void oldCallbackStillExecutesRealHandlerWithoutForwardingBackToNewCallback() {
        ServerLevel server = mock(ServerLevel.class);

        api.onHit(server, pos, spell);

        verify(block, times(1)).onHit(same(server), same(pos), same(spell));
        verifyNoMoreInteractions(block); // Also detects old -> new -> old recursion.
        verify(spell).resolver();
        verify(spell, times(1)).remove(RemovalReason.DISCARDED);
        verifyNoMoreInteractions(spell);
        verifyNoInteractions(server); // No inherited default's getBlockState lookup.
    }

    @Test
    void newCallbackWithNoTargetEmitsFailureParticlesAndDiscardsSpell() {
        ServerLevel server = mock(ServerLevel.class);
        WarpingSpellPrismTile tile = mock(WarpingSpellPrismTile.class);
        SpellResolver resolver = mock(SpellResolver.class);
        when(spell.resolver()).thenReturn(resolver);
        when(server.getBlockEntity(pos)).thenReturn(tile);
        // tile.getHitResult() defaults to null; the real handler must stop here.

        api.onHit((Level) server, state, pos, spell);

        verify(block, times(1)).onHit(same(server), same(pos), same(spell));
        verify(server).getBlockEntity(pos);
        verify(server).sendParticles(ParticleTypes.ANGRY_VILLAGER,
                7.5, 64.5, -2.5, 1, 0.0, 0.0, 0.0, 0.0);
        verifyNoMoreInteractions(server);
        verify(tile).getHitResult();
        verifyNoMoreInteractions(tile);
        verify(spell).resolver();
        verify(spell, times(1)).remove(RemovalReason.DISCARDED);
        verifyNoMoreInteractions(spell);
        verifyNoInteractions(resolver);
    }

    @Test
    void dependencyDefaultReallyIsNoOpForAnOldOnlyImplementation() {
        // Permanent negative-control fixture, NOT a replacement for the real block.
        // Uses the real inherited default, not a test-written no-op callback.
        LegacyOnlyPrism legacy = new LegacyOnlyPrism();
        IPrismaticBlock legacyApi = legacy;
        ServerLevel server = mock(ServerLevel.class);

        legacyApi.onHit((Level) server, state, pos, spell);
        assertEquals(0, legacy.serverCalls, "AN's new default must not forward to the old callback");
        legacyApi.onHit(server, pos, spell);
        assertEquals(1, legacy.serverCalls, "The legacy overload remains callable");
        verifyNoInteractions(server, spell);
    }

    private static final class LegacyOnlyPrism implements IPrismaticBlock {
        private int serverCalls;

        @Override
        public void onHit(ServerLevel server, BlockPos pos, EntityProjectileSpell spell) {
            serverCalls++;
        }
    }
}
