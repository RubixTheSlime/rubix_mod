package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import io.github.rubixtheslime.rubix.imixin.client.IMixinRenderLayer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import org.lwjgl.opengl.GL14;

public class XrayRenderLayer extends RenderLayer.MultiPhase implements IMixinRenderLayer.MultiPhase {
    private float xrayAlpha;
    private float regularAlpha;
    private final RenderPipeline regularPipeline;

    public XrayRenderLayer(String name, int size, boolean hasCrumbling, RenderPipeline xrayPipeline, RenderPipeline regularPipeline, RenderLayer.MultiPhaseParameters phases) {
        super(name, size, hasCrumbling, true, xrayPipeline, phases);
        this.regularPipeline = regularPipeline;
    }

    @Override
    public int rubix$getAdditionalPasses() {
        return 1;
    }

    @Override
    public void rubix$setupAdditionalPass(RenderPass renderPass, int index) {
        GL14.glBlendColor(0, 0, 0, regularAlpha);
        renderPass.setPipeline(regularPipeline);
    }

    @Override
    public void draw(BuiltBuffer buffer) {
        GL14.glBlendColor(0, 0, 0, xrayAlpha);
        super.draw(buffer);
    }

    public void setXrayAlpha(float xrayAlpha) {
        this.xrayAlpha = xrayAlpha;
    }

    public void setRegularAlpha(float regularAlpha) {
        this.regularAlpha = regularAlpha;
    }

    public void setAlphas(float regularAlpha, float xrayAlpha) {
        setRegularAlpha(regularAlpha);
        setXrayAlpha(xrayAlpha);
    }
}
