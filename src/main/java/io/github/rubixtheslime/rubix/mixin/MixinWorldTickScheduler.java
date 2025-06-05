package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.imixin.IMixinWorldTickScheduler;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import java.util.function.BiConsumer;

@Mixin(WorldTickScheduler.class)
public abstract class MixinWorldTickScheduler<T> implements IMixinWorldTickScheduler {
    @Shadow public abstract void tick(long time, int maxTicks, BiConsumer<BlockPos, T> ticker);

    @Shadow protected abstract void tick(BiConsumer<BlockPos, T> ticker);

    @Inject(method = "addTickableTicks(JI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/tick/WorldTickScheduler;addTickableTick(Lnet/minecraft/world/tick/OrderedTick;)V"))
    void addTickableTicks1Enter(long time, int maxTicks, CallbackInfo ci, @Local OrderedTick<?> orderedTick) {
        RedfileManager.enter(orderedTick);
    }

    @Inject(method = "addTickableTicks(Ljava/util/Queue;Lnet/minecraft/world/tick/ChunkTickScheduler;JI)V", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/tick/ChunkTickScheduler;peekNextTick()Lnet/minecraft/world/tick/OrderedTick;"))
    void addTickableTicks2Enter(Queue<ChunkTickScheduler<T>> tickableChunkTickSchedulers, ChunkTickScheduler<T> chunkTickScheduler, long tick, int maxTicks, CallbackInfo ci, @Local OrderedTick<?> orderedTick) {
        RedfileManager.enter(orderedTick);
    }

    @WrapMethod(method = "addTickableTick")
    void addTickableTickWrap(OrderedTick<T> tick, Operation<Void> original) {
        if (ModBlocks.TILE_TICK_SUPPRESSION_BLOCK.isAround(tick::pos)) return;
        original.call(tick);
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
