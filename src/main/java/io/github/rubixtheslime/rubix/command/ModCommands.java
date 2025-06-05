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
            Identifier.of(RubixMod.MOD_ID, "direction"),
            ModEnumType.DirectionArgument.class, ConstantArgumentSerializer.of(ModEnumType.DirectionArgument::direction)
        );
        ArgumentTypeRegistry.registerArgumentType(
            Identifier.of(RubixMod.MOD_ID, "horizontal_symmetry"),
            ModEnumType.TransHorizontalSymmetryArgument.class, ConstantArgumentSerializer.of(ModEnumType.TransHorizontalSymmetryArgument::symmetry)
        );
        ArgumentTypeRegistry.registerArgumentType(
            Identifier.of(RubixMod.MOD_ID, "redfile_compare_mode"),
            ModEnumType.CompareModeArgument.class, ConstantArgumentSerializer.of(ModEnumType.CompareModeArgument::compareMode)
        );
        ArgumentTypeRegistry.registerArgumentType(
            Identifier.of(RubixMod.MOD_ID, "redfile_extended_time_unit"),
            ModEnumType.RedfileTimeUnitArgument.class, ConstantArgumentSerializer.of(ModEnumType.RedfileTimeUnitArgument::unit)
        );
        if (EnabledMods.REDFILE) CommandRegistrationCallback.EVENT.register(RedfileCommand::register);
        if (EnabledMods.TRANS_WORLD) CommandRegistrationCallback.EVENT.register(TransWorldCommand::register);
    }

    public static <T> T tryOr(T d, Supplier<T> f) {
        try {
            return f.get();
        } catch (IllegalArgumentException ignored) {
            return d;
        }
    }
}
