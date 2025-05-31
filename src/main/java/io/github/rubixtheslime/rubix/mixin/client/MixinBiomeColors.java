package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.util.MoreColor;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.ColorResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {

    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private static void getColorGayify(BlockRenderView world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
        if (EnabledMods.GAY_GRASS && !EnabledMods.GAY_GRASS_ALL) {
            gayify(pos, cir);
        }
    }

    @Inject(method = "getWaterColor", at = @At("RETURN"), cancellable = true)
    private static void getWaterColorGayify(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (EnabledMods.GAY_GRASS_ALL) {
            gayify(pos, cir);
        }
    }

    @Unique
    private static void gayify(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        int initial = cir.getReturnValue();
        cir.setReturnValue(MoreColor.alphaBlend(RubixModClient.prideFlagManager.getColor(pos, false), initial));
    }

}
