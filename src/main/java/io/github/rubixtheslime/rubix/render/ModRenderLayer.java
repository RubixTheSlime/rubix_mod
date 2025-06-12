package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import io.github.rubixtheslime.rubix.redfile.client.ColorMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.TriState;

import java.util.OptionalDouble;

public class ModRenderLayer {
//    public static final RenderLayer HIGHLIGHT_VISIBLE = highlightOf(ModRenderPipeline.HIGHLIGHT_VISIBLE);
//    public static final RenderLayer HIGHLIGHT_HIDDEN = highlightOf(ModRenderPipeline.HIGHLIGHT_HIDDEN);
//    public static final RenderLayer HIGHLIGHT_ALL = highlightOf(ModRenderPipeline.HIGHLIGHT_ALL);
//    public static final RenderLayer HIGHLIGHT_VISIBLE_CULL = highlightOf(ModRenderPipeline.HIGHLIGHT_VISIBLE_CULL);
//    public static final RenderLayer HIGHLIGHT_HIDDEN_CULL = highlightOf(ModRenderPipeline.HIGHLIGHT_HIDDEN_CULL);
//    public static final RenderLayer HIGHLIGHT_ALL_CULL = highlightOf(ModRenderPipeline.HIGHLIGHT_ALL_CULL);

    public static final XrayRenderLayer HIGHLIGHT = new XrayRenderLayer(
        "highlight",
        786432,
        false,
        ModRenderPipeline.HIGHLIGHT_HIDDEN,
        ModRenderPipeline.HIGHLIGHT_VISIBLE,
        RenderLayer.MultiPhaseParameters.builder().build(false)
    );

    public static final XrayRenderLayer HIGHLIGHT_REDFILE_MULTISHAPE_TILE = new XrayRenderLayer(
        "highlight_redfile_multishape_tile",
        786432,
        false,
        ModRenderPipeline.HIGHLIGHT_TEX_HIDDEN,
        ModRenderPipeline.HIGHLIGHT_TEX_VISIBLE,
        RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(ColorMap.REDFILE_MULTISHAPE_TILES, TriState.DEFAULT, true)).build(false)
    );

    public static final RenderLayer PIE_CHART_CHUNKS = RenderLayer.of(
        "highlight_redfile_multishape_tile",
        786432,
        false,
        true,
        ModRenderPipeline.PIE_CHART_QUADS,
        RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(UncertainPieChart.QUARTER_CIRCLE, TriState.DEFAULT, false)).build(false)
    );

    public static final RenderLayer HIGHLIGHT_LINE = RenderLayer.of(
        "highlight_line",
        1536,
        false,
        false,
        ModRenderPipeline.HIGHLIGHT_LINE,
        RenderLayer.MultiPhaseParameters.builder().lineWidth(new RenderPhase.LineWidth(OptionalDouble.of(5))).build(false)
    );

//    private static RenderLayer highlightOf(RenderPipeline pipeline) {
//        var name = pipeline.getLocation().getPath().split("/")[1];
//        return RenderLayer.of(
//            name,
//            786432,
//            false,
//            true,
//            pipeline,
//            RenderLayer.MultiPhaseParameters.builder().build(false)
//        );
//    }

}
