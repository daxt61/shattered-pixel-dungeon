package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;

/**
 * Isolated logic for dungeon generation.
 * This class provides a clear interface to generate a level, place mobs, and place items.
 */
public class DungeonGenerator {

    /**
     * Generates a complete dungeon level for a given seed and depth.
     *
     * @param seedText The seed as a string.
     * @param depth The dungeon depth (1-26).
     * @return The generated Level object.
     */
    public static Level createLevel(String seedText, int depth) {
        // Convert text seed to long
        long seed = DungeonSeed.convertFromText(seedText);
        return createLevel(seed, depth);
    }

    /**
     * Generates a complete dungeon level for a given seed and depth.
     *
     * @param seed The seed as a long.
     * @param depth The dungeon depth (1-26).
     * @return The generated Level object.
     */
    public static Level createLevel(long seed, int depth) {
        // Initialize dungeon state
        Dungeon.seed = seed;
        Dungeon.depth = depth;

        // Initialize necessary game state for generation
        // These calls set up static references and random number generators
        Dungeon.init();

        // Dungeon.init() sets the depth to 1 by default, we reset it to the desired depth
        Dungeon.depth = depth;

        // Dungeon.newLevel() instantiates the correct Level subclass (Sewer, Prison, etc.)
        // and then calls level.create() which performs the full generation process.
        // The generation process includes:
        // 1. Layout building (rooms and corridors)
        // 2. Painting tiles
        // 3. Placing mobs
        // 4. Placing items and chests
        Level level = Dungeon.newLevel();

        return level;
    }
}
