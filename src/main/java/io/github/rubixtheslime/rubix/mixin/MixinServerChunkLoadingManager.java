package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.RedfileTags;
import io.github.rubixtheslime.rubix.redfile.RedfileTracker;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        RedfileManager.enterLeaf(entityTracker);
    }

    @Inject(method = "tickEntityMovement", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    public void tickEntityMovementExit(CallbackInfo ci) {
        RedfileManager.exit();
    }

    @Mixin(ServerChunkLoadingManager.EntityTracker.class)
    public static class EntityTracker implements RedfileTracker {

        @Shadow @Final public Entity entity;

        @Override
        public BlockPos getPosForRedfile() {
            return entity.getBlockPos().toImmutable();
        }

        @Override
        public RedfileTag getTagForRedfile() {
            return RedfileTags.ENTITY_MOVEMENT;
        }
    }

}
