package io.github.rubixtheslime.rubix.block;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class SuppressionBlock extends Block {

    public SuppressionBlock(Settings settings) {
        super(settings);
    }

    public boolean isAround(WorldAccess world, BlockPos pos) {

        if (world.getBlockState(pos).isOf(this))
            return true;
        for (Direction direction : DIRECTIONS) {
            if (world.getBlockState(pos.offset(direction)).isOf(this))
                return true;
        }
        return false;
    }

}
