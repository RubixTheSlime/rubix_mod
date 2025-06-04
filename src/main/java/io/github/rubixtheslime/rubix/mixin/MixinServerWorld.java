package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = ServerWorld.class)
public abstract class MixinServerWorld {

    @WrapMethod(method = "tick")
    public void tickHead(BooleanSupplier shouldKeepTicking, Operation<Void> original) {
        RedfileManager.enterAndTickWorld((ServerWorld) (Object) this);
        original.call(shouldKeepTicking);
        RedfileManager.exitWorld();
    }

    // random ticks handled in AbstractBlockState

    @Inject(method = "processBlockEvent", at = @At("HEAD"))
    public void processBlockEventEnter(BlockEvent event, CallbackInfoReturnable<Boolean> cir) {
        RedfileManager.enter(event);
    }

    @Inject(method = "processSyncedBlockEvents", at = @At("TAIL"))
    public void processSyncedBlockEventsExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

    // entity (tracker) movement handled in ServerChunkLoadingManager

    @Inject(method = "tickEntity",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    public void tickEntityEnter(Entity entity, CallbackInfo ci) {
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
