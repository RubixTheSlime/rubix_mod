package io.github.rubixtheslime.rubix;

public class GlobalHacks {
    public static boolean skipFabricBlockRender = false;
    public static boolean shouldSkipFabricBlockRender() {
        return EnabledMods.GAY_GRASS_VIDEO && skipFabricBlockRender;
    }
}
