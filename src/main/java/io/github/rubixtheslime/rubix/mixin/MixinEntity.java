package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileTracked;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Entity.class)
public abstract class MixinEntity implements RedfileTracked {

    @Shadow private BlockPos blockPos;

    @Inject(method = "tickBlockCollisions",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V")
    )
    public void blockStepOnEnter(List<Entity.QueuedCollisionCheck> checks, CallbackInfo ci, @Local BlockPos blockPos) {
        RedfileManager.enter(blockPos);
    }

    @Inject(method = "tickBlockCollisions",
    at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/block/Block;onSteppedOn(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/Entity;)V")
    )
    public void blockStepOnExit(List<Entity.QueuedCollisionCheck> checks, CallbackInfo ci) {
        RedfileManager.enter((Entity) (Object) this);
    }

    @Override
    public BlockPos rubix$getPosForRedfile() {
        return blockPos.mutableCopy();
    }
}
