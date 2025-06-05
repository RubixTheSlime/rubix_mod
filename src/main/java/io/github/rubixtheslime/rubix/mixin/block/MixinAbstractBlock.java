package io.github.rubixtheslime.rubix.mixin.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.block.ModBlocks;
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

import static io.github.rubixtheslime.rubix.redfile.RedfileTrackers.*;

@Mixin(AbstractBlock.class)
public abstract class MixinAbstractBlock {

    @Mixin(AbstractBlock.AbstractBlockState.class)
    public static abstract class MixinAbstractBlockState {

        @Shadow
        public abstract Block getBlock();

        @Inject(method = "neighborUpdate", at = @At("HEAD"))
        public void neighborUpdateEnter(World world, BlockPos pos, Block sourceBlock, WireOrientation wireOrientation, boolean notify, CallbackInfo ci) {
            NEIGHBOR_UPDATE.enter(pos);

        }

        // neighbor update exit handled implicitly by other things exiting

        @WrapMethod(method = "updateNeighbors(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V")
        public void stateChangeWrap(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, Operation<Void> original) {
            if (!world.isClient() && ModBlocks.STATE_CHANGE_SUPPRESS_BLOCK.isAround(() -> pos)) return;
            original.call(world, pos, flags, maxUpdateDepth);
        }

        @WrapMethod(method = "prepare(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;II)V")
        public void prepareWrap(WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth, Operation<Void> original) {
            var current = RedfileManager.getCurrentRaw();
            PREPARE_BLOCK.enterLeaf(pos);
            original.call(world, pos, flags, maxUpdateDepth);
            RedfileManager.enter(current);
        }

        @WrapMethod(method = "randomTick")
        public void randomTickWrap(ServerWorld world, BlockPos pos, Random random, Operation<Void> original) {
            if (ModBlocks.RANDOM_TICK_SUPPRESSION_BLOCK.isAround(() -> pos)) return;
            RANDOM_TICK.enter(pos);
            original.call(world, pos, random);
            RedfileManager.exit();
        }

        @WrapMethod(method = "onUse")
        public ActionResult onUseWrap(World world, PlayerEntity player, BlockHitResult hit, Operation<ActionResult> original) {
            RedfileManager.enter(hit);
            var res = original.call(world, player, hit);
            RedfileManager.exit();
            return res;
        }

        @WrapMethod(method = "onUseWithItem")
        public ActionResult onUseWithItemWrap(ItemStack stack, World world, PlayerEntity player, Hand hand, BlockHitResult hit, Operation<ActionResult> original) {
            RedfileManager.enter(hit);
            var res = original.call(stack, world, player, hand, hit);
            RedfileManager.exit();
            return res;
        }
    }
}
