package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.imixin.IMixinServerTickManager;
import net.minecraft.server.ServerTickManager;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerTickManager.class)
public abstract class MixinServerTickManager extends TickManager implements IMixinServerTickManager {

    @Shadow private long scheduledSprintTicks;

    @Shadow protected abstract void finishSprinting();

    @Unique
    private int forceSprinters = 0;

    @Override
    public void rubix$startForceSprint() {
        ++forceSprinters;
    }

    @Override
    public void rubix$stopForceSprint() {
        if (forceSprinters > 0) --forceSprinters;
    }

    @Inject(method = "isSprinting", at = @At("HEAD"), cancellable = true)
    public void isSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (forceSprinters > 0) cir.setReturnValue(true);
    }

    @Inject(method = "sprint", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerTickManager;finishSprinting()V"), cancellable = true)
    public void finishSprinting(CallbackInfoReturnable<Boolean> cir) {
        if (forceSprinters > 0) {
            if (this.scheduledSprintTicks > 0) {
                finishSprinting();
            }
            cir.setReturnValue(true);
        }
    }

}