package io.github.rubixtheslime.rubix.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.WorldView;

import java.util.List;
import java.util.Map;

public record DynColorData(Map<BlockPos, BlockEntry> blocks) {
    public void draw(RenderLayer layer, BlockBufferAllocatorStorage allocatorStorage, WorldView worldView, BlockColors blockColors) {
        BufferBuilder bufferBuilder = new BufferBuilder(allocatorStorage.get(layer), VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        blocks.forEach((pos, blockEntry) -> {
            blockEntry.quads.forEach(quadEntry -> {
                var color = blockColors.getColor(blockEntry.state, worldView, pos, quadEntry.bakedQuad.tintIndex());
                var a = ColorHelper.getAlphaFloat(color);
                var r = ColorHelper.getRedFloat(color);
                var g = ColorHelper.getGreenFloat(color);
                var b = ColorHelper.getBlueFloat(color);
                bufferBuilder.quad(quadEntry.entry, quadEntry.bakedQuad, r, g, b, a, quadEntry.i, quadEntry.j);
            });
        });
        layer.draw(bufferBuilder.end());
    }

    public record BlockEntry(BlockState state, List<QuadEntry> quads) {
    }

    public record QuadEntry(MatrixStack.Entry entry, BakedQuad bakedQuad, int i, int j) {
    }
}
