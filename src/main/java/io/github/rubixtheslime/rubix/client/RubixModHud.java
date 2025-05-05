package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.util.Util;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class RubixModHud {

    static void init() {
        HudLayerRegistrationCallback.EVENT.register(wrapper ->
                wrapper.addLayer(new IdentifiedLayer() {
                    @Override
                    public Identifier id() {
                        return Identifier.of(RubixMod.MOD_ID, "hud_render");
                    }

                    @Override
                    public void render(DrawContext context, RenderTickCounter tickCounter) {
                        var client = MinecraftClient.getInstance();
                        renderRedfileHud(context, client, tickCounter);

//                    var text = Text.translatable("rubix.trans_rights").asOrderedText();
//                    var width = textRenderer.getWidth(text);
//                    var height = textRenderer.getWrappedLinesHeight("a", 1000);
//                    context.drawText(
//                        textRenderer,
//                        text,
//                        (context.getScaledWindowWidth() - width) / 2,
//                        (context.getScaledWindowHeight() - height) / 2,
//                        -1,
//                        false
//                    );
                    }
                })
        );
    }

    private static void renderRedfileHud(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        var manager = ((IMixinMinecraftClient)client).rubix$getRedfileResultManager();
        var lookingAt = manager.getLookingAt(client);
        var sumOfSelected = manager.getSumOfSelected(client.world);
        List<String> lines = new ArrayList<>();
        if (lookingAt != null) {
            var interval = lookingAt.getInterval();
            lines.addLast(Text.translatable("rubix.hud.redfile.value", Util.formatTime(interval.getLowerBound()), Util.formatTime(interval.getUpperBound())).getString());
        }
        if (sumOfSelected != null) {
            lines.addLast(Text.translatable("rubix.hud.redfile.sum", Util.formatTime(sumOfSelected.getLowerBound()), Util.formatTime(sumOfSelected.getUpperBound())).getString());
        }
        if (lines.isEmpty()) return;
        var textRenderer = MinecraftClient.getInstance().textRenderer;

//        int textWidth = textRenderer.getWidth(text);
        int textHeight = lines.size() * 9;
        int x = context.getScaledWindowWidth() / 2 + 10;
        int y = (context.getScaledWindowHeight() - textHeight) / 2 + 1;
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(textRenderer, lines.get(i), x, y + i * 8, -1, false);
        }

    }
}
