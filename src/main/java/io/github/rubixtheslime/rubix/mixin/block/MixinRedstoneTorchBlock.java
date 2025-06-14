package io.github.rubixtheslime.rubix.mixin.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.redfile.RedfileTrackers;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RedstoneTorchBlock.class)
public class MixinRedstoneTorchBlock {

    @WrapMethod(method = "isBurnedOut")
    private static boolean isBurnedOutWrap(World world, BlockPos pos, boolean addNew, Operation<Boolean> original) {
        RedfileTrackers.TORCH_BURNOUT.enter(pos);
        var res = original.call(world, pos, addNew);
        RedfileTrackers.TILE_TICK_BLOCK.enter(pos);
        return res;
    }

}
