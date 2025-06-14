package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.command.client.ModClientCommands;
import io.github.rubixtheslime.rubix.gaygrass.PrideFlagManager;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.network.RedfileTranslationPacket;
import io.github.rubixtheslime.rubix.redfile.client.RedfileClientInit;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

public class RubixModClient implements ClientModInitializer {
    public static PrideFlagManager prideFlagManager;

    @Override
    public void onInitializeClient() {
        ModClientCommands.init();
        ModKeyBinds.init();
        ModHud.init();
        if (EnabledMods.REDFILE) {
            RedfileClientInit.init();
            RubixMod.RUBIX_MOD_CHANNEL.registerClientbound(RedfileResultPacket.class, (message, access) -> {
                var client = MinecraftClient.getInstance();
                ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().addResult(message);
            });
            RubixMod.RUBIX_MOD_CHANNEL.registerClientbound(RedfileTranslationPacket.class, (message, access) -> {
                var client = MinecraftClient.getInstance();
                ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().setTranslation(message);
            });

        }
    }

}
