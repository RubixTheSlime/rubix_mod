package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit;
import io.github.rubixtheslime.rubix.redfile.RedfileSummarizer;
import io.github.rubixtheslime.rubix.util.SetOperation;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Supplier;

public class ModEnumType {

    public static class RedfileTimeUnitArgument extends EnumArgumentType<ExtendedRedfileTimeUnit> {
        private static final Codec<ExtendedRedfileTimeUnit> CODEC = StringIdentifiable.createCodec(ExtendedRedfileTimeUnit::values);

        private RedfileTimeUnitArgument(Supplier<ExtendedRedfileTimeUnit[]> values) {
            super(CODEC, values);
        }

        public static RedfileTimeUnitArgument trialUnit() {
            return new RedfileTimeUnitArgument(ExtendedRedfileTimeUnit::trialLengths);
        }

        public static RedfileTimeUnitArgument runUnit() {
            return new RedfileTimeUnitArgument(ExtendedRedfileTimeUnit::runLengths);
        }

        public static RedfileTimeUnitArgument unit() {
            return new RedfileTimeUnitArgument(ExtendedRedfileTimeUnit::values);
        }

        public static ExtendedRedfileTimeUnit getUnit(CommandContext<?> context, String id) {
            return context.getArgument(id, ExtendedRedfileTimeUnit.class);
        }
    }

    public static class SetOp extends EnumArgumentType<SetOperation> {
        private static final Codec<SetOperation> CODEC = StringIdentifiable.createCodec(SetOperation::values);

        private SetOp() {
            super(CODEC, SetOperation::values);
        }

        public static SetOp setOp() {
            return new SetOp();
        }

        public static SetOperation getSetOp(CommandContext<?> context, String id) {
            return context.getArgument(id, SetOperation.class);
        }
    }

    public static class CompareModeArgument extends EnumArgumentType<RedfileSummarizer.CompareMode> {
        private static final Codec<RedfileSummarizer.CompareMode> CODEC = StringIdentifiable.createCodec(RedfileSummarizer.CompareMode::values);

        private CompareModeArgument() {
            super(CODEC, RedfileSummarizer.CompareMode::values);
        }

        public static CompareModeArgument compareMode() {
            return new CompareModeArgument();
        }

        public static RedfileSummarizer.CompareMode getMode(CommandContext<?> context, String id) {
            return context.getArgument(id, RedfileSummarizer.CompareMode.class);
        }
    }
}
