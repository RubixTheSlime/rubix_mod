package io.github.rubixtheslime.rubix.mixin.fabric.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TerrainRenderContext.class, remap = false)
public class MixinTerrainRenderContext {

    @Inject(method = "bufferModel", at = @At("HEAD"), cancellable = true)
    void bufferModelUnhook(BlockStateModel model, BlockState blockState, BlockPos blockPos, CallbackInfo ci) {
        if (EnabledMods.GAY_GRASS_VIDEO) ci.cancel();
    }

}
