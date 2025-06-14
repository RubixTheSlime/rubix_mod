package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;


public class ModKeyBinds {
    private static final List<DebugIndexKey> debugIndexes = new ArrayList<>(10);
    private static KeyBinding incModifier;
    private static KeyBinding decModifier;
    private static KeyBinding toggleRedfileSelect;
    private static KeyBinding toggleRedfileXray;
    private static KeyBinding moveRedfileLayerUp;
    private static KeyBinding moveRedfileLayerDown;

    public static void init() {
        toggleRedfileSelect = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.redfile_toggle_select",
            GLFW.GLFW_KEY_V,
            "category.rubix.redfile"
        ));
        moveRedfileLayerUp = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.redfile_layer_up",
            GLFW.GLFW_KEY_UNKNOWN,
            "category.rubix.redfile"
        ));
        moveRedfileLayerDown = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.redfile_layer_down",
            GLFW.GLFW_KEY_UNKNOWN,
            "category.rubix.redfile"
        ));
        toggleRedfileXray = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.redfile_toggle_xray",
            GLFW.GLFW_KEY_UNKNOWN,
            "category.rubix.redfile"
        ));


        incModifier = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.debug_inc",
            GLFW.GLFW_KEY_KP_ADD,
            "category.rubix.debug"
        ));
        decModifier = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.rubix.debug_dec",
            GLFW.GLFW_KEY_KP_SUBTRACT,
            "category.rubix.debug"
        ));
        for (int i = 0; i < 10; i++) {
            debugIndexes.addLast(new DebugIndexKey(i));
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (var data : debugIndexes) {
                int i = data.index;
                while (data.keyBinding.wasPressed()){
                    Text message = null;
                    if (incModifier.isPressed()) {
                        RDebug.setI(i, RDebug.getI(i) + 1);
                        message = Text.translatable("text.rubix.debug.inc", data.index, RDebug.getI(i));
                    } else if (decModifier.isPressed()) {
                        RDebug.setI(i, RDebug.getI(i) - 1);
                        message = Text.translatable("text.rubix.debug.dec", data.index, RDebug.getI(i));
                    } else {
                        RDebug.setB(i, !RDebug.getB(i));
                        message = Text.translatable("text.rubix.debug.toggle", data.index, RDebug.getB(i));
                    }
                    if (message != null && client.player != null) {
                        client.player.sendMessage(message, true);
                    }
                }
            }

            if (EnabledMods.REDFILE) {
                var redfileManager = ((IMixinMinecraftClient) client).rubix$getRedfileResultManager();
                while (toggleRedfileSelect.wasPressed()) {
                    redfileManager.toggleLookingAt(client);
                }
                while (moveRedfileLayerUp.wasPressed()) {
                    redfileManager.moveLayer(client.world, true);
                }
                while (moveRedfileLayerDown.wasPressed()) {
                    redfileManager.moveLayer(client.world, false);
                }
                while (toggleRedfileXray.wasPressed()) {
                    RubixMod.CONFIG.redfileOptions.xrayEnabled(!RubixMod.CONFIG.redfileOptions.xrayEnabled());
                }
            }
        });
    }

    static class DebugIndexKey {
        final int index;
        final KeyBinding keyBinding;

        DebugIndexKey(int index) {
            this.index = index;
            try {
                this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.rubix.debug_" + index,
                    InputUtil.Type.KEYSYM,
                    (Integer) GLFW.class.getField("GLFW_KEY_KP_" + index).get(0),
                    "category.rubix.debug"
                ));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
