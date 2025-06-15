package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RedfileTags {
    public static final RedfileTag UNFAMILIAR = registerVanilla("unfamiliar", RedfileTag.of());
    public static final RedfileTag UNKNOWN = registerVanilla("unknown", RedfileTag.of());
    public static final RedfileTag UNTAGGED = registerVanilla("untagged", RedfileTag.of());

    public static final RedfileTag BLOCK_EVENT = registerVanilla("block_event", RedfileTag.of());
    public static final RedfileTag BLOCK_INTERACT = registerVanilla("block_interact", RedfileTag.of());
    public static final RedfileTag BLOCK_STEP = registerVanilla("block_step", RedfileTag.of());
    public static final RedfileTag ENTITY_MOVEMENT = registerVanilla("entity_movement", RedfileTag.of());
    public static final RedfileTag ENTITY_TICK = registerVanilla("entity_tick", RedfileTag.of());
    public static final RedfileTag NEIGHBOR_UPDATE = registerVanilla("neighbor_update", RedfileTag.of());
    public static final RedfileTag PREPARE_BLOCK = registerVanilla("prepare_block", RedfileTag.of());
    public static final RedfileTag RANDOM_TICK = registerVanilla("random_tick", RedfileTag.of());
    public static final RedfileTag SET_BLOCK = registerVanilla("set_block", RedfileTag.of());
    public static final RedfileTag STATE_UPDATE = registerVanilla("state_update", RedfileTag.of());
    public static final RedfileTag TILE_ENTITY_TICK = registerVanilla("tile_entity_tick", RedfileTag.of());
    public static final RedfileTag TILE_TICK_BLOCK = registerVanilla("tile_tick_block", RedfileTag.of());
    public static final RedfileTag TILE_TICK_FLUID = registerVanilla("tile_tick_fluid", RedfileTag.of());
    public static final RedfileTag TORCH_BURNOUT = registerVanilla("torch_burnout", RedfileTag.of());

    public static final RedfileTag SUPPRESSION_BLOCK = register("suppression_block", RedfileTag.of());

    private static RedfileTag register(String name, RedfileTag tag) {
        return Registry.register(ModRegistries.REDFILE_TAG, Identifier.of(RubixMod.MOD_ID, name), tag);
    }

    private static RedfileTag registerVanilla(String name, RedfileTag tag) {
        return Registry.register(ModRegistries.REDFILE_TAG, Identifier.ofVanilla(name), tag);
    }
}
