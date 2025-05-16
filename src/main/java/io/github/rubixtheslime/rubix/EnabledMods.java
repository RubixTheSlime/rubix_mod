package io.github.rubixtheslime.rubix;

public class EnabledMods {
    ///  this is public only because the runtime shits itself on hotswap if it isn't. pretty please don't use it
    public static final io.github.rubixtheslime.rubix.RubixConfig.EnabledMods enabledMods = RubixMod.CONFIG.enabledMods;
    public static final boolean REDFILE = enabledMods.enableRedfile();
    public static final boolean GAY_GRASS = enabledMods.enableGayGrass();
    public static final boolean GAY_GRASS_VIDEO = enabledMods.enableGayGrass() && enabledMods.enableGayGrassVideo();
    public static final boolean DEBUG = enabledMods.enableDebug();
}
