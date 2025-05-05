package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.block.SculkSensorBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {
    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void tickBlockEntityEnter(CallbackInfo ci, @Local BlockEntityTickInvoker blockEntityTickInvoker) {
        RedfileManager.enter(blockEntityTickInvoker);
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    public void tickBlockEntitiesExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

}
