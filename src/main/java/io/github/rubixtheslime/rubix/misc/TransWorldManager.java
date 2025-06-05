package io.github.rubixtheslime.rubix.misc;

import io.github.rubixtheslime.rubix.EnabledMods;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TransWorldManager {
    private static final Direction[] BASE_VALUES = new Direction[] {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final Impl IMPL = EnabledMods.TRANS_WORLD ? new Actual() : new Empty();

    public static long getPos(long pos) {
        return IMPL.getPos(pos);
    }

    public static BlockPos getPos(BlockPos pos) {
        return IMPL.getPos(pos);
    }

    public static Direction transDirection(Direction direction) {
        return IMPL.transDirection(direction);
    }

    public static Direction untransDirection(Direction direction) {
        return IMPL.untransDirection(direction);
    }

    public static Direction[] getDirections() {
        return IMPL.getDirections();
    }

    public static void setOrientation(HorizontalSymmetry symmetry) {
        setOrientation(Direction.UP, symmetry.toSouth, symmetry.toEast);
    }

    public static void setOrientation(Direction toUp) {
        if (toUp == Direction.DOWN) {
            setOrientation(Direction.DOWN, Direction.SOUTH, Direction.EAST);
        } else {
            setOrientation(toUp, switch (toUp) {
                case NORTH -> Direction.UP;
                case SOUTH -> Direction.DOWN;
                default -> Direction.SOUTH;
            });
        }
    }

    public static void setOrientation(Direction toUp, Direction toSouth) {
        setOrientation(toUp, toSouth, Direction.fromVector(toUp.getVector().crossProduct(toSouth.getVector()), null));
    }

    public static void setOrientation(Direction toUp, Direction toSouth, Direction toEast) {
        IMPL.setOrientation(toUp, toSouth, toEast);
    }

    public static boolean nextOrientation2d() {
        return IMPL.nextOrientation2d();
    }

    public static boolean nextOrientation3d() {
        return IMPL.nextOrientation3d();
    }

    public static void setOffset(int x, int y, int z) {
        IMPL.setOffset(x, y, z);
    }

    public enum HorizontalSymmetry implements StringIdentifiable {
        IDENTITY("identity", Direction.SOUTH, Direction.EAST),
        ROT_90("cw_90", Direction.WEST, Direction.SOUTH),
        ROT_180("rot_180", Direction.NORTH, Direction.WEST),
        ROT_270("ccw_90", Direction.EAST, Direction.NORTH),
        MIRROR_X("flip_x", Direction.SOUTH, Direction.WEST),
        MIRROR_XZ("flip_xz", Direction.WEST, Direction.NORTH),
        MIRROR_Z("flip_z", Direction.NORTH, Direction.EAST),
        MIRROR_NXZ("flip_nxz", Direction.EAST, Direction.SOUTH),
        ;
        private final String name;
        private final Direction toSouth;
        private final Direction toEast;

        HorizontalSymmetry(String name, Direction toSouth, Direction toEast) {
            this.name = name;
            this.toSouth = toSouth;
            this.toEast = toEast;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    private static abstract class Impl {
        abstract long getPos(long pos);
        abstract BlockPos getPos(BlockPos pos);
        abstract Direction transDirection(Direction direction);
        abstract Direction untransDirection(Direction direction);
        abstract Direction[] getDirections();

        abstract void setOrientation(Direction toUp, Direction toSouth, Direction toEast);
        abstract void setOffset(int x, int y, int z);

        abstract boolean nextOrientation2d();
        abstract boolean nextOrientation3d();
    }

    private static class Empty extends Impl {

        @Override
        long getPos(long pos) {
            return pos;
        }

        @Override
        BlockPos getPos(BlockPos pos) {
            return pos;
        }

        @Override
        Direction transDirection(Direction direction) {
            return direction;
        }

        @Override
        Direction untransDirection(Direction direction) {
            return direction;
        }

        @Override
        Direction[] getDirections() {
            return BASE_VALUES;
        }

        @Override
        void setOrientation(Direction toUp, Direction toSouth, Direction toEast) {
        }

        @Override
        void setOffset(int x, int y, int z) {
        }

        @Override
        boolean nextOrientation2d() {
            return true;
        }

        @Override
        boolean nextOrientation3d() {
            return true;
        }
    }

    private static class Actual extends Impl {
        private int xOffset = 0;
        private int yOffset = 0;
        private int zOffset = 0;
        private Direction[] directions = BASE_VALUES;
        private Direction[] invDirections = BASE_VALUES;

        @Override
        long getPos(long pos) {
            return BlockPos.add(pos, xOffset, yOffset, zOffset);
        }

        @Override
        BlockPos getPos(BlockPos pos) {
            return pos.add(xOffset, yOffset, zOffset);
        }

        @Override
        Direction transDirection(Direction direction) {
            return directions[direction.getIndex()];
        }

        @Override
        Direction untransDirection(Direction direction) {
            return invDirections[direction.getIndex()];
        }

        @Override
        Direction[] getDirections() {
            return directions;
        }

        @Override
        void setOrientation(Direction toUp, Direction toSouth, Direction toEast) {
            directions = new Direction[]{toUp.getOpposite(), toUp, toSouth.getOpposite(), toSouth, toEast.getOpposite(), toEast};
            invDirections = new Direction[directions.length];
            for (int i = 0; i < directions.length; i++) {
                invDirections[directions[i].getIndex()] = Direction.byIndex(i);
            }

        }

        @Override
        void setOffset(int x, int y, int z) {
            xOffset = x;
            yOffset = y;
            zOffset = z;
        }

        @Override
        boolean nextOrientation2d() {
            return true;
        }

        @Override
        boolean nextOrientation3d() {
            return true;
        }
    }

}
