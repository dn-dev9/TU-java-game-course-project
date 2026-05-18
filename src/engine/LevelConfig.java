package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration parameters for a specific game level
 * using fibonacci growth.
 */
public class LevelConfig {

    private static final List<Integer> ROWS = new ArrayList<>(Arrays.asList(10, 15));
    private static final List<Integer> COLS = new ArrayList<>(Arrays.asList(10, 10));
    private static final List<Integer> MONSTERS = new ArrayList<>(Arrays.asList( 2,  3));
    private static final List<Integer> TREASURES = new ArrayList<>(Arrays.asList( 2,  2));

    private final int level;
    private final int rows;
    private final int cols;
    private final int monsterCount;
    private final int treasureCount;

    /**
     * constructor directly setts the configuration for the given level number
     *
     * @param level game level
     */
    public LevelConfig(int level) {
        this.level = level;
        buildFib(ROWS, level);
        buildFib(COLS, level);
        buildFib(MONSTERS, level);
        buildFib(TREASURES, level);
        int idx = level - 1;
        rows = ROWS.get(idx);
        cols = COLS.get(idx);
        monsterCount = MONSTERS.get(idx);
        treasureCount = TREASURES.get(idx);
    }

    /**
     * Extends the seq list in place
     * until it has at least targetLevel amount of entries.
     * Each new value is the sum of the two prev values.
     *
     * @param seq the sequence to extend (modified in place)
     * @param targetLevel the required length
     */
    private static void buildFib(List<Integer> seq, int targetLevel) {
        while (seq.size() < targetLevel) {
            int n = seq.size();
            seq.add(seq.get(n - 1) + seq.get(n - 2));
        }
    }

    public int getLevel() { return level; }

    public int getRows() { return rows; }

    public int getCols() { return cols; }

    public int getMonsterCount() { return monsterCount; }

    public int getTreasureCount() { return treasureCount; }

    @Override
    public String toString() {
        return "Level " + level + ": " + rows + "x" + cols
                + ", " + monsterCount + " monsters, " + treasureCount + " treasures";
    }
}
