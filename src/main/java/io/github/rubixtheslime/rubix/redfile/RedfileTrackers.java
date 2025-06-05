package io.github.rubixtheslime.rubix.redfile;

import org.spongepowered.asm.mixin.Unique;

public class RedfileTrackers {
    public static final RedfileTracker.Simple BLOCK_EVENT = RedfileTracker.Simple.of(RedfileTags.BLOCK_EVENT);
    public static final RedfileTracker.Simple BLOCK_STEP = RedfileTracker.Simple.of(RedfileTags.BLOCK_STEP);
    public static final RedfileTracker.Simple NEIGHBOR_UPDATE = RedfileTracker.Simple.of(RedfileTags.NEIGHBOR_UPDATE);
    public static final RedfileTracker.Simple RANDOM_TICK = RedfileTracker.Simple.of(RedfileTags.RANDOM_TICK);
    public static final RedfileTracker.Simple PREPARE_BLOCK = RedfileTracker.Simple.of(RedfileTags.PREPARE_BLOCK);
    public static final RedfileTracker.Simple TORCH_BURNOUT = RedfileTracker.Simple.of(RedfileTags.TORCH_BURNOUT);
    public static final RedfileTracker.Simple SET_BLOCK = RedfileTracker.Simple.of(RedfileTags.SET_BLOCK);
    public static final RedfileTracker.Simple STATE_CHANGE_UPDATE = RedfileTracker.Simple.of(RedfileTags.STATE_CHANGE_UPDATE);
    public static final RedfileTracker.Simple TILE_TICK_BLOCK = RedfileTracker.Simple.of(RedfileTags.TILE_TICK_BLOCK);
}
