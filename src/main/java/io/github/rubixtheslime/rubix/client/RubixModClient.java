package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.command.client.ModClientCommands;
import io.github.rubixtheslime.rubix.gaygrass.PrideFlagManager;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.network.RedfileResultPacket;
import io.github.rubixtheslime.rubix.redfile.client.RedfileClientInit;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class RubixModClient implements ClientModInitializer {
    public static PrideFlagManager prideFlagManager;

    @Override
    public void onInitializeClient() {
        ModClientCommands.init();
        ModKeyBinds.init();
        RubixModHud.init();
        if (EnabledMods.REDFILE) {
            RedfileClientInit.init();
            RubixMod.RUBIX_MOD_CHANNEL.registerClientbound(RedfileResultPacket.class, (message, access) -> {
                var client = MinecraftClient.getInstance();
                ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().addResult(message, client.world);
            });
        }
    }
}
