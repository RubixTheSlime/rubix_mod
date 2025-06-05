package io.github.rubixtheslime.rubix.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.rubixtheslime.rubix.misc.TransWorldManager;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.stream.Stream;

@Mixin(Direction.class)
public class MixinDirection {
    @WrapMethod(method = "values")
    private static Direction[] values(Operation<Direction[]> original) {
        return TransWorldManager.getDirections();
    }

    @Mixin(Direction.Type.class)
    public static abstract class Type {

        @Shadow public abstract Stream<Direction> stream();

        @WrapMethod(method = "test(Lnet/minecraft/util/math/Direction;)Z")
        boolean test(Direction direction, Operation<Boolean> original) {
            return original.call(TransWorldManager.untransDirection(direction));
        }

        @WrapMethod(method = "iterator")
        Iterator<Direction> iteratorWrap(Operation<Iterator<Direction>> original) {
            return stream().iterator();
        }

        @WrapMethod(method = "stream")
        Stream<Direction> streamWrap(Operation<Stream<Direction>> original) {
            return original.call().map(TransWorldManager::transDirection);
        }
    }

}
