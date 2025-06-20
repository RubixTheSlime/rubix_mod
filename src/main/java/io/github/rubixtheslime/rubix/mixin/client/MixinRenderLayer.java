package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderPass;
import io.github.rubixtheslime.rubix.imixin.client.IMixinRenderLayer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderLayer.class)
public class MixinRenderLayer implements IMixinRenderLayer {

    @Mixin(RenderLayer.MultiPhase.class)
    public static class MultiPhase implements IMixinRenderLayer.MultiPhase {

        @Override
        public int rubix$getAdditionalPasses() {
            return 0;
        }

        @Override
        public void rubix$setupAdditionalPass(RenderPass pass, int index) {
        }

        @Inject(method = "draw", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(II)V"))
        void draw(BuiltBuffer buffer, CallbackInfo ci, @Local RenderPass renderPass) {
            for (int i = 0; i < rubix$getAdditionalPasses(); ++i) {
                rubix$setupAdditionalPass(renderPass, i);
                renderPass.drawIndexed(0, buffer.getDrawParameters().indexCount());
            }
        }

    }
}