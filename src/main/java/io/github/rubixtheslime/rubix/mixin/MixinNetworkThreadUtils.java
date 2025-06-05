package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(NetworkThreadUtils.class)
public class MixinNetworkThreadUtils {
    @Unique
    private static final Map<Packet<?>, ServerWorld> PACKET_WORLD_MAP = new ConcurrentHashMap<>();

    @Inject(method = "forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V", at = @At("HEAD"))
    private static <T extends PacketListener> void forceMainThreadWrap(Packet<T> packet, T listener, ServerWorld world, CallbackInfo ci) {
        PACKET_WORLD_MAP.put(packet, world);
    }

    @WrapOperation(method = "method_11072", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/Packet;apply(Lnet/minecraft/network/listener/PacketListener;)V"))
    private static <T extends PacketListener> void applyWrap(Packet<T> instance, T t, Operation<Void> original) {
        var world = PACKET_WORLD_MAP.remove(instance);
        RedfileManager.enterWorld(world);
        original.call(instance, t);
        RedfileManager.exitWorld(world);
    }


}
