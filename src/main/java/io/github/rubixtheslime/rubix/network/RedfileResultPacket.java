package io.github.rubixtheslime.rubix.network;

import net.minecraft.util.Identifier;

import java.util.Map;

public record RedfileResultPacket(
    Map<Long, int[]> data,
    int trialCount,
    boolean splitTags,
    Identifier worldIdentifier
) {

}
