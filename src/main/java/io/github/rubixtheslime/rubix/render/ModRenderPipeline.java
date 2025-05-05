package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import io.github.rubixtheslime.rubix.RubixMod;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class ModRenderPipeline {
    public static final RenderPipeline HIGHLIGHT_HIDDEN = highlightOf("hidden", false, false, false);
    public static final RenderPipeline HIGHLIGHT_VISIBLE = highlightOf("visible", false, false, true);
    public static final RenderPipeline HIGHLIGHT_HIDDEN_CULL = highlightOf("hidden_cull", false, true, false);
    public static final RenderPipeline HIGHLIGHT_VISIBLE_CULL = highlightOf("visible_cull", false, true, true);
    public static final RenderPipeline HIGHLIGHT_TEX_HIDDEN = highlightOf("hidden_tex", true, false, false);
    public static final RenderPipeline HIGHLIGHT_TEX_VISIBLE = highlightOf("visible_tex", true, false, true);
    public static final RenderPipeline HIGHLIGHT_TEX_HIDDEN_CULL = highlightOf("hidden_tex_cull", true, true, false);
    public static final RenderPipeline HIGHLIGHT_TEX_VISIBLE_CULL = highlightOf("visible_tex_cull", true, true, true);

    public static final RenderPipeline HIGHLIGHT_LINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(RubixMod.MOD_ID, "pipeline/highlight_line"))
            .withCull(false)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build()
    );

    private static RenderPipeline highlightOf(String name, boolean tex, boolean cull, boolean visible) {
        return RenderPipelines.register(
            RenderPipeline.builder(tex ? RenderPipelines.POSITION_TEX_COLOR_SNIPPET : RenderPipelines.POSITION_COLOR_SNIPPET)
                .withLocation(Identifier.of(RubixMod.MOD_ID, "pipeline/highlight_" + name))
                .withBlend(ModBlendFunction.TRANSLUCENT_HIVE)
                .withCull(cull)
                .withDepthTestFunction(visible ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.GREATER_DEPTH_TEST)
                .withDepthWrite(false)
                .build()
        );
    }

}
