package io.github.rubixtheslime.rubix.redfile.client;

import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class RedfileClientInit {
    public static void init() {
        var o = RubixMod.CONFIG.redfileOptions;
        o.subscribeToColorMapMode(RedfileClientInit::dirtyColor);

        o.subscribeToGradientP1Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientP10Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientP100Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientN1Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientN10Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientN100Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientU1Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientU10Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientU100Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientM1Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientM10Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientM100Color(RedfileClientInit::dirtyColor);
        o.subscribeToGradientS1Color(RedfileClientInit::dirtyColor);

        o.subscribeToMultishapePColor(RedfileClientInit::dirtyColor);
        o.subscribeToMultishapeNColor(RedfileClientInit::dirtyColor);
        o.subscribeToMultishapeUColor(RedfileClientInit::dirtyColor);
        o.subscribeToMultishapeMColor(RedfileClientInit::dirtyColor);
        o.subscribeToMultishapeSColor(RedfileClientInit::dirtyColor);
    }

    private static void dirtyColor(Object x) {
        var client = MinecraftClient.getInstance();
        ((IMixinMinecraftClient)client).rubix$getRedfileResultManager().markColorDirty();
    }
}
