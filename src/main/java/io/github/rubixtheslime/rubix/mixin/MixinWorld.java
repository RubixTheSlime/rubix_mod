package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import io.github.rubixtheslime.rubix.redfile.RedfileTrackers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow @Final private List<BlockEntityTickInvoker> pendingBlockEntityTickers;

    @Shadow public abstract boolean isClient();

    @Unique private final RedfileTracker.ListRandom listTracker = RedfileTracker.ListRandom.of(RedfileTags.TILE_ENTITY_TICK);

    @Inject(method = "<init>", at = @At("TAIL"))
    void init(
        MutableWorldProperties properties,
        RegistryKey<World> registryRef,
        DynamicRegistryManager registryManager,
        RegistryEntry<DimensionType> dimensionEntry,
        boolean isClient,
        boolean debugWorld,
        long seed,
        int maxChainedNeighborUpdates,
        CallbackInfo ci
    ) {
        listTracker.set(this.isClient() ? List.of() : pendingBlockEntityTickers);
    }

    @WrapMethod(method = "updateComparators")
    void updateComparatorsWrap(BlockPos pos, Block block, Operation<Void> original) {
        if (ModBlocks.COMPARATOR_SUPPRESSION_BLOCK.isAround(() -> (WorldView) this, () -> pos)) return;
        original.call(pos, block);
    }

    @WrapMethod(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    boolean setBlockStateWrap(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, Operation<Boolean> original) {
        var current = RedfileManager.getCurrentRaw();
        RedfileTrackers.SET_BLOCK.enter(pos);
        var res = original.call(pos, state, flags, maxUpdateDepth);
        RedfileManager.enter(current);
        return res;
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    public void tickBlockEntityAddEnter(CallbackInfo ci) {
        RedfileManager.enterLeaf(listTracker);
    }

    @WrapOperation(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void tickBlockEntityEnter(BlockEntityTickInvoker instance, Operation<Void> original) {
        if (ModBlocks.TILE_ENTITY_SUPPRESSION_BLOCK.isAround(() -> (WorldView) this, instance::getPos))
            return;
        RedfileManager.enter(instance);
        original.call(instance);
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    public void tickBlockEntitiesExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

    @WrapMethod(method = "tickEntity")
    <T extends Entity> void tickEntityWrap(Consumer<T> tickConsumer, T entity, Operation<Void> original) {
        if (ModBlocks.ENTITY_SUPPRESSION_BLOCK.isAround(() -> (WorldView) this, entity::getBlockPos) && !entity.isPlayer()) return;
        original.call(tickConsumer, entity);
    }

}
