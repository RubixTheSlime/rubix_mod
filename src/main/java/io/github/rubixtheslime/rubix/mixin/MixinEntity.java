package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.redfile.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class MixinEntity implements RedfileTracker {

    @Shadow private BlockPos blockPos;

    @WrapOperation(method = "tickBlockCollisions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V")
    )
    public void blockStepOnWrap(Block instance, World world, BlockPos pos, BlockState state, Entity entity, Operation<Void> original) {
        RedfileTrackers.BLOCK_STEP.enter(pos);
        original.call(instance, world, pos, state, entity);
        RedfileManager.enter(entity);
    }

    @Override
    public BlockPos getPosForRedfile() {
        return blockPos.toImmutable();
    }

    @Override
    public RedfileTag getTagForRedfile() {
        return RedfileTags.ENTITY_TICK;
    }
}
