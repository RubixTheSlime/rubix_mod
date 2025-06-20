package io.github.rubixtheslime.rubix;

import io.github.rubixtheslime.rubix.redfile.client.ColorMapMode;
import io.github.rubixtheslime.rubix.redfile.UncertaintyType;
import io.github.rubixtheslime.rubix.render.ColorMode;
import io.wispforest.owo.config.annotation.*;

@SuppressWarnings("unused")
@Modmenu(modId = "rubix")
@Config(name = "rubix", wrapperName = "RubixConfig")
public class RubixConfigModel {

    @Nest public EnabledMods enabledMods = new EnabledMods();
    @Nest public RedfileOptions redfileOptions = new RedfileOptions();
    @Nest public GayGrassOptions gayGrassOptions = new GayGrassOptions();
    @Nest public MiscOptions miscOptions = new MiscOptions();
    @Nest public DebugOptions debugOptions = new DebugOptions();

    public static class EnabledMods {
        @RestartRequired
        public boolean enableGayGrass = false;
        @RestartRequired
        public boolean enableRedfile = false;
        @RestartRequired
        public boolean enableSuppressionBlocks = false;
        @RestartRequired
        public boolean enableDebug = false;

        @SectionHeader("danger_zone")
        @RestartRequired
        public boolean enableGayGrassVideo = false;
//        @RestartRequired
//        public boolean enableTransWorld = false;

    }

    public static class RedfileOptions {
        @RangeConstraint(min = 0, max = 1)
        public float opacity = 0.75f;
        @RangeConstraint(min = 0, max = 1)
        public float xray = 0.3f;
        public boolean xrayEnabled = true;
        @RangeConstraint(min = 0, max = 100)
        public float confidence = 95f;
        public boolean displayConfidenceHud = false;
        @Hook public ColorMode pieColorMode = ColorMode.RED_GREEN_BLUE;
        @Hook public boolean pieUncertainty = true;
        @Hook public UncertaintyType uncertaintyMode = UncertaintyType.ERROR;
        @Hook public String selectionColor = "808080";

        @Hook public ColorMapMode colorMapMode = ColorMapMode.RGB_GRADIENT;

        @SectionHeader("redfile_gradient")

        @Hook public String gradientP1Color = "000000";
        @Hook public String gradientP10Color = "000000";
        @Hook public String gradientP100Color = "000000";
        @Hook public String gradientN1Color = "550055";
        @Hook public String gradientN10Color = "0000ff";
        @Hook public String gradientN100Color = "00ccff";
        @Hook public String gradientU1Color = "00ff00";
        @Hook public String gradientU10Color = "ffcc00";
        @Hook public String gradientU100Color = "ff0000";
        @Hook public String gradientM1Color = "ff44ff";
        @Hook public String gradientM10Color = "ffffff";
        @Hook public String gradientM100Color = "ffffff";
        @Hook public String gradientS1Color = "ffffff";

        @SectionHeader("redfile_multishape")

        @Hook public String multishapePColor = "5b8efd";
        @Hook public String multishapeNColor = "725def";
        @Hook public String multishapeUColor = "dd217d";
        @Hook public String multishapeMColor = "ff5f00";
        @Hook public String multishapeSColor = "ffb00d";
    }

    public static class GayGrassOptions {
        @RestartRequired
        public boolean colorAll = false;
        public boolean threadedRebuild = false;
        public boolean matchRotate = false;
        public float rotateDamp = 0.01f;
        public float rotateSpeed = 5;
    }

    public static class MiscOptions {
        public boolean forceSyncWorldThreads = false;
    }

    public static class DebugOptions {
        @Hook public boolean debugBool0 = false;
        @Hook public boolean debugBool1 = false;
        @Hook public boolean debugBool2 = false;
        @Hook public boolean debugBool3 = false;
        @Hook public boolean debugBool4 = false;
        @Hook public boolean debugBool5 = false;
        @Hook public boolean debugBool6 = false;
        @Hook public boolean debugBool7 = false;
        @Hook public boolean debugBool8 = false;
        @Hook public boolean debugBool9 = false;

        @Hook public int debugInt0 = 0;
        @Hook public int debugInt1 = 0;
        @Hook public int debugInt2 = 0;
        @Hook public int debugInt3 = 0;
        @Hook public int debugInt4 = 0;
        @Hook public int debugInt5 = 0;
        @Hook public int debugInt6 = 0;
        @Hook public int debugInt7 = 0;
        @Hook public int debugInt8 = 0;
        @Hook public int debugInt9 = 0;

        @Hook public float debugFloat0 = 0;
        @Hook public float debugFloat1 = 0;
        @Hook public float debugFloat2 = 0;
        @Hook public float debugFloat3 = 0;
        @Hook public float debugFloat4 = 0;
        @Hook public float debugFloat5 = 0;
        @Hook public float debugFloat6 = 0;
        @Hook public float debugFloat7 = 0;
        @Hook public float debugFloat8 = 0;
        @Hook public float debugFloat9 = 0;

        @Hook public String debugString0 = "";
        @Hook public String debugString1 = "";
        @Hook public String debugString2 = "";
        @Hook public String debugString3 = "";
        @Hook public String debugString4 = "";
        @Hook public String debugString5 = "";
        @Hook public String debugString6 = "";
        @Hook public String debugString7 = "";
        @Hook public String debugString8 = "";
        @Hook public String debugString9 = "";

    }

}
