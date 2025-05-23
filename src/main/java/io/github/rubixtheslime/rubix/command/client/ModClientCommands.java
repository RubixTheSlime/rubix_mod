package io.github.rubixtheslime.rubix.command.client;

import com.mojang.brigadier.context.CommandContext;
import io.github.rubixtheslime.rubix.EnabledMods;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.math.BlockPos;

public class ModClientCommands {

    public static void init() {
        if (EnabledMods.REDFILE) ClientCommandRegistrationCallback.EVENT.register(RedfiledCommand::register);
        if (EnabledMods.GAY_GRASS_VIDEO) ClientCommandRegistrationCallback.EVENT.register(GayGrassCommand::register);
    }

    public static BlockPos getBlockPos(CommandContext<FabricClientCommandSource> context, String name) {
        var x = context.getArgument(name, net.minecraft.command.argument.PosArgument.class);
        return BlockPos.ofFloored(x.getPos(new FakeServerCommandSource(context.getSource())));
    }
}
