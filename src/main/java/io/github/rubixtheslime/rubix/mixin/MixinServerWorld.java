package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(value = ServerWorld.class)
public abstract class MixinServerWorld {

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickHead(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        RedfileManager.enterAndTickWorld((ServerWorld) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tickTail(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        RedfileManager.exitWorld();
    }

    @Inject(method = "tickBlock", at = @At("HEAD"))
    public void tickBlockEnter(BlockPos pos, Block block, CallbackInfo ci) {
        RedfileManager.enter(pos);
    }

    @Inject(method = "tickFluid", at = @At("HEAD"))
    public void tickFluidEnter(BlockPos pos, Fluid fluid, CallbackInfo ci) {
        RedfileManager.enter(pos);
    }

    // tile tick exit handled by WorldTickScheduler
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

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"))
    public void tickEntitiesExit(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        RedfileManager.exit();
    }

    // block entities handled in World

}
