package io.github.rubixtheslime.rubix;

import net.fabricmc.loader.api.FabricLoader;

import static io.github.rubixtheslime.rubix.RubixMod.CONFIG;

public class EnabledMods {
    private static final io.github.rubixtheslime.rubix.RubixConfig.EnabledMods enabledMods = CONFIG.enabledMods;
    private static final FabricLoader loader = FabricLoader.getInstance();
    public static final boolean EX_LITHIUM = loader.isModLoaded("lithium");

    public static final boolean DEBUG = enabledMods.enableDebug();
    public static final boolean GAY_GRASS = enabledMods.enableGayGrass();
    public static final boolean GAY_GRASS_VIDEO = GAY_GRASS && enabledMods.enableGayGrassVideo();
    public static final boolean GAY_GRASS_ALL = GAY_GRASS_VIDEO || CONFIG.gayGrassOptions.colorAll();
    public static final boolean REDFILE = enabledMods.enableRedfile();
    public static final boolean SUPPRESSION_BLOCKS = enabledMods.enableSuppressionBlocks();
}
