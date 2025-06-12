package io.github.rubixtheslime.rubix.mixin;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(
        method = {"onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/server/network/ConnectedClientData;)V"},
        at = {@At(
            value = "NEW",
            target = "Lnet/minecraft/network/packet/s2c/play/SynchronizeRecipesS2CPacket;"
        )}
    )
    private void hookOnPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData arg, CallbackInfo ci) {
        if (EnabledMods.REDFILE) RubixMod.RUBIX_MOD_CHANNEL.serverHandle(player).send(RedfileManager.getTranslationPacket());
    }
}
