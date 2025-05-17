package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.class)
public abstract class MixinAbstractBlock {

    @Mixin(AbstractBlock.AbstractBlockState.class)
    public static abstract class MixinAbstractBlockState {

        @Shadow
        public abstract Block getBlock();

        @Inject(method = "neighborUpdate", at = @At("HEAD"))
        public void neighborUpdateEnter(World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
            RedfileManager.enter(pos);
        }

        // neighbor update exit handled implicitly by other things exiting

        @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"))
        public void stateChangeUpdateEnter(WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
            RedfileManager.enter(pos);
        }

        @Inject(method = "updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V", at = @At("TAIL"))
        public void stateChangeUpdateReturn(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, CallbackInfo ci) {
            RedfileManager.enter(pos);
        }

        @Inject(method = "randomTick", at = @At("HEAD"))
        public void randomTickEnter(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
            RedfileManager.enter(pos);
        }

        @Inject(method = "randomTick", at = @At("TAIL"))
        public void randomTickExit(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
            RedfileManager.exit();
        }

        // handled by packets, need to exit for neighbor updatesr even if we don't enter. entering is
        // complicated for these, but also likely negligible
        @Inject(method = "onUse", at = @At("HEAD"))
        public void onUseExit(World world, PlayerEntity player, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
            RedfileManager.exit();
        }

        @Inject(method = "onUseWithItem", at = @At("HEAD"))
        public void onUseWithItemExit(ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
            RedfileManager.exit();
        }
    }
}
