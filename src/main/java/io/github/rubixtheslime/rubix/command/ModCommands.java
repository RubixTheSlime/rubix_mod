package io.github.rubixtheslime.rubix.command;

import io.github.rubixtheslime.rubix.EnabledMods;
import io.github.rubixtheslime.rubix.RubixMod;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ModCommands {
    public static void init() {
        ArgumentTypeRegistry.registerArgumentType(
            Identifier.of(RubixMod.MOD_ID, "redfile_detail"),
            ModEnumType.Detail.class, ConstantArgumentSerializer.of(ModEnumType.Detail::detail)
        );
        ArgumentTypeRegistry.registerArgumentType(
            Identifier.of(RubixMod.MOD_ID, "redfile_extended_time_unit"),
            ModEnumType.ExtendedRedfileTimeUnit.class, ConstantArgumentSerializer.of(ModEnumType.ExtendedRedfileTimeUnit::unit)
        );
        if (EnabledMods.REDFILE) CommandRegistrationCallback.EVENT.register(RedfileCommand::register);
    }

    public static <T> T tryOr(T d, Supplier<T> f) {
        try {
            return f.get();
        } catch (IllegalArgumentException ignored) {
            return d;
        }
    }
}
