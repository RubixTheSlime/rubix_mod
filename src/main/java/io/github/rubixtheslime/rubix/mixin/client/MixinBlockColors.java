package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.util.MoreColor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockColors.class)
public class MixinBlockColors {

    @WrapMethod(method = "registerColorProvider")
    void registerColorProvider(BlockColorProvider provider, Block[] blocks, Operation<Void> original) {
        if (!EnabledMods.GAY_GRASS_ALL || blocks[0] == Blocks.WATER) {
            original.call(provider, blocks);
            return;
        }
        original.call((BlockColorProvider) (state, world, pos, tintIndex) -> {
            var res = provider.getColor(state, world, pos, tintIndex);
            return MoreColor.alphaBlend(RubixModClient.prideFlagManager.getColor(pos, false), res);
        }, blocks);
    }

}
