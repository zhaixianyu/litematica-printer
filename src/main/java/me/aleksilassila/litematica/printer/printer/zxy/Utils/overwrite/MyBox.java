package me.aleksilassila.litematica.printer.printer.zxy.Utils.overwrite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class MyBox implements Iterable<BlockPos> {
    public boolean yIncrement = true;
    public boolean sphereMode = false;
    public Iterator<BlockPos> iterator;
    public BlockPos center;
    public int range;

    public double minX;
    public double minY;
    public double minZ;
    public double maxX;
    public double maxY;
    public double maxZ;

    public MyBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        minX = Math.min(x1, x2);
        minY = Math.min(y1, y2);
        minZ = Math.min(z1, z2);
        maxX = Math.max(x1, x2);
        maxY = Math.max(y1, y2);
        maxZ = Math.max(z1, z2);
    }

    public MyBox(fi.dy.masa.litematica.selection.Box box) {
        this(Vec3.atLowerCornerOf(box.getPos1()), Vec3.atLowerCornerOf(box.getPos2()));
    }

    public MyBox(BlockPos pos) {
        this(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX(),
                pos.getY(),
                pos.getZ()
        );
        center = pos;
    }

    public MyBox(BlockPos pos, int range) {
        this(pos);
        this.range = range;
        this.expand(range);
    }

    public MyBox(Vec3 pos1, Vec3 pos2) {
        this((int) pos1.x, (int) pos1.y, (int) pos1.z, (int) pos2.x, (int) pos2.y, (int) pos2.z);
    }

    //因原方法最大值比较时使用的是 < 而不是 <= 因此 最小边界能被覆盖 而最大边界不能
    public boolean contains(Vec3 vec) {
        return this.contains(vec.x(), vec.y(), vec.z());
    }
    public boolean contains(Vec3i vec) {
        return this.contains(vec.getX(), vec.getY(), vec.getZ());
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    public MyBox expand(double x, double y, double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
        return this;
    }

    public MyBox expand(int value) {
        return this.expand(value, value, value);
    }

    public MyBox setSphereMode(boolean sphereMode) {
        this.sphereMode = sphereMode;
        return this;
    }

    public MyBox setYIncrement(boolean yIncrement) {
        this.yIncrement = yIncrement;
        return this;
    }

    public Iterator<BlockPos> initIterator() {
        if (this.iterator == null) this.iterator = iterator();
        return this.iterator;
    }

    public void resetIterator() {
        this.iterator = iterator();
    }

    @Override
    public @NotNull Iterator<BlockPos> iterator() {
        return new Iterator<BlockPos>() {
            public BlockPos currPos;
            public int sphereMinX, sphereMaxX;
            public int sphereMinZ, sphereMaxZ;

            {
                initCurrPos();
            }

            @Override
            public boolean hasNext() {
                return currPos != null;
            }

            //思路，理解为将球体切片，再分成条，根据当前y计算xz的有效范围
            @Override
            public BlockPos next() {
//                if (currPos == null) {
//                    initCurrPos();
//                    return currPos;
//                }
                BlockPos returnPos = currPos;
                int x = currPos.getX();
                int y = currPos.getY();
                int z = currPos.getZ();
                x++;
                if ((sphereMode && x > sphereMaxX) || x > maxX) {
                    z++;
                    x = getXNode(z, y);
                    if ((sphereMode && z > sphereMaxZ) || z > maxZ) {
                        y = yIncrement ? y + 1 : y - 1;
                        z = getZNode(y);
                        x = getXNode(z, y);
                        if (yIncrement ? y > maxY : y < minY) {
                            currPos = null;
                            return returnPos;
                        }
                    }
                }
                currPos = new BlockPos(x, y, z);
                return returnPos;
            }
            public int getZNode(int y){
                if (!sphereMode) return (int) minZ;
                y = y - center.getY();
                int i = (range * range - y * y);
                int node = (int) Math.sqrt(i);
                sphereMinZ = center.getZ() - node;
                sphereMaxZ = center.getZ() + node;
                return sphereMinZ;
            }
            public int getXNode(int z, int y) {
                if (!sphereMode) return (int) minX;
                z = z - center.getZ();
                y = y - center.getY();
                int x = center.getX();
                int sqrt = (int) Math.sqrt(range * range - z * z - y * y);
                sphereMinX = x - sqrt;
                sphereMaxX = x + sqrt;
                return sphereMinX;
            }
            public void initCurrPos() {
                currPos = new BlockPos((int) minX, (int) (yIncrement ? minY : maxY), (int) minZ);
                if (sphereMode) {
                    int z = getZNode((int) minY);
                    int x = getXNode(z, currPos.getY());
                    currPos = new BlockPos(x, currPos.getY(), z);
                }
            }
        };
    }
}
