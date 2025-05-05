package io.github.rubixtheslime.rubix.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import io.github.rubixtheslime.rubix.redfile.RedfileInstance;
import io.github.rubixtheslime.rubix.util.SetOperation;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.StringIdentifiable;

import java.util.function.Supplier;

public class ModEnumType {
    public static class Detail extends EnumArgumentType<RedfileInstance.DetailEnum> {
        private static final Codec<RedfileInstance.DetailEnum> CODEC = StringIdentifiable.createCodec(RedfileInstance.DetailEnum::values);

        private Detail() {
            super(CODEC, RedfileInstance.DetailEnum::values);
        }

        public static Detail detail() {
            return new Detail();
        }

        public static RedfileInstance.DetailEnum getDetail(CommandContext<?> context, String id) {
            return context.getArgument(id, RedfileInstance.DetailEnum.class);
        }
    }

    public static class ExtendedRedfileTimeUnit extends EnumArgumentType<io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit> {
        private static final Codec<io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit> CODEC = StringIdentifiable.createCodec(io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit::values);

        private ExtendedRedfileTimeUnit() {
            super(CODEC, io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit::values);
        }

        public static ExtendedRedfileTimeUnit unit() {
            return new ExtendedRedfileTimeUnit();
        }

        public static io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit getUnit(CommandContext<?> context, String id) {
            return context.getArgument(id, io.github.rubixtheslime.rubix.redfile.ExtendedRedfileTimeUnit.class);
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
}
