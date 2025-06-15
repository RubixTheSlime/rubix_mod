package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.redfile.client.RedfileHud;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

public class ModHud {
    public static void init() {
        HudLayerRegistrationCallback.EVENT.register(wrapper ->
            wrapper.addLayer(new IdentifiedLayer() {
                @Override
                public Identifier id() {
                    return Identifier.of(RubixMod.MOD_ID, "hud_render");
                }

                @Override
                public void render(DrawContext context, RenderTickCounter tickCounter) {
                    var client = MinecraftClient.getInstance();
                    var hudEnabled = !client.options.hudHidden;
                    if (EnabledMods.REDFILE && hudEnabled) {
                        RedfileHud.renderRedfileHud(context, client, tickCounter);
                    }
                }
            })
        );

        if (EnabledMods.REDFILE) {
            RedfileHud.init();
        }
    }
}
