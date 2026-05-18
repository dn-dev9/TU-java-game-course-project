package engine;

import io.FileManager;
import items.Armor;
import items.Item;
import items.Spell;
import items.Weapon;
import model.GameMap;
import model.Monster;
import model.Position;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Builds maps and populates them
 * Maze for levels are generated once ans saved to level{i}.txt files
 * Population - randomply places the treasures T, monsters M, exit E
 */
public class MapGenerator {

    private static final Random RNG = new Random();
    private static final int[][] DIRS = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}}; // directions have a step of 2

    /**
     * Generates all level files from level 1 to Game.Max_LEVEL if they dont already exist
     * @throws IOException
     */
    public static void generateLevelFiles() throws IOException {
        for (int i = 1; i <= Game.MAX_LEVEL; i++) {
            File f = new File("level" + i + ".txt");
            if (!f.exists()) {
                LevelConfig cfg = new LevelConfig(i);
                char[][] grid = buildMaze(oddDim(cfg.getRows()), oddDim(cfg.getCols()));
                FileManager.saveMaze(grid, f.getPath());
            }
        }
    }

    /**
     * Populates the bare map with a:
     *  start position
     *  exit
     *  monsters
     *  trasures
     * @param map
     * @param cfg
     */
    public static void populate(GameMap map, LevelConfig cfg) {
        char[][] grid = map.getGrid();
        int rows = grid.length;
        int cols = grid[0].length;

        map.setStartRow(1);
        map.setStartCol(1);

        int exitRow = rows - 2;
        int exitCol = cols - 2;
        map.setExitRow(exitRow);
        map.setExitCol(exitCol);
        map.setCell(exitRow, exitCol, 'E');

        List<Position> free = freeCells(grid, rows, cols, 1, 1, exitRow, exitCol);

        for (int i = 0; i < cfg.getMonsterCount() && !free.isEmpty(); i++) {
            Position pos = free.remove(RNG.nextInt(free.size()));
            map.addMonster(new Monster(cfg.getLevel(), pos));
        }

        List<Item> items = defaultTreasures(cfg.getTreasureCount());
        for (int i = 0; i < cfg.getTreasureCount() && !free.isEmpty(); i++) {
            Position pos = free.remove(RNG.nextInt(free.size()));
            map.addTreasure(pos.getRow(), pos.getCol(), items.get(i));
        }
    }

    /**
     * Generates a map and is used as a fallback if a level file cannot be read
     */
    public static GameMap generate(LevelConfig cfg) {
        char[][] grid = buildMaze(oddDim(cfg.getRows()), oddDim(cfg.getCols()));
        GameMap map = new GameMap(grid);
        populate(map, cfg);
        return map;
    }

    /**
     * Creates the ready to use maze
     *  rows and cols must be off for the carve algorithm to work
     * @param rows row count
     * @param cols column count
     * @return the finished grid
     */
    private static char[][] buildMaze(int rows, int cols) {
        char[][] grid = new char[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = '#';
        carve(grid, 1, 1, rows, cols);
        return grid;
    }

    /**
     * A recursive DFS backtracking algorithm which marks a cell as visited and then
     * visits each unvisited neighbour  randomly 2 steps away
     * Guarantees that each odd interior cell will be visited at least once
     * so a path from start to end is also guaranteed!
     *
     * @param grid the grid being carved
     * @param r    current row
     * @param c    current column
     * @param rows total row count
     * @param cols total column count
     */
    private static void carve(char[][] grid, int r, int c, int rows, int cols) {
        grid[r][c] = '.';
        List<int[]> dirs = new ArrayList<>(Arrays.asList(DIRS));
        Collections.shuffle(dirs, RNG);
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 && grid[nr][nc] == '#') {
                grid[r + d[0] / 2][c + d[1] / 2] = '.';
                carve(grid, nr, nc, rows, cols);
            }
        }
    }

    /**
     * Collects all floor cells '.' excluding the start and exit positions
     * start and end positions are excluded
     * @param grid
     * @param rows
     * @param cols
     * @param startR start cell row
     * @param startC start cell col
     * @param exitR  exit cell row
     * @param exitC  exit cell col
     * @return shuffleable list of available positions for monster/treasure placement
     */
    private static List<Position> freeCells(char[][] grid, int rows, int cols, int startR, int startC, int exitR, int exitC) {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (grid[r][c] == '.' && !(r == startR && c == startC)
                        && !(r == exitR && c == exitC))
                    list.add(new Position(r, c));
        return list;
    }

    /**
     * Generates a list of count items cycling through weapon => spell -> armor,
     * each with a random bonus between 15 % and 30 %
     *
     * @param count number of items to generate
     * @return list of items ready to place on the map
     */
    private static List<Item> defaultTreasures(int count) {
        String[] wNames = {"Sword", "Dagger", "Axe", "Mace"};
        String[] sNames = {"Fireball", "Lightning", "Frost Nova", "Arcane Missile"};
        String[] aNames = {"Leather Armor", "Chain Mail", "Plate Armor", "Magic Shield"};
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double bonus = 15.0 + RNG.nextInt(16);
            switch (i % 3) {
                case 0: items.add(new Weapon(wNames[i % wNames.length], bonus)); break;
                case 1: items.add(new Spell(sNames[i % sNames.length], bonus)); break;
                case 2: items.add(new Armor(aNames[i % aNames.length], bonus)); break;
            }
        }
        return items;
    }

    /**
     * makes sure dimensions of maze are always odd for the DFS to work properly
     * @param n
     * @return n if odd or n + 1
     */
    private static int oddDim(int n) { return n % 2 == 0 ? n + 1 : n; }
}
