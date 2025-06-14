package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.WireOrientation;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChainRestrictedNeighborUpdater.class)
public class MixinChainRestrictedNeighborUpdater {

    @WrapMethod(method = "replaceWithStateForNeighborUpdate")
    void stateUpdateWrap(Direction direction, BlockState neighborState, BlockPos pos, BlockPos neighborPos, int flags, int maxUpdateDepth, Operation<Void> original) {
        if (ModBlocks.STATE_CHANGE_SUPPRESS_BLOCK.isAroundRedfile()) return;
        original.call(direction, neighborState, pos, neighborPos, flags, maxUpdateDepth);
    }

    @WrapMethod(method = "updateNeighbors")
    void update1Wrap(BlockPos pos, Block sourceBlock, Direction except, WireOrientation orientation, Operation<Void> original) {
        if (ModBlocks.UPDATE_SUPPRESSION_BLOCK.isAroundRedfile()) return;
        original.call(pos, sourceBlock, except, orientation);
    }

    @WrapMethod(method = "updateNeighbor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;)V")
    void update2Wrap(BlockPos pos, Block sourceBlock, WireOrientation orientation, Operation<Void> original) {
        if (ModBlocks.UPDATE_SUPPRESSION_BLOCK.isAroundRedfile()) return;
        original.call(pos, sourceBlock, orientation);
    }

    @WrapMethod(method = "updateNeighbor(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/world/block/WireOrientation;Z)V")
    void update3Wrap(BlockState state, BlockPos pos, Block sourceBlock, WireOrientation orientation, boolean notify, Operation<Void> original) {
        if (ModBlocks.UPDATE_SUPPRESSION_BLOCK.isAroundRedfile()) return;
        original.call(state, pos, sourceBlock, orientation, notify);
    }

    @WrapMethod(method = "runQueuedUpdates")
    void runUpdatesWrap(Operation<Void> original) {
        var current = RedfileManager.getCurrentRaw();
        original.call();
        RedfileManager.enter(current);
    }


}
