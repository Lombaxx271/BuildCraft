package buildcraft.lib.bpt;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import buildcraft.api.bpt.*;

public class Blueprint extends BlueprintBase {
    /** Stores all of the blocks, using {@link BlueprintBase#min} as the origin. */
    private SchematicBlock[][][] contentBlocks;
    private List<SchematicEntity> contentEntities;

    public Blueprint(NBTTagCompound nbt) {
        super(nbt);
        deserializeNBT(nbt);
    }

    public Blueprint(BlockPos anchor, BlockPos min, BlockPos max, EnumFacing direction) {
        super(anchor, min, max, direction);
        BlockPos size = max.subtract(min).add(1, 1, 1);
        contentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        contentEntities = new ArrayList<>();
    }

    @Override
    protected void rotateContentsBy(Rotation rotation) {
        BlockPos oldMax = max.subtract(min);
        BlockPos newMax = rotate(oldMax, rotation);
        BlockPos size = newMax.add(1, 1, 1);
        SchematicBlock[][][] newContentBlocks = new SchematicBlock[size.getX()][size.getY()][size.getZ()];
        BlockPos arrayOffset = newMax.subtract(oldMax);

        for (int x = 0; x < contentBlocks.length; x++) {
            SchematicBlock[][] inXLayer = contentBlocks[x];
            for (int y = 0; y < inXLayer.length; y++) {
                SchematicBlock[] inYLayer = inXLayer[y];
                for (int z = 0; z < inYLayer.length; z++) {
                    SchematicBlock schematic = inYLayer[z];
                    schematic.rotate(rotation);
                    BlockPos original = new BlockPos(x, y, z);
                    BlockPos rotated = rotate(original, rotation);
                    rotated = rotated.add(arrayOffset);
                    newContentBlocks[rotated.getX()][rotated.getY()][rotated.getZ()] = schematic;
                }
            }
        }

        for (SchematicEntity schematic : contentEntities) {
            schematic.rotate(rotation);
        }
    }

    @Override
    protected void mirrorContents(Mirror mirror) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("Implement this!");

    }

    @Override
    protected void translateContentsBy(Vec3i by) {
        for (SchematicBlock[][] ar2 : contentBlocks) {
            for (SchematicBlock[] ar1 : ar2) {
                for (SchematicBlock schematic : ar1) {
                    if (schematic == null) continue;
                    schematic.translate(by);
                }
            }
        }

        for (SchematicEntity schematic : contentEntities) {
            schematic.translate(by);
        }
    }

    @Override
    public Map<Schematic, Iterable<IBptTask>> createTasks(IBuilder builder) {
        Map<Schematic, Iterable<IBptTask>> tasks = new IdentityHashMap<>();
        for (SchematicBlock[][] ar2 : contentBlocks) {
            for (SchematicBlock[] ar1 : ar2) {
                for (SchematicBlock schematic : ar1) {
                    if (schematic == null) continue;
                    tasks.put(schematic, schematic.createTasks(builder));
                }
            }
        }

        for (SchematicEntity schematic : contentEntities) {
            tasks.put(schematic, schematic.createTasks(builder));
        }

        return tasks;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        NBTTagList list = new NBTTagList();
        // We have just moved down to
        for (int x = 0; x <= max.getX(); x++) {
            for (int y = 0; y <= max.getY(); y++) {
                for (int z = 0; z <= max.getZ(); z++) {
                    SchematicBlock block = contentBlocks[x][y][z];
                    list.appendTag(BlueprintAPI.serializeSchematic(block));
                }
            }
        }
        nbt.setTag("blocks", list);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}