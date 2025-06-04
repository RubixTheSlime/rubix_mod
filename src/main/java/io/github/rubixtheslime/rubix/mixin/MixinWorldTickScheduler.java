package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public class MixinWorldTickScheduler<T> {

    @Inject(method = "addTickableTicks(JI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;addTickableTick(Lnet/minecraft/world/tick/OrderedTick;)V"))
    void addTickableTicksEnter(long time, int maxTicks, CallbackInfo ci, @Local OrderedTick<?> orderedTick) {
        RedfileManager.enter(orderedTick);
    }

    @Inject(method = "tick(Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z"))
    void tickBlockFluidEnter(BiConsumer<BlockPos, T> ticker, CallbackInfo ci, @Local OrderedTick<?> orderedTick){
        RedfileManager.enter(orderedTick);
    }

    @Inject(method = "tick(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    public void tickBlockFluidExit(BiConsumer<BlockPos, T> ticker, CallbackInfo ci) {
        RedfileManager.exit();
    }

}
