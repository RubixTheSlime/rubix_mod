package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public class MixinWorldTickScheduler<T> {

    @Inject(method = "tick(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void tickBlockFluidExit(BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {
        RedfileManager.exit();
    }

}
