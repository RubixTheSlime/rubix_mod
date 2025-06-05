package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.rubixtheslime.rubix.redfile.RedfileTrackers.STATE_CHANGE_UPDATE;

@Mixin(NeighborUpdater.class)
public interface MixinNeighborUpdater {

    @Inject(method = "replaceWithStateForNeighborUpdate(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)V", at = @At("HEAD"))
    private static void replaceStateWrap(WorldAccess world, Direction direction, BlockPos pos, BlockPos neighborPos, BlockState neighborState, int flags, int maxUpdateDepth, CallbackInfo ci) {
        STATE_CHANGE_UPDATE.enter(pos);
    }

}
