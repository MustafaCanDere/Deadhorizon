package com.deadhorizon.block;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.EnumMap;
import java.util.Map;

/** Builds per-direction shapes from boxes written for a north-facing block (front at z = 0), in pixels. */
public final class Shapes {
    private Shapes() {
    }

    public static Map<Direction, VoxelShape> horizontal(double[]... boxes) {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        for (Direction dir : Direction.Type.HORIZONTAL) {
            VoxelShape shape = VoxelShapes.empty();
            for (double[] b : boxes) {
                shape = VoxelShapes.union(shape, rotate(b, dir));
            }
            map.put(dir, shape.simplify());
        }
        return map;
    }

    public static double[] box(double x1, double y1, double z1, double x2, double y2, double z2) {
        return new double[]{x1, y1, z1, x2, y2, z2};
    }

    private static VoxelShape rotate(double[] b, Direction dir) {
        double x1 = b[0] / 16, y1 = b[1] / 16, z1 = b[2] / 16, x2 = b[3] / 16, y2 = b[4] / 16, z2 = b[5] / 16;
        return switch (dir) {
            case EAST -> VoxelShapes.cuboid(1 - z2, y1, x1, 1 - z1, y2, x2);
            case SOUTH -> VoxelShapes.cuboid(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
            case WEST -> VoxelShapes.cuboid(z1, y1, 1 - x2, z2, y2, 1 - x1);
            default -> VoxelShapes.cuboid(x1, y1, z1, x2, y2, z2);
        };
    }
}
