package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.rubixtheslime.rubix.redfile.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RedfileCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("redfile")
            .executes(context -> {
                net.minecraft.text.MutableText message = Text.translatable(RedfileManager.running() ? "rubix.command.redfile.running" : "rubix.command.redfile.not_running");
                context.getSource().sendFeedback(() -> message, false);
                return 1;
            })
            .requires(source -> source.hasPermissionLevel(2))
            .then(new Suppliers().getRun())
            .then(literal("stopall")
                .executes(context -> {
                    boolean success = RedfileManager.tryStop();
                    if (success) return 1;
                    context.getSource().sendFeedback(() -> Text.translatable("rubix.command.redfile.idle"), false);
                    return 0;
                })
            )
            .then(literal("signal")
                .executes(context -> {
                    RedfileEndCondition.SignalCondition.activateAll(context.getSource().getWorld());
                    return 1;
                })
                .then(argument("name", StringArgumentType.word())
                    .executes(context -> {
                        RedfileEndCondition.SignalCondition.activate(
                            context.getSource().getWorld(),
                            StringArgumentType.getString(context, "name")
                        );
                        return 1;
                    })
                )
            )
        );
    }

    @FunctionalInterface
    private interface PartSupply<T> {
        T get(Suppliers s, CommandContext<ServerCommandSource> context);
    }

    private static class Suppliers {
        PartSupply<BlockBox> boxFunction;
        PartSupply<DataCollector.Builder> collectorFunction;
        PartSupply<RedfileEndCondition.Builder> runEndConditionFunction;
        PartSupply<RedfileEndCondition.Builder> trialEndConditionFunction;

        PartSupply<RedfileSummarizer> summarizerFunction;
        PartSupply<Double> alphaFunction;
        PartSupply<Boolean> splitTagsFunction;

        private RedfileSummarizer getSummarizer(CommandContext<ServerCommandSource> context) {
            return summarizerFunction.get(this, context);
        }

        private double getAlpha(CommandContext<ServerCommandSource> context) {
            return alphaFunction.get(this, context);
        }

        private boolean getSplit(CommandContext<ServerCommandSource> context) {
            return splitTagsFunction.get(this, context);
        }

        Suppliers() {
            boxFunction = (s, context) -> null;
            collectorFunction = (s, context) -> DataCollector.summary(s.getSummarizer(context), s.getSplit(context));
            runEndConditionFunction = (s, context) -> RedfileEndCondition.trialCountCondition(30);
            trialEndConditionFunction = (s, context) -> RedfileEndCondition.tickCondition(100);
            summarizerFunction = (s, context) -> RedfileSummarizer.average(s.getAlpha(context));
            alphaFunction = (s, context) -> 0.05;
            splitTagsFunction = (s, context) -> false;
        }

        Suppliers(Suppliers suppliers) {
            boxFunction = suppliers.boxFunction;
            collectorFunction = suppliers.collectorFunction;
            runEndConditionFunction = suppliers.runEndConditionFunction;
            trialEndConditionFunction = suppliers.trialEndConditionFunction;
            summarizerFunction = suppliers.summarizerFunction;
            alphaFunction = suppliers.alphaFunction;
            splitTagsFunction = suppliers.splitTagsFunction;
        }

        private Suppliers applied(Consumer<Suppliers> f) {
            var res = new Suppliers(this);
            f.accept(res);
            return res;
        }

        Suppliers withBox(PartSupply<BlockBox> f) {
            return applied(s -> s.boxFunction = f);
        }

        Suppliers withRunType(PartSupply<DataCollector.Builder> f) {
            return applied(s -> s.collectorFunction = f);
        }

        Suppliers withEndCondition(PartSupply<RedfileEndCondition.Builder> f, boolean isTrial) {
            return applied(s -> {
                if (isTrial) s.trialEndConditionFunction = f;
                else s.runEndConditionFunction = f;
            });
        }

        Suppliers withSummarizer(PartSupply<RedfileSummarizer> f) {
            return applied(s -> s.summarizerFunction = f);
        }

        Suppliers withAlpha(PartSupply<Double> f) {
            return applied(s -> s.alphaFunction = f);
        }

        Suppliers withSplitTags(PartSupply<Boolean> f) {
            return applied(s -> s.splitTagsFunction = f);
        }

        private ArgumentBuilder<ServerCommandSource, ?> getRun() {
            return literal("run")
                .executes(execute())
                .then(addRunType(literal("world")))
                .then(argument("from", BlockPosArgumentType.blockPos())
                    .then(withBox((s, context) -> BlockBox.create(
                            BlockPosArgumentType.getBlockPos(context, "from"),
                            BlockPosArgumentType.getBlockPos(context, "to")
                    )).addRunType(argument("to", BlockPosArgumentType.blockPos())))
                );
        }

        private ArgumentBuilder<ServerCommandSource, ?> addRunType(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return builder
                .executes(execute())
                .then(addSplitTags(literal("average")))
                .then(literal("compare")
                    .then(literal("control")
                        .then(this
                            .withSummarizer((s, context) ->
                                RedfileSummarizer.setControl(StringArgumentType.getString(context, "cmp_name"))
                            )
                            .withSplitTags((s, context) -> true)
                            .addRunEndCondition(argument("cmp_name", StringArgumentType.word()))
                        )
                    )
                    .then(argument("cmp_mode", ModEnumType.CompareModeArgument.compareMode())
                        .then(withSummarizer((s, context) ->
                                RedfileSummarizer.compare(ModEnumType.CompareModeArgument.getMode(context, "cmp_mode"), StringArgumentType.getString(context, "cmp_name"), s.getAlpha(context))
                            ).addSplitTags(argument("cmp_name", StringArgumentType.word()))
                        )
                    )
                )
                .then(literal("heatmap")
                    .requires(ServerCommandSource::isExecutedByPlayer)
                    .executes(withRunType((s, context) -> DataCollector.heatMap(s.getSplit(context))).execute())
                    .then(withRunType((s, context) -> DataCollector.heatMap(s.getSplit(context)))
                        .withSplitTags((s, context) -> BoolArgumentType.getBool(context, "split_tags"))
                        .addRunEndCondition(argument("split_tags", BoolArgumentType.bool()))
                    )
                );
        }

        private static final String[] ALPHA_SUGGESTIONS = {"0.05", "0.3", "0.01", "0.001"};

        private ArgumentBuilder<ServerCommandSource, ?> addSplitTags(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return builder
                .executes(execute())
                .then(withSplitTags((s, context) -> BoolArgumentType.getBool(context, "split_tags"))
                    .addAlpha(argument("split_tags", BoolArgumentType.bool()))
                );
        }

        private ArgumentBuilder<ServerCommandSource, ?> addAlpha(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return builder
                .executes(execute())
                .then(addRunEndCondition(literal("-")))
                .then(withAlpha((s, context) ->
                    (double) FloatArgumentType.getFloat(context, "alpha")
                ).addRunEndCondition(argument("alpha", FloatArgumentType.floatArg())
                    .suggests((source, builder1) -> CommandSource.suggestMatching(ALPHA_SUGGESTIONS, builder1))
                ));
        }

        private ArgumentBuilder<ServerCommandSource, ?> addEndCondition(
            ArgumentBuilder<ServerCommandSource, ?> builder,
            String name,
            String name2,
            boolean isTrial,
            Function<Long, RedfileEndCondition.Builder> defaultConditionBuilder
        ) {
            return builder
                .executes(execute())
                .then(afterEndCondition(literal("-"), isTrial))
                .then(argument(name + "_length", FloatArgumentType.floatArg())
                    .executes(withEndCondition((s, context) ->
                        defaultConditionBuilder.apply((long) FloatArgumentType.getFloat(context, name + "_length")),
                        isTrial
                    ).execute())
                    .then(withEndCondition((s, context) ->
                        ModEnumType.RedfileTimeUnitArgument.getUnit(context, name + "_unit").getEndCondition(FloatArgumentType.getFloat(context, name + "_length")),
                        isTrial
                    )
                        .afterEndCondition(argument(name + "_unit", ModEnumType.RedfileTimeUnitArgument.runUnit()), isTrial))
                )
                .then(literal("signal")
                    .executes(withEndCondition((s, context) ->
                        RedfileEndCondition.signalCondition(context.getSource().getWorld(), "-", 1),
                        isTrial
                    ).execute())
                    .then(argument(name2 + "_name", StringArgumentType.word())
                        .executes(withEndCondition((s, context) ->
                            RedfileEndCondition.signalCondition(context.getSource().getWorld(), StringArgumentType.getString(context, name2 + "_name"), 1),
                            isTrial
                        ).execute())
                        .then(withEndCondition((s, context) ->
                            RedfileEndCondition.signalCondition(
                                context.getSource().getWorld(),
                                StringArgumentType.getString(context, name2 + "_name"),
                                IntegerArgumentType.getInteger(context, name2 + "_grouping")
                            ),
                            isTrial
                        ).afterEndCondition(argument(name2 + "_grouping", IntegerArgumentType.integer(1)), isTrial))
                    )
                );
        }

        private ArgumentBuilder<ServerCommandSource, ?> afterEndCondition(ArgumentBuilder<ServerCommandSource, ?> builder, boolean isTrial) {
            return isTrial ? addFlags(builder) : addTrialEndCondition(builder);
        }

        private ArgumentBuilder<ServerCommandSource, ?> addRunEndCondition(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return addEndCondition(
                builder.then(
                    withEndCondition((s, context) -> RedfileEndCondition.indefiniteCondition(), false).afterEndCondition(literal("nolimit"), false)
                ),
                "run",
                "end",
                false,
                RedfileEndCondition::trialCountCondition
            );
        }

        private ArgumentBuilder<ServerCommandSource, ?> addTrialEndCondition(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return addEndCondition(builder, "trial", "split", true, RedfileEndCondition::tickCondition);
        }

        private ArgumentBuilder<ServerCommandSource, ?> addFlags(ArgumentBuilder<ServerCommandSource, ?> builder) {
            return builder
                .executes(execute())
//                .then(argument("do_load", BoolArgumentType.bool())
//                    .executes(execute())
                    .then(argument("do_sprint", BoolArgumentType.bool())
                        .executes(execute())
//                    )
                );
        }

        private Command<ServerCommandSource> execute() {
            return context -> {
                var box = boxFunction.get(this, context);

                boolean success = RedfileManager.tryStart(
                    box,
                    runEndConditionFunction.get(this, context),
                    trialEndConditionFunction.get(this, context),
                    collectorFunction.get(this, context),
                    ModCommands.tryOr(true, () -> BoolArgumentType.getBool(context, "do_load")) && box != null,
                    ModCommands.tryOr(true, () -> BoolArgumentType.getBool(context, "do_sprint")),
                    context.getSource()
                );

                context.getSource().sendFeedback(() -> Text.translatable(success ? "rubix.command.redfile.started" : "rubix.command.redfile.already_running"), false);
                return success ? 1 : 0;
            };
        }

    }

    private record Getters(
        Function<CommandContext<ServerCommandSource>, BlockBox> boxFunction,
        Function<CommandContext<ServerCommandSource>, RedfileEndCondition.Builder> runEndConditionFunction,
        Function<CommandContext<ServerCommandSource>, RedfileEndCondition.Builder> trialEndConditionFunction
    ) {}

}
