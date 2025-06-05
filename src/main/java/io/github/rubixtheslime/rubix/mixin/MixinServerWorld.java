package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.block.ModBlocks;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.EntityLookupView;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;

@Mixin(value = ServerWorld.class)
public abstract class MixinServerWorld extends World implements EntityLookupView, StructureWorldAccess {

    @Shadow @Final private List<BlockEvent> blockEventQueue;

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @WrapMethod(method = "tick")
    public void tickHead(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        var worldThis = (ServerWorld) (Object) this;
        RedfileManager.enterWorld(worldThis);
        RedfileManager.tickWorld(worldThis);
        original.call(shouldKeepTicking);
        RedfileManager.exitWorld(worldThis);
    }

    // random ticks handled in AbstractBlockState

    @WrapMethod(method = "processBlockEvent")
    public boolean processBlockEventCancel(BlockEvent event, Operation<Boolean> original) {
        if (ModBlocks.BLOCK_EVENT_SUPPRESSION_BLOCK.isAround(event::pos)) {
            this.blockEventQueue.add(event);
            return false;
        }
        RedfileManager.enter(event);
        return original.call(event);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("TAIL"))
    public void processSyncedBlockEventsExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

    // entity (tracker) movement handled in ServerChunkLoadingManager

    @Inject(method = "method_31420", at = @At("HEAD"))
    public void tickEntityEnter(TickManager tickManager, Profiler profiler, Entity entity, CallbackInfo ci) {
        RedfileManager.enter(entity);
    }

    @Inject(method = "tickPassenger", at = @At("HEAD"))
    public void tickPassengerEnter(Entity vehicle, Entity passenger, CallbackInfo ci) {
        RedfileManager.enter(passenger);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickBlockEntities()V"))
    public void tickEntitiesExit(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        RedfileManager.exit();
    }

    // block entities handled in World

}
