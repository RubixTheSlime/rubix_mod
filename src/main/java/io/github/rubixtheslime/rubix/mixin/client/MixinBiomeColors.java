package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
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

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    private static void getColor(BlockRenderView world, BlockPos pos, ColorResolver resolver, CallbackInfoReturnable<Integer> cir) {
        if (!EnabledMods.GAY_GRASS) return;

        var initial = world.getColor(pos, resolver);
        double x = pos.getX();
        double z = pos.getZ();
        int attempt = RubixModClient.prideFlagManager.getColor(initial, x, z);
        cir.setReturnValue(attempt & 0x00ffffff);
    }
}
