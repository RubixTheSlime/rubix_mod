package io.github.rubixtheslime.rubix.command.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.rubixtheslime.rubix.command.ModCommands;
import io.github.rubixtheslime.rubix.command.ModEnumType;
import io.github.rubixtheslime.rubix.imixin.client.IMixinMinecraftClient;
import io.github.rubixtheslime.rubix.redfile.client.RedfileResultManager;
import io.github.rubixtheslime.rubix.util.SetOperation;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.World;

import java.util.function.BiPredicate;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;


public class RedfilecCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("redfilec")
            .then(literal("debug")
                .then(literal("empty")
                    .executes(ofManager(RedfileResultManager::addEmpty))
                )
                .then(literal("map")
                    .then(argument("pos", BlockPosArgumentType.blockPos())
                        .then(argument("width", IntegerArgumentType.integer())
                            .executes(context -> {
                                var pos = ModClientCommands.getBlockPos(context, "pos");
                                int width = IntegerArgumentType.getInteger(context, "width");
                                if (width == 0) return 0;
                                getManager(context).addDebug(pos, width, context.getSource().getWorld());
                                return 1;
                            })
                        )
                    )
                )
            )
            .then(literal("layer")
                .then(literal("off").executes(ofManager(((manager, world) -> manager.setLayer(world, null)))))
                .then(literal("up").executes(ofManager((manager, world) -> manager.moveLayer(world, true))))
                .then(literal("down").executes(ofManager((manager, world) -> manager.moveLayer(world, false))))
                .then(literal("here")
                    .executes(context -> {
                        var manager = getManager(context);
                        return manager.setLayer(context.getSource().getWorld(), (int) context.getSource().getPosition().y) ? 1 : 0;
                    })
                )
            )
            .then(literal("runs")
                .then(literal("show")
                    .then(literal("all").executes(ofManager(((manager, world) -> manager.setAllActive(world, true)))))
                    .then(argument("index", IntegerArgumentType.integer())
                        .executes(context -> {
                            return getManager(context).setActive(context.getSource().getWorld(), IntegerArgumentType.getInteger(context, "index"), true) ? 1 : 0;
                        })
                    )
                )
                .then(literal("hide")
                    .then(literal("all").executes(ofManager(((manager, world) -> manager.setAllActive(world, false)))))
                    .then(argument("index", IntegerArgumentType.integer())
                        .executes(context -> {
                            return getManager(context).setActive(context.getSource().getWorld(), IntegerArgumentType.getInteger(context, "index"), false) ? 1 : 0;
                        })
                    )
                )
                .then(literal("drop")
                    .then(literal("all").executes(ofManager(RedfileResultManager::clearResults)))
                    .then(argument("index", IntegerArgumentType.integer())
                        .executes(context -> {
                            return getManager(context).drop(context.getSource().getWorld(), IntegerArgumentType.getInteger(context, "index")) ? 1 : 0;
                        })
                    )
                )
            )
            .then(literal("select")
                .then(argument("operation", ModEnumType.SetOp.setOp())
                    .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                            .then(argument("blockstate", BlockStateArgumentType.blockState(registryAccess))
                                .executes(RedfilecCommand::applySetOp)
                            )
                            .executes(RedfilecCommand::applySetOp)
                        )
                    )
                    .then(argument("blockstate", BlockStateArgumentType.blockState(registryAccess))
                        .executes(RedfilecCommand::applySetOp)
                    )
                    .executes(RedfilecCommand::applySetOp)
                )
                .then(literal("run")
                    .then(argument("index", IntegerArgumentType.integer())
                        .executes(context -> {
                            return getManager(context).selectAll(context.getSource().getWorld(), IntegerArgumentType.getInteger(context, "index")) ? 1 : 0;
                        })
                    )
                )
            )
        );
    }

    private static int applySetOp(CommandContext<FabricClientCommandSource> context) {
        var world = context.getSource().getWorld();
        if (!getManager(context).isSelecting(world)) {
            context.getSource().sendFeedback(Text.translatable("rubix.command.redfile.not_selecting"));
            return 0;
        }
        SetOperation setOp = ModEnumType.SetOp.getSetOp(context, "operation");
        BlockBox box = ModCommands.tryOr(null, () -> BlockBox.create(
            ModClientCommands.getBlockPos(context, "from"),
            ModClientCommands.getBlockPos(context, "to")
        ));
        BlockStateArgument blockStateArgument = ModCommands.tryOr(null, () -> context.getArgument("blockstate", BlockStateArgument.class));
        return getManager(context).applySetOperationToSelection(world, setOp, box, blockStateArgument) ? 1 : 0;
    }

    private static Command<FabricClientCommandSource> ofManager(BiPredicate<RedfileResultManager, World> f) {
        return context -> f.test(getManager(context), context.getSource().getWorld()) ? 1 : 0;
    }

    private static RedfileResultManager getManager(CommandContext<FabricClientCommandSource> context) {
        return ((IMixinMinecraftClient) context.getSource().getClient()).rubix$getRedfileResultManager();
    }
}
