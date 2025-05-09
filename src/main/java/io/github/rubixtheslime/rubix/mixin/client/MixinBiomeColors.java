package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
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

import java.util.Random;

@Mixin(BiomeColors.class)
public class MixinBiomeColors {
    @Unique
    private static final Random random = new Random(0);

    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true)
    private static void getWaterColorGreenScreen(BlockRenderView world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (DynColorBuilder.isGreenScreening()) cir.setReturnValue(0xff0000ff);
    }

    @Inject(method = "getColor", at = @At("RETURN"), cancellable = true)
    private static void getColorGayify(BlockRenderView world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
        if (!EnabledMods.GAY_GRASS || DynColorBuilder.isPostGreenScreening()) return;
        int initial = RDebug.b0() ? (random.nextInt() | (-1 << 24)) : cir.getReturnValue();
        cir.setReturnValue(MoreColor.alphaBlend(RubixModClient.prideFlagManager.getColor(pos), initial));
    }

}
