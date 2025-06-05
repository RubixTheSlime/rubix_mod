package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.rubixtheslime.rubix.misc.TransWorldManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.*;

public class TransWorldCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("transworld")
            .then(literal("translate")
                .then(literal("random")
                    .then(argument("max_value", IntegerArgumentType.integer())
                        .executes(context -> 1)
                    )
                )
                .then(argument("x_offset", IntegerArgumentType.integer())
                    .then(argument("y_offset", IntegerArgumentType.integer())
                        .then(argument("z_offset", IntegerArgumentType.integer())
                            .executes(context -> {
                                TransWorldManager.setOffset(
                                    IntegerArgumentType.getInteger(context, "x_offset"),
                                    IntegerArgumentType.getInteger(context, "y_offset"),
                                    IntegerArgumentType.getInteger(context, "z_offset")
                                );
                                return 1;
                            })
                        )
                    )
                )
            )
            .then(literal("rotate")
                .then(argument("symmetry", ModEnumType.TransHorizontalSymmetryArgument.symmetry())
                    .executes(context -> {
                        TransWorldManager.setOrientation(ModEnumType.TransHorizontalSymmetryArgument.getSymmetry(context, "symmetry"));
                        return 1;
                    })
                )
                .then(argument("to_up", ModEnumType.DirectionArgument.direction())
                    .executes(context -> {
                        TransWorldManager.setOrientation(
                            ModEnumType.DirectionArgument.getDirection(context, "to_up")
                        );
                        return 1;
                    })
                    .then(argument("to_south", ModEnumType.DirectionArgument.direction())
                        .executes(context -> {
                            TransWorldManager.setOrientation(
                                ModEnumType.DirectionArgument.getDirection(context, "to_up"),
                                ModEnumType.DirectionArgument.getDirection(context, "to_south")
                            );
                            return 1;
                        })
                        .then(argument("to_east", ModEnumType.DirectionArgument.direction())
                            .executes(context -> {
                                TransWorldManager.setOrientation(
                                    ModEnumType.DirectionArgument.getDirection(context, "to_up"),
                                    ModEnumType.DirectionArgument.getDirection(context, "to_south"),
                                    ModEnumType.DirectionArgument.getDirection(context, "to_east")
                                );
                                return 1;
                            })

                        )
                    )
                )
            )

        );
    }
}
