package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileTracked;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockEvent.class)
public class MixinBlockEvent implements RedfileTracked {

    @Shadow @Final private BlockPos pos;

    @Override
    public BlockPos rubix$getPosForRedfile() {
        return pos;
    }
}
