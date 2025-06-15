package io.github.rubixtheslime.rubix.block;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTrackers;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

import java.util.function.Supplier;

public abstract class SuppressionBlock extends Block {
    private SuppressionBlock(Settings settings) {
        super(settings);
    }

    public static SuppressionBlock of(Settings settings) {
        return EnabledMods.SUPPRESSION_BLOCKS ? new Actual(settings) : new Empty(settings);
    }

    public boolean isAround(Supplier<BlockPos> posSupplier) {
        return isAround(RedfileManager::getCurrentWorld, posSupplier);
    }

    public abstract boolean isAround(Supplier<WorldView> worldSupplier, Supplier<BlockPos> posSupplier);

    public boolean isAroundRedfile() {
        return isAround(RedfileManager::getCurrentWorld, () -> RedfileManager.getCurrent().getPosForRedfile());
    }

    private static class Empty extends SuppressionBlock {
        private Empty(Settings settings) {
            super(settings);
        }

        @Override
        public boolean isAround(Supplier<WorldView> worldSupplier, Supplier<BlockPos> posSupplier) {
            return false;
        }
    }

    private static class Actual extends SuppressionBlock {

        private Actual(Settings settings) {
            super(settings);
        }

        @Override
        public boolean isAround(Supplier<WorldView> worldSupplier, Supplier<BlockPos> posSupplier) {
            var redfileTracker = RedfileManager.getCurrentRaw();
            RedfileTrackers.SUPPRESSION_BLOCK.enter(posSupplier);
            var res = isAroundInner(worldSupplier, posSupplier);
            RedfileManager.enter(redfileTracker);
            return res;
        }

        public boolean isAroundInner(Supplier<WorldView> worldSupplier, Supplier<BlockPos> posSupplier) {
            var pos = posSupplier.get();
            var world = worldSupplier.get();
            if (pos == null || world == null)
                return false;

            if (world.getBlockState(pos).isOf(this))
                return true;
            for (Direction direction : DIRECTIONS) {
                if (world.getBlockState(pos.offset(direction)).isOf(this))
                    return true;
            }
            return false;
        }
    }

}
