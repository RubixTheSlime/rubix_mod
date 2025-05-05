package io.github.rubixtheslime.rubix;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.github.rubixtheslime.rubix.RubixConfig.DebugOptions;
import org.jetbrains.annotations.NotNull;

public class RDebug {
    private static final DebugOptions OPTIONS = RubixMod.CONFIG.debugOptions;

    public static boolean b0() { return getB(0); }
    public static boolean b1() { return getB(1); }
    public static boolean b2() { return getB(2); }
    public static boolean b3() { return getB(3); }
    public static boolean b4() { return getB(4); }
    public static boolean b5() { return getB(5); }
    public static boolean b6() { return getB(6); }
    public static boolean b7() { return getB(7); }
    public static boolean b8() { return getB(8); }
    public static boolean b9() { return getB(9); }

    public static int i0() { return getI(0); }
    public static int i1() { return getI(1); }
    public static int i2() { return getI(2); }
    public static int i3() { return getI(3); }
    public static int i4() { return getI(4); }
    public static int i5() { return getI(5); }
    public static int i6() { return getI(6); }
    public static int i7() { return getI(7); }
    public static int i8() { return getI(8); }
    public static int i9() { return getI(9); }

    public static float f0() { return getF(0); }
    public static float f1() { return getF(1); }
    public static float f2() { return getF(2); }
    public static float f3() { return getF(3); }
    public static float f4() { return getF(4); }
    public static float f5() { return getF(5); }
    public static float f6() { return getF(6); }
    public static float f7() { return getF(7); }
    public static float f8() { return getF(8); }
    public static float f9() { return getF(9); }

    public static String s0() { return getS(0); }
    public static String s1() { return getS(1); }
    public static String s2() { return getS(2); }
    public static String s3() { return getS(3); }
    public static String s4() { return getS(4); }
    public static String s5() { return getS(5); }
    public static String s6() { return getS(6); }
    public static String s7() { return getS(7); }
    public static String s8() { return getS(8); }
    public static String s9() { return getS(9); }

    @SuppressWarnings("unchecked")
    private static final Consumer<Boolean>[] SET_B = new Consumer[] {
        (Consumer<Boolean>) OPTIONS::debugBool0,
        (Consumer<Boolean>) OPTIONS::debugBool1,
        (Consumer<Boolean>) OPTIONS::debugBool2,
        (Consumer<Boolean>) OPTIONS::debugBool3,
        (Consumer<Boolean>) OPTIONS::debugBool4,
        (Consumer<Boolean>) OPTIONS::debugBool5,
        (Consumer<Boolean>) OPTIONS::debugBool6,
        (Consumer<Boolean>) OPTIONS::debugBool7,
        (Consumer<Boolean>) OPTIONS::debugBool8,
        (Consumer<Boolean>) OPTIONS::debugBool9
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<Boolean>[] GET_B = new Supplier[] {
        (Supplier<Boolean>) OPTIONS::debugBool0,
        (Supplier<Boolean>) OPTIONS::debugBool1,
        (Supplier<Boolean>) OPTIONS::debugBool2,
        (Supplier<Boolean>) OPTIONS::debugBool3,
        (Supplier<Boolean>) OPTIONS::debugBool4,
        (Supplier<Boolean>) OPTIONS::debugBool5,
        (Supplier<Boolean>) OPTIONS::debugBool6,
        (Supplier<Boolean>) OPTIONS::debugBool7,
        (Supplier<Boolean>) OPTIONS::debugBool8,
        (Supplier<Boolean>) OPTIONS::debugBool9
    };

    @SuppressWarnings("unchecked")
    private static final Consumer<Integer>[] SET_I = new Consumer[] {
        (Consumer<Integer>) OPTIONS::debugInt0,
        (Consumer<Integer>) OPTIONS::debugInt1,
        (Consumer<Integer>) OPTIONS::debugInt2,
        (Consumer<Integer>) OPTIONS::debugInt3,
        (Consumer<Integer>) OPTIONS::debugInt4,
        (Consumer<Integer>) OPTIONS::debugInt5,
        (Consumer<Integer>) OPTIONS::debugInt6,
        (Consumer<Integer>) OPTIONS::debugInt7,
        (Consumer<Integer>) OPTIONS::debugInt8,
        (Consumer<Integer>) OPTIONS::debugInt9
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<Integer>[] GET_I = new Supplier[] {
        (Supplier<Integer>) OPTIONS::debugInt0,
        (Supplier<Integer>) OPTIONS::debugInt1,
        (Supplier<Integer>) OPTIONS::debugInt2,
        (Supplier<Integer>) OPTIONS::debugInt3,
        (Supplier<Integer>) OPTIONS::debugInt4,
        (Supplier<Integer>) OPTIONS::debugInt5,
        (Supplier<Integer>) OPTIONS::debugInt6,
        (Supplier<Integer>) OPTIONS::debugInt7,
        (Supplier<Integer>) OPTIONS::debugInt8,
        (Supplier<Integer>) OPTIONS::debugInt9
    };

    @SuppressWarnings("unchecked")
    private static final Consumer<Float>[] SET_F = new Consumer[] {
        (Consumer<Float>) OPTIONS::debugFloat0,
        (Consumer<Float>) OPTIONS::debugFloat1,
        (Consumer<Float>) OPTIONS::debugFloat2,
        (Consumer<Float>) OPTIONS::debugFloat3,
        (Consumer<Float>) OPTIONS::debugFloat4,
        (Consumer<Float>) OPTIONS::debugFloat5,
        (Consumer<Float>) OPTIONS::debugFloat6,
        (Consumer<Float>) OPTIONS::debugFloat7,
        (Consumer<Float>) OPTIONS::debugFloat8,
        (Consumer<Float>) OPTIONS::debugFloat9
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<Float>[] GET_F = new Supplier[] {
        (Supplier<Float>) OPTIONS::debugFloat0,
        (Supplier<Float>) OPTIONS::debugFloat1,
        (Supplier<Float>) OPTIONS::debugFloat2,
        (Supplier<Float>) OPTIONS::debugFloat3,
        (Supplier<Float>) OPTIONS::debugFloat4,
        (Supplier<Float>) OPTIONS::debugFloat5,
        (Supplier<Float>) OPTIONS::debugFloat6,
        (Supplier<Float>) OPTIONS::debugFloat7,
        (Supplier<Float>) OPTIONS::debugFloat8,
        (Supplier<Float>) OPTIONS::debugFloat9
    };

    @SuppressWarnings("unchecked")
    private static final Consumer<String>[] SET_S = new Consumer[] {
        (Consumer<String>) OPTIONS::debugString0,
        (Consumer<String>) OPTIONS::debugString1,
        (Consumer<String>) OPTIONS::debugString2,
        (Consumer<String>) OPTIONS::debugString3,
        (Consumer<String>) OPTIONS::debugString4,
        (Consumer<String>) OPTIONS::debugString5,
        (Consumer<String>) OPTIONS::debugString6,
        (Consumer<String>) OPTIONS::debugString7,
        (Consumer<String>) OPTIONS::debugString8,
        (Consumer<String>) OPTIONS::debugString9
    };

    @SuppressWarnings("unchecked")
    private static final Supplier<String>[] GET_S = new Supplier[] {
        (Supplier<String>) OPTIONS::debugString0,
        (Supplier<String>) OPTIONS::debugString1,
        (Supplier<String>) OPTIONS::debugString2,
        (Supplier<String>) OPTIONS::debugString3,
        (Supplier<String>) OPTIONS::debugString4,
        (Supplier<String>) OPTIONS::debugString5,
        (Supplier<String>) OPTIONS::debugString6,
        (Supplier<String>) OPTIONS::debugString7,
        (Supplier<String>) OPTIONS::debugString8,
        (Supplier<String>) OPTIONS::debugString9
    };

    private static <T> void set(int i, @NotNull T value, Consumer<T>[] consumers) {
        Consumer<T> f = EnabledMods.DEBUG && i >= 0 && i <= 9 ? consumers[i] : null;
        if (f != null) f.accept(value);
    }

    private static <T> @NotNull T get(int i, T mdefault, Supplier<T>[] suppliers) {
        Supplier<T> f = EnabledMods.DEBUG && i >= 0 && i <= 9 ? suppliers[i] : null;
        return f == null ? mdefault : f.get();
    }

    public static <T> void set(int i, @NotNull T value) {
        switch (value) {
            case Boolean casted -> set(i, casted, SET_B);
            case Integer casted -> set(i, casted, SET_I);
            case Float casted -> set(i, casted, SET_F);
            case String casted -> set(i, casted, SET_S);
            default -> throw new RuntimeException("invalid value for More Debug: %s".formatted(value));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull T get(int i, Class<T> cls) {
        if (cls == Boolean.class) return (T) get(i, false, GET_B);
        if (cls == Integer.class) return (T) get(i, 0, GET_I);
        if (cls == Float.class) return (T) get(i, 0f, GET_F);
        if (cls == String.class) return (T) get(i, "", GET_S);
        throw new RuntimeException("invalid class for More Debug: %s".formatted(cls));
    }

    public static void setB(int i, boolean v) {
        set(i, v);
    }

    public static boolean getB(int i) {
        return get(i, Boolean.class);
    }

    public static void setI(int i, Integer v) {
        set(i, v);
    }

    public static Integer getI(int i) {
        return get(i, Integer.class);
    }

    public static void setF(int i, Float v) {
        set(i, v);
    }

    public static Float getF(int i) {
        return get(i, Float.class);
    }

    public static void setS(int i, @NotNull String v) {
        set(i, v);
    }

    public static @NotNull String getS(int i) {
        return get(i, String.class);
    }

}
