package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
public class MixinServerChunkLoadingManager {
    @Inject(method = "tickEntityMovement", at = @At(
        value = "FIELD",
        target = "Lnet/minecraft/server/world/ServerChunkLoadingManager$EntityTracker;trackedSection:Lnet/minecraft/util/math/ChunkSectionPos;"
    ), slice = @Slice(
        to = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/ChunkSectionPos;from(Lnet/minecraft/world/entity/EntityLike;)Lnet/minecraft/util/math/ChunkSectionPos;")
    ))
    public void tickEntityMovementEnter(CallbackInfo ci, @Local ServerChunkLoadingManager.EntityTracker entityTracker) {
        RedfileManager.enter(entityTracker.entity);
    }

    @Inject(method = "tickEntityMovement", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    public void tickEntityMovementExit(CallbackInfo ci) {
        RedfileManager.exit();
    }
}
