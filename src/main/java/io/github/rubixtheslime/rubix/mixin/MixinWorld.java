package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
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

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow @Final private List<BlockEntityTickInvoker> pendingBlockEntityTickers;
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
        listTracker.set(pendingBlockEntityTickers);
    }

    @Inject(method = "tickBlockEntities", at = @At("HEAD"))
    public void tickBlockEntityAddEnter(CallbackInfo ci) {
        RedfileManager.enterLeaf(listTracker);
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    public void tickBlockEntityEnter(CallbackInfo ci, @Local BlockEntityTickInvoker blockEntityTickInvoker) {
        RedfileManager.enter(blockEntityTickInvoker);
    }

    @Inject(method = "tickBlockEntities", at = @At("TAIL"))
    public void tickBlockEntitiesExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

}
