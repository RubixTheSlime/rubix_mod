package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity {
    @Shadow public abstract ServerWorld getServerWorld();

    @WrapMethod(method = "playerTick")
    void playerTickWrap(Operation<Void> original) {
        RedfileManager.enterWorld(this.getServerWorld());
        original.call();
        RedfileManager.exitWorld(this.getServerWorld());
    }
}
