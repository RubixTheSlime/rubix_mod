package io.github.rubixtheslime.rubix.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.imixin.client.IMixinBuiltBuffer;
import io.github.rubixtheslime.rubix.imixin.client.IMixinVertexConsumer;
import io.github.rubixtheslime.rubix.render.DynColorBuilder;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.VertexConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BufferBuilder.class, priority = 2000)
public abstract class MixinBufferBuilder implements VertexConsumer, IMixinVertexConsumer {

    @Shadow private int vertexCount;
    @Shadow @Final private VertexFormat vertexFormat;
    @Unique private DynColorBuilder deferrer = null;

    @Override
    public DynColorBuilder rubix$getDeferrer() {
        if (deferrer == null) deferrer = new DynColorBuilder(vertexFormat);
        return deferrer;
    }

    @WrapMethod(method = "endNullable")
    BuiltBuffer build(Operation<BuiltBuffer> original) {
        if (!EnabledMods.GAY_GRASS_VIDEO || deferrer == null) return original.call();
        int skipped = vertexCount;
        var colorData = deferrer.finish((BufferBuilder) (Object) this, skipped);

        var res = original.call();
        if (res == null) return null;

        ((IMixinBuiltBuffer)res).rubix$setDynColor(colorData);
        return res;
    }

}