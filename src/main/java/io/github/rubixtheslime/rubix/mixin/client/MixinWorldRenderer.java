package io.github.rubixtheslime.rubix.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {
    @Shadow private @Nullable ClientWorld world;

    @Shadow @Final private MinecraftClient client;

    @Shadow private Frustum frustum;

    @Shadow public abstract void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix);

    @Inject(method = "render", at = @At("TAIL"))
    public void render(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!EnabledMods.REDFILE) return;
        MatrixStack stack = new MatrixStack();
        stack.multiplyPositionMatrix(positionMatrix);
//        stack.translate(client.gameRenderer.getCamera().getPos());
        ((IMixinMinecraftClient) client).rubix$getRedfileResultManager().render(stack, client.gameRenderer.getCamera().getPos(), frustum, world);
    }
}
