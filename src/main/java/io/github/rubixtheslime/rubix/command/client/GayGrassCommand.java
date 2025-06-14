package io.github.rubixtheslime.rubix.command.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.rubixtheslime.rubix.client.RubixModClient;
import io.github.rubixtheslime.rubix.gaygrass.FlagBuffer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.*;

public class GayGrassCommand {
    private static final SuggestionProvider<FabricClientCommandSource> VIDEO_SUPPLIER = ((context, builder) -> CommandSource.suggestIdentifiers(RubixModClient.prideFlagManager.getAnimatedNames(), builder));

    private static ArgumentBuilder<FabricClientCommandSource, ?> addVideoArgument(ArgumentBuilder<FabricClientCommandSource, ?> input, Consumer<FlagBuffer.Animated> f) {
        return input
            .executes(context -> {
                RubixModClient.prideFlagManager.applyToAnimated("*", f);
                return 1;
            })
            .then(argument("id", IdentifierArgumentType.identifier())
                .suggests(VIDEO_SUPPLIER)
                .executes(context -> {
                    RubixModClient.prideFlagManager.applyToAnimated(String.valueOf(context.getArgument("id", Identifier.class)), f);
                    return 1;
                })
            );
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal("gaygrass")
            .then(literal("video")
                .then(addVideoArgument(literal("play"), FlagBuffer.Animated::play))
                .then(addVideoArgument(literal("pause"), FlagBuffer.Animated::pause))
                .then(addVideoArgument(literal("restart"), FlagBuffer.Animated::restart))
            )
        );
    }
}
