package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import io.github.rubixtheslime.rubix.misc.TransWorldManager;
import io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit;
import io.github.rubixtheslime.rubix.redfile.RedfileSummarizer;
import io.github.rubixtheslime.rubix.util.SetOperation;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.Direction;

import java.util.function.Supplier;

public class ModEnumType {

    public static class RedfileTimeUnitArgument extends EnumArgumentType<ExtendedRedfileTimeUnit> {
        private static final Codec<ExtendedRedfileTimeUnit> CODEC = StringIdentifiable.createCodec(ExtendedRedfileTimeUnit::values);

        private RedfileTimeUnitArgument(Supplier<ExtendedRedfileTimeUnit[]> values) {
            super(CODEC, values);
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

    public static class TransHorizontalSymmetryArgument extends EnumArgumentType<TransWorldManager.HorizontalSymmetry> {
        private static final Codec<TransWorldManager.HorizontalSymmetry> CODEC = StringIdentifiable.createCodec(TransWorldManager.HorizontalSymmetry::values);

        private TransHorizontalSymmetryArgument() {
            super(CODEC, TransWorldManager.HorizontalSymmetry::values);
        }

        public static TransHorizontalSymmetryArgument symmetry() {
            return new TransHorizontalSymmetryArgument();
        }

        public static TransWorldManager.HorizontalSymmetry getSymmetry(CommandContext<?> context, String id) {
            return context.getArgument(id, TransWorldManager.HorizontalSymmetry.class);
        }
    }

    public static class DirectionArgument extends EnumArgumentType<Direction> {
        private static final Codec<Direction> CODEC = StringIdentifiable.createCodec(Direction::values);

        private DirectionArgument() {
            super(CODEC, Direction::values);
        }

        public static DirectionArgument direction() {
            return new DirectionArgument();
        }

        public static Direction getDirection(CommandContext<?> context, String id) {
            return context.getArgument(id, Direction.class);
        }
    }
}
