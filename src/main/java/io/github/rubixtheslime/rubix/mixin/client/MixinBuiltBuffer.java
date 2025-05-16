package io.github.rubixtheslime.rubix.mixin.client;

import io.github.rubixtheslime.rubix.imixin.client.IMixinBuiltBuffer;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import net.minecraft.client.render.BuiltBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(BuiltBuffer.class)
public abstract class MixinBuiltBuffer implements IMixinBuiltBuffer {
    @Shadow public abstract ByteBuffer getBuffer();

    @Unique
    DynColorBuilder.PartialBuilt dynColorData;


    @Inject(method = "getBuffer", at = @At("RETURN"), cancellable = true)
    void getBufferMakeRO(CallbackInfoReturnable<ByteBuffer> cir) {
        cir.setReturnValue(cir.getReturnValue().asReadOnlyBuffer());
    }

    @Override
    public void rubix$setDynColor(DynColorBuilder.PartialBuilt built) {
        dynColorData = built;
    }

    @Override
    public DynColorBuilder.Built rubix$getDynColor() {
        return dynColorData == null ? null : dynColorData.build(this.getBuffer());
    }

    @Override
    public boolean rubix$hasDynColor() {
        return dynColorData != null;
    }
}
