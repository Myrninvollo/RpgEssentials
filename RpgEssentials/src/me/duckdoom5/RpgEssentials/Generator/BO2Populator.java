package me.duckdoom5.RpgEssentials.Generator;

import java.util.ArrayList;
import java.util.Random;

import me.duckdoom5.RpgEssentials.BO2ObjectManager;
import me.duckdoom5.RpgEssentials.RpgEssentials;
import me.duckdoom5.RpgEssentials.util.BO2BlockData;
import me.duckdoom5.RpgEssentials.util.BO2Object;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

public class BO2Populator extends BlockPopulator {

    final int ChanceForTree = 10;
    final int ChanceForObject = 1;
    final boolean BailOutput = false;
    boolean b = false;

    @Override
    public void populate(World world, Random rng, Chunk source) {
        BailError("Starting bo2 object population");
        final ArrayList<Block> blocks = GetSurfaceBlocks(source, world);
        final ArrayList<BO2Object> trees = BO2ObjectManager.getTrees();
        int roll;

        // Generate Trees.
        for (int i = 0; i < blocks.size(); i++) {
            // roll = rng.nextInt(1000);
            // if (roll > ChanceForTree) {
            // // Dont want a tree here
            // continue;
            // }
            for (int t = 0; t < trees.size(); t++) {
                final Block b = blocks.get(i);
                final BO2Object obj = trees.get(t);

                if (!obj.canSpawnInBiome(b.getBiome())) {
                    BailError("wrong biome");
                    continue;
                }
                if (!obj.canSpawnOnBlock(b.getTypeId())) {
                    BailError("wrong type");
                    continue; // Bail, wrong type
                }
                roll = rng.nextInt(1000);
                final int ChanceToSpawn = obj.getRarity();
                if (roll > ChanceToSpawn) {
                    BailError("fate decides no");
                    continue; // Bail, fate decides no
                }

                if (b.getY() < obj.getSpawnElevationMin()) {
                    BailError("Too low");
                    continue; // Bail, Too low
                }
                if (b.getY() >= obj.getSpawnElevationMax()) {
                    BailError("Too High");
                    continue; // Bail, Too High
                }
                if (b.getY() + obj.getMaxY() >= 127) {
                    BailError("Over the top");
                    continue; // Bail, Goes over the top of the world
                }
                if (b.getY() + obj.getMinY() <= 0) {
                    BailError("Out of the bottom");
                    continue; // Bail, Goes over the top of the world
                }

                // Feels good to get past all those continues.
                PlaceObjectInWorld(b, obj, world, rng);

                break; // Kill the loop if we get here, no other tree can spawn
            }
        }
        final ArrayList<BO2Object> objects = BO2ObjectManager.getNotTrees();

        // Generate non trees
        if (objects.size() == 0)
            return; // we are done here
        for (int i = 0; i < blocks.size(); i++) {
            // roll = rng.nextInt(1000);
            // if (roll > ChanceForObject) {
            // // Dont want an object here
            // continue;
            // }
            // give a chance for each of the objects to spawn
            for (int t = 0; t < objects.size(); t++) {
                final Block b = blocks.get(i);
                final BO2Object obj = objects.get(t);
                roll = rng.nextInt(1000);
                final int ChanceToSpawn = obj.getRarity();
                if (roll > ChanceToSpawn) {
                    BailError("fate decides no");
                    continue; // Bail, fate decides no
                }
                if (!obj.canSpawnInBiome(b.getBiome())) {
                    BailError("wrong biome");
                    continue;
                }

                if (!obj.canSpawnOnBlock(b.getTypeId())) {
                    BailError("wrong type");
                    continue; // Bail, wrong type
                }

                if (b.getY() < obj.getSpawnElevationMin()) {
                    BailError("Too low");
                    continue; // Bail, Too low
                }

                if (b.getY() >= obj.getSpawnElevationMax()) {
                    BailError("Too High");
                    continue; // Bail, Too High
                }

                if (b.getY() + obj.getMaxY() >= 127) {
                    BailError("Over the top");
                    continue; // Bail, Goes over the top of the world
                }

                if (b.getY() + obj.getMinY() <= 0) {
                    BailError("Out of the bottom");
                    continue; // Bail, Goes over the top of the world
                }

                // Look at the block that is above the one selected to find
                // light level
                final int checkBlock = world.getBlockAt(b.getX(), b.getY() + 2,
                        b.getZ()).getLightLevel();
                if (!obj.canSpawnSunlight()) {
                    if (!(checkBlock > 8)) {
                        BailError("Too Dark");
                        continue;
                    }
                }
                if (!obj.canSpawnDarkness()) {
                    if (!(checkBlock < 9)) {
                        BailError("Too Light");
                        continue;
                    }
                }

                // // 4 and up is 'lit', below is 'dark'
                // if (b.getLightLevel() < (byte) 4 && !obj.canSpawnDarkness())
                // {
                // BailError("Too Dark");
                // continue; // Bail, Goes over the top of the world
                // }
                //
                // // 4 and up is 'lit', below is 'dark'
                // if (b.getLightLevel() >= (byte) 4 && !obj.canSpawnSunlight())
                // {
                // BailError("Too Light");
                // continue; // Bail, Goes over the top of the world
                // }

                // Feels good to get past all those continues.
                PlaceObjectInWorld(b, obj, world, rng);

                break; // Kill the loop if we get here, no other tree can spawn
            }
        }
    }

    public void BailError(String reason) {
        RpgEssentials.getLog().info(reason);
        if (!BailOutput)
            return;
        // BO2Plugin.ConOut("[Bail] " + reason);
    }

    public void PlaceObjectInWorld(Block center, BO2Object object, World world,
            Random rng) {
        final int X = center.getX();
        final int Y = center.getY();
        final int Z = center.getZ();

        // Random Rotation.
        // 0 - No Rotation
        // 1 - Flip X dir
        // 2 - Flip Z Dir
        // 3 - Flip both

        int xrot = 1;
        int zrot = 1;
        if (object.canRandomRotation()) {
            final int rot = rng.nextInt() % 4;

            if (rot == 1) {
                xrot = -1;
            }
            if (rot == 2) {
                zrot = -1;
            }
            if (rot == 3) {
                xrot = -1;
                zrot = -1;
            }
        }

        final BO2BlockData[] data = object.getData();

        // Do Bailout checks before actually placing blocks
        for (final BO2BlockData element : data) {
            final int nX = (X + element.x) * xrot;
            final int nZ = (Z + element.z) * zrot;
            final Chunk c = center.getChunk();
            if (!IsCoordInChunk(nX, nZ, c)) {
                BailError("Not in chunk");
                return;
            }
            final Material mat = element.type;
            if (mat == Material.BEDROCK) {
                BailError("Replacing bedrock");
                return;
            }
        }
        // BO2Plugin.ConOut("Placing");

        // Place object in world
        for (final BO2BlockData element : data) {
            final int nX = (X + element.x) * xrot;
            final int nY = (Y + element.y);
            final int nZ = (Z + element.z) * zrot;

            final Material mat = element.type;
            final int dat = element.data;

            final Block modify = world.getBlockAt(nX, nY + 1, nZ);
            modify.setType(mat);
            modify.setData((byte) dat);
            if (!b) {
                b = true;
            }
        }
    }

    public boolean IsCoordInChunk(int x, int z, Chunk check) {
        final int cX = check.getX() * 16;
        final int cZ = check.getZ() * 16;
        final int cXmax = cX + 15;
        final int cZmax = cZ + 15;

        if (x >= cX && cXmax >= x) {
            if (z >= cZ && cZmax >= z) {
                return true;
            }
        }

        return false;
    }

    public ArrayList<Block> GetSurfaceBlocks(Chunk source, World world) {
        final ArrayList<Block> blocks = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Block b = source.getBlock(x, 64, z);
                final int y = world.getHighestBlockYAt(b.getX(), b.getZ());
                b = source.getBlock(x, y, z);

                blocks.add(b.getRelative(BlockFace.DOWN)); // Get the block
                                                           // below
                // (since highest is an
                // air block
            }
        }
        return blocks;
    }

    public ArrayList<Block> GetOpenBlocks(Chunk source, World world) {
        final ArrayList<Block> blocks = new ArrayList<>();

        for (int x = (source.getX() * 16); x < (source.getX() * 16) + 16; x++) {
            for (int z = (source.getZ() * 16); z < (source.getZ() * 16) + 16; z++) {
                for (int y = world.getHighestBlockYAt(x, z) - 1; y > 0; y--) {
                    final Block candidate = world.getBlockAt(x, y, z);
                    if (candidate.getType() == Material.AIR)
                        continue; // Bail, it's air
                    final Material mat = candidate.getRelative(BlockFace.UP).getType();
                    // Get all blocks that have Air, Water, or Lava above them.
                    // Objects will only spawn if above block is Air, unless
                    // SpawnLava = true or SpawnWater = true
                    if (mat == Material.AIR || mat == Material.WATER || mat == Material.LAVA) {
                        // She's a keeper!
                        blocks.add(candidate);
                    }

                }
            }
        }
        return blocks;
    }

}
