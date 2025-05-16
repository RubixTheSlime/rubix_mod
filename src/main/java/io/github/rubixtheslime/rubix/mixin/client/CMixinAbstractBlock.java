package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.imixin.client.ICMixinAbstractBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Environment(EnvType.CLIENT)
@Mixin(AbstractBlock.class)
public class CMixinAbstractBlock implements ICMixinAbstractBlock {

    @Mixin(AbstractBlock.AbstractBlockState.class)
    public static abstract class CMixinAbstractBlockState implements ICMixinAbstractBlock.ICMixinAbstractBlockState {
        @Unique private boolean skipNextRenderTypeCheck = false;

        @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
        public void getRenderType(CallbackInfoReturnable<BlockRenderType> cir) {
//            if (skipNextRenderTypeCheck) {
//                skipNextRenderTypeCheck = false;
//                cir.setReturnValue(BlockRenderType.INVISIBLE);
//            }
        }

        @Override
        public void rubix$skipNextRenderTypeCheck() {
            skipNextRenderTypeCheck = true;
        }
    }
}
