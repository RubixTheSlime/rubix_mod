package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit;
import io.github.rubixtheslime.rubix.redfile.RedfileManager;
import io.github.rubixtheslime.rubix.redfile.RedfileInstance;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;

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
                .then(getRun())
                .then(literal("stop")
                    .executes(context -> {
                        boolean success = RedfileManager.tryStop();
                        if (success) return 1;
                        context.getSource().sendFeedback(() -> Text.translatable("rubix.command.redfile.idle"), false);
                        return 0;
                    })
                )
        );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> getRun() {
        return literal("run")
            .then(getRun2(literal("everywhere"), context -> null))
            .then(argument("from", BlockPosArgumentType.blockPos())
                .then(getRun2(argument("to", BlockPosArgumentType.blockPos()), context ->
                    BlockBox.create(
                        BlockPosArgumentType.getBlockPos(context, "from"),
                        BlockPosArgumentType.getBlockPos(context, "to")
                    )
                ))
            );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> getRun2(
        ArgumentBuilder<ServerCommandSource, ?> builder,
        Function<CommandContext<ServerCommandSource>, BlockBox> boxFunction
    ) {
        return builder
            .executes(getRun4(boxFunction, null))
            .then(argument("run_type", ModEnumType.Detail.detail())
                .executes(getRun4(boxFunction, null))
                .then(getRun3(literal("nolimit").executes(getRun4(boxFunction, null)),
                    boxFunction,
                    context -> ExtendedRedfileTimeUnit.INDEFINITE
                ))
                .then(argument("length", FloatArgumentType.floatArg())
                    .executes(getRun4(boxFunction, null))
                    .then(getRun3(argument("unit", ModEnumType.ExtendedRedfileTimeUnit.unit()),
                        boxFunction,
                        context -> ModEnumType.ExtendedRedfileTimeUnit.getUnit(context, "unit")
                    ))
                )
            );
    }

    private static ArgumentBuilder<ServerCommandSource, ?> getRun3(
        ArgumentBuilder<ServerCommandSource, ?> builder,
        Function<CommandContext<ServerCommandSource>, BlockBox> boxFunction,
        Function<CommandContext<ServerCommandSource>, ExtendedRedfileTimeUnit> unitFunction
    ) {
        return builder
            .executes(getRun4(boxFunction, unitFunction))
            .then(argument("do_load", BoolArgumentType.bool())
            .executes(getRun4(boxFunction, unitFunction))
            .then(argument("do_sprint", BoolArgumentType.bool())
                .executes(getRun4(boxFunction, unitFunction))
            ));
    }

    private static Command<ServerCommandSource> getRun4(
        Function<CommandContext<ServerCommandSource>, BlockBox> boxFunction,
        Function<CommandContext<ServerCommandSource>, ExtendedRedfileTimeUnit> unitFunction
    ) {
        return context -> {
            var unitEnum = unitFunction == null ? ExtendedRedfileTimeUnit.TICKS : unitFunction.apply(context);
            var box = boxFunction.apply(context);

            boolean success = RedfileManager.tryStart(
                box,
                ModCommands.tryOr(1000L, () -> (long) (FloatArgumentType.getFloat(context, "length") * unitEnum.getScale())),
                unitEnum.getUnit(),
                ModCommands.tryOr(RedfileInstance.DetailEnum.SUMMARY, () -> ModEnumType.Detail.getDetail(context, "run_type")),
                ModCommands.tryOr(true, () -> BoolArgumentType.getBool(context, "do_load")) && box != null,
                ModCommands.tryOr(true, () -> BoolArgumentType.getBool(context, "do_sprint")),
                context.getSource()
            );

            context.getSource().sendFeedback(() -> Text.translatable(success ? "rubix.command.redfile.started" : "rubix.command.redfile.already_running"), false);
            return success ? 1 : 0;
        };
    }


}
