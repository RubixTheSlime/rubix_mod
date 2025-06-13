package io.github.rubixtheslime.rubix.redfile;

import io.github.rubixtheslime.rubix.ModRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RedfileTags {
    public static final RedfileTag UNFAMILIAR = register("unfamiliar", RedfileTag.of());
    public static final RedfileTag UNKNOWN = register("unknown", RedfileTag.of());
    public static final RedfileTag UNTAGGED = register("untagged", RedfileTag.of());

    public static final RedfileTag BLOCK_EVENT = register("block_event", RedfileTag.of());
    public static final RedfileTag BLOCK_INTERACT = register("block_interact", RedfileTag.of());
    public static final RedfileTag BLOCK_STEP = register("block_step", RedfileTag.of());
    public static final RedfileTag ENTITY_MOVEMENT = register("entity_movement", RedfileTag.of());
    public static final RedfileTag ENTITY_TICK = register("entity_tick", RedfileTag.of());
    public static final RedfileTag NEIGHBOR_UPDATE = register("neighbor_update", RedfileTag.of());
    public static final RedfileTag PREPARE_BLOCK = register("prepare_block", RedfileTag.of());
    public static final RedfileTag RAIL_POWER_SEARCH = register("rail_power_search", RedfileTag.of());
    public static final RedfileTag RANDOM_TICK = register("random_tick", RedfileTag.of());
    public static final RedfileTag SET_BLOCK = register("set_block", RedfileTag.of());
    public static final RedfileTag STATE_UPDATE = register("state_update", RedfileTag.of());
    public static final RedfileTag TILE_ENTITY_TICK = register("tile_entity_tick", RedfileTag.of());
    public static final RedfileTag TILE_TICK_BLOCK = register("tile_tick_block", RedfileTag.of());
    public static final RedfileTag TILE_TICK_FLUID = register("tile_tick_fluid", RedfileTag.of());
    public static final RedfileTag TORCH_BURNOUT = register("torch_burnout", RedfileTag.of());
    public static final RedfileTag VIBRATION = register("vibration", RedfileTag.of());

    private static RedfileTag register(String name, RedfileTag tag) {
        return Registry.register(ModRegistries.REDFILE_TAG, Identifier.ofVanilla(name), tag);
    }
}
