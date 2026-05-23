package engine;

import io.FileManager;
import items.Armor;
import items.Item;
import items.Spell;
import items.Weapon;
import model.Cell;
import model.GameMap;
import model.Monster;
import model.Position;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Builds maps and populates them with monsters, treasures, and an exit.
 * Level maze files are generated once and saved to level{n}.txt.
 * Population randomply places monsters M, treasures T, and the exit E.
 */
public class MapGenerator {

    private static final Random RNG = new Random();
    private static final List<int[]> DIRS = List.of(
            new int[]{-2, 0},
            new int[]{ 2, 0},
            new int[]{ 0, -2},
            new int[]{ 0, 2}
    );

    /**
     * Generates all level files from level 1 to Game.MAX_LEVEL if they don't already exist.
     */
    public static void generateLevelFiles() throws IOException {
        for (int i = 1; i <= Game.MAX_LEVEL; i++) {
            File f = new File("level" + i + ".txt");
            if (!f.exists()) {
                LevelConfig cfg = new LevelConfig(i);
                List<List<Cell>> cells = buildMaze(oddDim(cfg.getRows()), oddDim(cfg.getCols()));
                FileManager.saveMaze(cells, f.getPath());
            }
        }
    }

    /**
     * Populates a bare map with a start position, exit, monsters, and treasures.
     *
     * @param map the empty map to populate
     * @param cfg level configuration (counts, level number)
     */
    public static void populate(GameMap map, LevelConfig cfg) {
        int rows = map.getRows();
        int cols = map.getCols();

        map.setStartRow(1);
        map.setStartCol(1);

        int exitRow = rows - 2;
        int exitCol = cols - 2;
        map.markExit(exitRow, exitCol);

        List<Position> free = freeCells(map.getCells(), rows, cols, 1, 1, exitRow, exitCol);

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
     * Generates a map and is used as a fallback if a level file cannot be read.
     *
     * @param cfg LevelConfig object containing map's features
     * @return the map cell grid
     */
    public static GameMap generate(LevelConfig cfg) {
        List<List<Cell>> cells = buildMaze(oddDim(cfg.getRows()), oddDim(cfg.getCols()));
        GameMap map = new GameMap(cells);
        populate(map, cfg);
        return map;
    }

    /**
     * Builds a maze as a List of Cell rows.
     * Rows and cols must be odd for the DFS carver algorithm to work correctly.
     *
     * @param rows row count
     * @param cols column count
     * @return the finished grid
     */
    private static List<List<Cell>> buildMaze(int rows, int cols) {
        List<List<Cell>> cells = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            List<Cell> row = new ArrayList<>();
            for (int c = 0; c < cols; c++)
                row.add(new Cell(Cell.Type.WALL));
            cells.add(row);
        }
        carve(cells, 1, 1, rows, cols);
        return cells;
    }

    /**
     * Recursive DFS backtracking maze carver.
     * Marks the current cell as floor, then visits unvisited neighbours
     * two steps away in a random order — guaranteeing full connectivity.
     *
     * @param cells the grid being carved
     * @param r current row index
     * @param c current column index
     * @param rows total row count
     * @param cols total column count
     */
    private static void carve(List<List<Cell>> cells, int r, int c, int rows, int cols) {
        cells.get(r).set(c, new Cell(Cell.Type.FLOOR));
        List<int[]> dirs = new ArrayList<>(DIRS);
        Collections.shuffle(dirs, RNG);
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1
                    && cells.get(nr).get(nc).isWall()) {
                cells.get(r + d[0] / 2).set(c + d[1] / 2, new Cell(Cell.Type.FLOOR));
                carve(cells, nr, nc, rows, cols);
            }
        }
    }

    /**
     * Lists all floor cells excluding the start and exit positions.
     *
     * @return list of available positions for monster/treasure placement
     */
    private static List<Position> freeCells(List<List<Cell>> cells, int rows, int cols,
                                            int startR, int startC, int exitR, int exitC) {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                Cell cell = cells.get(r).get(c);
                if (!cell.isWall()
                        && !(r == startR && c == startC)
                        && !(r == exitR  && c == exitC))
                    list.add(new Position(r, c));
            }
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

    /** Ensures a dimension is odd, as required by the DFS carver. */
    private static int oddDim(int n) { return n % 2 == 0 ? n + 1 : n; }
}
