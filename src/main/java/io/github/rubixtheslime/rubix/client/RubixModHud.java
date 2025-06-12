package io.github.rubixtheslime.rubix.client;

import io.github.rubixtheslime.rubix.RDebug;
import io.github.rubixtheslime.rubix.RubixMod;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.redfile.RedfileSummarizer;
import io.github.rubixtheslime.rubix.redfile.RedfileTag;
import io.github.rubixtheslime.rubix.redfile.TaggedStats;
import io.github.rubixtheslime.rubix.render.ModRenderLayer;
import io.github.rubixtheslime.rubix.render.UncertainPieChart;
import io.github.rubixtheslime.rubix.util.MoreMath;
import io.github.rubixtheslime.rubix.util.Util;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.debug.PieChart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RubixModHud {
    private static final int CHART_WIDTH = 100;
    private static final int CHART_HEIGHT = 100;
    private static final int CHART_SAMPLES = 64;
    private static final int TEXT_PAD = 10;

    static void init() {
        HudLayerRegistrationCallback.EVENT.register(wrapper ->
                wrapper.addLayer(new IdentifiedLayer() {
                    @Override
                    public Identifier id() {
                        return Identifier.of(RubixMod.MOD_ID, "hud_render");
                    }

                    @Override
                    public void render(DrawContext context, RenderTickCounter tickCounter) {
                        var client = MinecraftClient.getInstance();
                        renderRedfileHud(context, client, tickCounter);

//                    var text = Text.translatable("rubix.trans_rights").asOrderedText();
//                    var width = textRenderer.getWidth(text);
//                    var height = textRenderer.getWrappedLinesHeight("a", 1000);
//                    context.drawText(
//                        textRenderer,
//                        text,
//                        (context.getScaledWindowWidth() - width) / 2,
//                        (context.getScaledWindowHeight() - height) / 2,
//                        -1,
//                        false
//                    );
                    }
                })
        );
    }

    private static List<Text> getRedfileLines(TaggedStats.Display display, String mainKey) {
        boolean dislayConfidence = RubixMod.CONFIG.redfileOptions.displayConfidenceHud();
        double alpha = RubixMod.CONFIG.redfileOptions.alpha();
        List<Text> lines = new ArrayList<>();
        var sumInterval = MoreMath.clampZero(display.sum().middleInterval(alpha));
        var sumRangeStr = Util.formatTimeInterval(RedfileSummarizer.CompareMode.RANGE, sumInterval, dislayConfidence, false);
        lines.addLast(Text.translatable(mainKey, sumRangeStr));
        if (display.isOnlyUntagged()) return lines;

        display.data().forEach((tag, mv) -> {
            var interval = MoreMath.clampZero(mv.middleInterval(alpha));
            var rangeStr = Util.formatTimeInterval(RedfileSummarizer.CompareMode.RANGE, interval, false, false);
            lines.addLast(Text.translatable("rubix.hud.redfile.with_tag", tag.getName(), rangeStr));
        });
        return lines;
    }

    private static void renderRedfileHud(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        var manager = ((IMixinMinecraftClient) client).rubix$getRedfileResultManager();
        var lookingAt = manager.getLookingAt(client);
        var blockStats = lookingAt == null ? null : lookingAt.stats();
        var sumStats = manager.getSumOfSelected(client.world);
        double alpha = RubixMod.CONFIG.redfileOptions.alpha();
        boolean displayConfidence = RubixMod.CONFIG.redfileOptions.displayConfidenceHud();
        var stats = sumStats == null ? blockStats : sumStats;
        if (stats == null || stats.isEmpty()) return;
        List<RedfileRenderEntry> renderEntries = new ArrayList<>();

        if (blockStats != null && !blockStats.isEmpty()) renderEntries.addLast(new RedfileRenderEntry(Color.white, Text.translatable("rubix.hud.redfile.block"), blockStats.sum(), alpha, displayConfidence, false));
        if (sumStats != null && !sumStats.isEmpty()) renderEntries.addLast(new RedfileRenderEntry(Color.white, Text.translatable("rubix.hud.redfile.sum"), sumStats.sum(), alpha, displayConfidence, false));
        if (!stats.isOnlyUntagged()) {
            for (var entry : stats.data().entrySet()) {
                var color = entry.getKey().getColor();
                var name = entry.getKey().getName();
                var mv = entry.getValue();
                renderEntries.addLast(new RedfileRenderEntry(color, name, mv, alpha, false, true));
            }
        }

        var pieChartEntries = renderEntries.stream()
            .filter(RedfileRenderEntry::includeInPieChart)
            .map(entry -> new UncertainPieChart.GaussianEntry(entry.color, entry.mv.mean(), entry.mv.stdDev()))
            .toList();
        var pieChart = pieChartEntries.isEmpty() ? null : new UncertainPieChart(pieChartEntries, CHART_SAMPLES);

        var textRenderer = MinecraftClient.getInstance().textRenderer;
        int yTextOffset = pieChart == null ? 0 : CHART_HEIGHT + 5;
        int hudHeight = renderEntries.size() * 9 + yTextOffset;
        int nameTextWidth = renderEntries.stream().map(entry -> textRenderer.getWidth(entry.name)).max(Integer::compareTo).orElse(0);
        int amountTextWidth = renderEntries.stream().map(entry -> textRenderer.getWidth(entry.mvText)).max(Integer::compareTo).orElse(0);
        int textWidth = nameTextWidth + TEXT_PAD + amountTextWidth;
        int chartWidth = pieChart == null ? 0 : CHART_WIDTH;
        int hudWidth = Math.max(textWidth, chartWidth);
        int textOffset = Math.max(hudWidth - textWidth, 0) / 2;
        int chartOffset = Math.max(hudWidth - chartWidth, 0) / 2;
        int x = context.getScaledWindowWidth() - hudWidth - 10;
        int y = context.getScaledWindowHeight() - hudHeight - 10;

        context.draw(vertexConsumers -> {
            var builder = vertexConsumers.getBuffer(ModRenderLayer.PIE_CHART_CHUNKS);
            builder.vertex(x - 5, y - 5, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x + 5 + hudWidth, y - 5, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x + 5 + hudWidth, y + 5 + hudHeight, 9).color(0x7f222222).texture(0, 0);
            builder.vertex(x - 5, y + 5 + hudHeight, 9).color(0x7f222222).texture(0, 0);
        });

        if (pieChart != null) {
            context.draw(vertexConsumers -> {
                var matrices = context.getMatrices();
                matrices.push();
                matrices.translate(x + chartOffset, y, 0);
                matrices.scale(CHART_WIDTH, CHART_HEIGHT, 1);
                pieChart.draw(vertexConsumers, matrices);
                matrices.pop();
            });
        }
        int yText = y + yTextOffset;
        int xName = x + textOffset;
        int xMv = x + textOffset + nameTextWidth + TEXT_PAD;
        for (var entry : renderEntries) {
            context.drawText(textRenderer, entry.name, xName, yText, entry.color.getRGB(), false);
            context.drawText(textRenderer, entry.mvText, xMv, yText, entry.color.getRGB(), false);
            yText += 9;
        }
    }

    private record RedfileRenderEntry(Color color, Text name, MoreMath.MeanAndVar mv, Text mvText, boolean displayConfidence, boolean includeInPieChart) {
        RedfileRenderEntry(Color color, Text name, MoreMath.MeanAndVar mv, double alpha, boolean displayConfidence, boolean includeInPieChart) {
            this(color, name, mv, getText(mv, alpha, displayConfidence), displayConfidence, includeInPieChart);
        }

        private static Text getText(MoreMath.MeanAndVar mv, double alpha, boolean displayConfidence) {
            var sumInterval = MoreMath.clampZero(mv.middleInterval(alpha));
            return Util.formatTimeInterval(RedfileSummarizer.CompareMode.RANGE, sumInterval, displayConfidence, false);
        }

    }

    void arst(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        var manager = ((IMixinMinecraftClient) client).rubix$getRedfileResultManager();
        var lookingAt = manager.getLookingAt(client);
        var sumOfSelected = manager.getSumOfSelected(client.world);
        List<List<Text>> textBlocks = new ArrayList<>();
        if (lookingAt != null) {
            textBlocks.addLast(getRedfileLines(lookingAt.stats(), "rubix.hud.redfile.value"));
        }
        if (sumOfSelected != null) {
            textBlocks.addLast(getRedfileLines(sumOfSelected, ""));
        }
        if (textBlocks.isEmpty()) return;
        List<Text> lines = new ArrayList<>();
        for (var block : textBlocks) {
            if (!lines.isEmpty()) lines.addLast(Text.empty());
            lines.addAll(block);
        }
        var textRenderer = MinecraftClient.getInstance().textRenderer;

//        int textWidth = textRenderer.getWidth(text);
        int textHeight = lines.size() * 9;
        int x = context.getScaledWindowWidth() / 2 + 10;
        int y = (context.getScaledWindowHeight() - textHeight) / 2 + 1;
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(textRenderer, lines.get(i), x, y + i * 8, -1, false);
        }

    }
}
