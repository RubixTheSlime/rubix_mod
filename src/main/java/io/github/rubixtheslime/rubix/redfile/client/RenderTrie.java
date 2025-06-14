package io.github.rubixtheslime.rubix.redfile.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.*;
import org.apache.commons.math3.util.IntegerSequence;
import org.apache.commons.math3.util.Pair;
import org.joml.*;

import java.lang.Math;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Environment(EnvType.CLIENT)
public class RenderTrie {
    private final Object root;
    private final int depth;
    private final Vector3i origin;

    public RenderTrie(Map<Long, Integer> entries) {
        if (entries.isEmpty()) {
            root = null;
            depth = -1;
            origin = null;
            return;
        }
        Vector3i min = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vector3i max = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        entries.forEach((pos, value) -> {
            Vector3i v = longToVec(pos);
            min.min(v);
            max.max(v);
        });
        origin = min;
        var diff = max.sub(min);
        depth = Math.max(32 - Integer.numberOfLeadingZeros(diff.get(diff.maxComponent())), 1);
        root = depth == 1 ? new int[8] : new Object[8];
        entries.forEach((pos, value) -> {
            Vector3i v = longToVec(pos);
            put(v, value);
        });
    }

    private void put(Vector3i location, int value) {
        location.sub(origin);
        var node = root;
        for (int depthRemaining = depth; depthRemaining > 1; depthRemaining--) {
            int index = extractOctant(location, depthRemaining);
            var branch = assumeBranch(node);
            node = branch[index];
            if (node == null) {
               node = depthRemaining == 2 ? new int[8] : new Object[8];
               branch[index] = node;
            }
        }
        assumeLeaf(node)[toIndex(location)] = value;
    }

    public void visit(Vector3i cameraPos, Frustum frustum, BiConsumer<Vector3i, Integer> f) {
        if (root == null) return;
        List<RecursionEntry> stack = new ArrayList<>();
        addToStack(stack, origin.sub(cameraPos, new Vector3i()), root, depth, false, frustum, cameraPos);
        // use manual + flattening recursion because it's almost always faster
        while (!stack.isEmpty()) {
            var item = stack.removeLast();
            item.handle(stack, frustum, cameraPos, f);
        }
    }

    private record RecursionEntry(int x, int y, int z, Object node, int depthRemaining, boolean fullyInside) {
        void handle(List<RecursionEntry> stack, Frustum frustum, Vector3i camera, BiConsumer<Vector3i, Integer> f) {
            int antiOctant = 7 ^ octantRel(originRelToCam(), depthRemaining);

            if (node instanceof int[] leaf) {
                assert depthRemaining == 1;
                for (int i = 0; i < 8; ++i) {
                    int index = i ^ antiOctant;
                    f.accept(originRelToCam().add(ofOctant(index, 1)), leaf[index]);
                }
            } else if (node instanceof Object[] branch) {
                // iterate reversed to pull items back out correctly
                for (int i = 7; i >= 0; --i) {
                    int index = i ^ antiOctant;
                    var subItem = branch[index];
                    if (subItem == null) continue;
                    addToStack(
                        stack,
                        originRelToCam().add(ofOctant(index, depthRemaining)),
                        subItem,
                        depthRemaining - 1,
                        fullyInside,
                        frustum,
                        camera
                    );
                }
            } else {
                throw new RuntimeException("invalid trie");
            }
        }

        public RecursionEntry(Vector3i originRelToCam, Object node, int depthRemaining, boolean fullyInside) {
            this(originRelToCam.x, originRelToCam.y, originRelToCam.z, node, depthRemaining, fullyInside);
        }

        public Vector3i originRelToCam() {
            return new Vector3i(x, y, z);
        }
    }

    private static void addToStack(List<RecursionEntry> stack, Vector3i origin, Object node, int depth, boolean fullyInside, Frustum frustum, Vector3i camera) {
        if (!fullyInside) {
            int size = (1 << depth) - 1;
            var tmpOrigin = origin.add(camera, new Vector3i());
            int intersect = frustum.intersectAab(new BlockBox(tmpOrigin.x, tmpOrigin.y, tmpOrigin.z, tmpOrigin.x + size , tmpOrigin.y + size, tmpOrigin.z + size));
            if (intersect != FrustumIntersection.INTERSECT && intersect != FrustumIntersection.INSIDE) return;
            fullyInside = intersect == FrustumIntersection.INSIDE;
        }
        stack.addLast(new RecursionEntry(origin, node, depth, fullyInside));
    }

    public Stream<Vector4i> streamRayCast(Vector3d cameraPos, Vector3d facing) {
        if (root == null) return Stream.empty();
        var res = Stream.of(new StreamRayCastIntermediate(new Vector3d(origin).sub(cameraPos), root));
        int facingFromOctant = (facing.x < 0 ? 1 : 0) | (facing.y < 0 ? 2 : 0) | (facing.z < 0 ? 4 : 0);
        for (int remaining = depth; remaining > 1; remaining--) {
            int tmpRemaining = remaining;
            res = res.flatMap(entry -> {
                var branch = assumeBranch(entry.node);
                return entry.stream(facing, facingFromOctant, 7, tmpRemaining)
                    .map(pair -> branch[pair.getSecond()] == null ? null : new StreamRayCastIntermediate(pair.getFirst(), branch[pair.getSecond()]))
                    .filter(Objects::nonNull);
            });
        }
        return res.flatMap(entry -> {
            var leaf = assumeLeaf(entry.node);
            return entry.stream(facing, facingFromOctant, 5, 1)
                .map(pair -> {
                    var actual = new Vector3i(pair.getFirst().add(cameraPos, new Vector3d()), RoundingMode.HALF_EVEN);
                    return new Vector4i(actual.x, actual.y, actual.z, leaf[pair.getSecond()]);
                });
        });
    }

    private record StreamRayCastIntermediate(Vector3d originRelToCam, Object node) {
        Stream<Pair<Vector3d, Integer>> stream(Vector3d facing, int facingFromOctant, int octantMask, int depth) {
            var farCornerDelta = new Vector3d(ofOctant((facingFromOctant ^ 7) & octantMask, depth));
            var boxCornerDelta = new Vector3d(ofOctant(octantMask, depth));
            return StreamSupport
                .stream(new IntegerSequence.Range(0, 7, 1).spliterator(), false)
                .map(x -> x ^ facingFromOctant)
                .map(octant -> {
                    var newOrigin = new Vector3d(ofOctant(octant, depth)).add(originRelToCam);
                    // only forwards
                    if (newOrigin.add(farCornerDelta, new Vector3d()).dot(facing) < 0) return null;
                    // prune non-intersecting
                    var boxCorner = newOrigin.add(boxCornerDelta, new Vector3d());
                    if (!lineIntersectsBox(facing, new Box(newOrigin.x, newOrigin.y, newOrigin.z, boxCorner.x, boxCorner.y, boxCorner.z))) return null;
                    return Pair.create(newOrigin, octant);
                })
                .filter(Objects::nonNull);
        }
    }

    private static Vector3i longToVec(long v) {
        var block = BlockPos.fromLong(v);
        return new Vector3i(block.getX(), block.getY(), block.getZ());
    }

    private static int[] assumeLeaf(Object o) {
        if (o instanceof int[] a) return a;
        throw new RuntimeException("invalid trie");
    }

    private static Object[] assumeBranch(Object o) {
        if (o instanceof Object[] a) return a;
        throw new RuntimeException("invalid trie");
    }

    private static int toIndex(Vector3i v) {
        return v.x | (v.y << 1) | (v.z << 2);
    }

    private int extractOctant(Vector3i location, int depth) {
        int res = (location.x >> (depth - 1))
            | ((location.y >> (depth - 1)) << 1)
            | ((location.z >> (depth - 1)) << 2);
        int mask = ~(-1 << depth - 1);
        location.x &= mask;
        location.y &= mask;
        location.z &= mask;
        return res;
    }

    private static int octantRel(Vector3i originRelToCam, int depth) {
        int cutoff = -1 << depth - 1;
        return (originRelToCam.x <= cutoff ? 1 : 0) | (originRelToCam.y <= cutoff ? 2 : 0) | (originRelToCam.z <= cutoff ? 4 : 0);
    }

    private static boolean lineIntersectsBox(Vector3d direction, Box box) {
        return lineIntersectsRect(direction.x, direction.y, box.minX, box.minY, box.maxX, box.maxY)
            && lineIntersectsRect(direction.z, direction.y, box.minZ, box.minY, box.maxZ, box.maxY)
            && lineIntersectsRect(direction.x, direction.z, box.minX, box.minZ, box.maxX, box.maxZ);
    }

    private static boolean lineIntersectsRect(double dirX, double dirY, double minX, double minY, double maxX, double maxY) {
        if (dirX == 0 && dirY == 0) return minX <= 0 && minY <= 0 && maxX >= 0 && maxY >= 0;
        // parameters are in the right order, doing a rotation
        Vector2d normal = new Vector2d(dirY, -dirX);
        int mask = lineIntersectsRectSub(normal, minX, minY) | lineIntersectsRectSub(normal, maxX, maxY);
        if (mask == 3) return true;
        mask |= lineIntersectsRectSub(normal, maxX, minY);
        return mask == 3 || (mask | lineIntersectsRectSub(normal, minX, maxY)) == 3;
    }

    private static int lineIntersectsRectSub(Vector2d normal, double x, double y) {
        double a = normal.dot(new Vector2d(x, y));
        return a > 0 ? 2 : 1;
    }

    private static Vector3i ofOctant(int index, int depth) {
        return new Vector3i((index & 1) << (depth - 1), ((index >> 1) & 1) << (depth - 1), ((index >> 2) & 1) << (depth - 1));
    }

}
