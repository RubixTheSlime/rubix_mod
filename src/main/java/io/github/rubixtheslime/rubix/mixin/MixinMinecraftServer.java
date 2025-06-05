package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
//    @WrapOperation(method = "runOneTask", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getChunkManager()Lnet/minecraft/server/world/ServerChunkManager;"))
//    ServerChunkManager runOneTaskWrap(ServerWorld instance, Operation<ServerChunkManager> original) {
//        RedfileManager.enterWorld(instance);
//        return original.call(instance);
//    }
//
//    @Inject(method = "runOneTask", at = @At("TAIL"))
//    void runOneTaskExit(CallbackInfoReturnable<Boolean> cir) {
//        RedfileManager.exitWorld();
//    }

}
