package model;

import items.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the game map grid for a single game level
 * Cell symbols:
 *  '#' – wall
 *  '.' – walkable floor
 *  'T' – treasure item
 *  'M' – monster
 *  'E' – exit
 *  '@' – hero position (used only when printing)
 * The hero starts at the top-left free cell and must reach the exit at bottom-right
 */
public class GameMap {

    private final char[][] grid;
    private final int rows;
    private final int cols;
    private int startRow;
    private int startCol;
    private int exitRow;
    private int exitCol;

    /**
     *  All living monsters on this map.
     */
    private final List<Monster> monsters;
    /**
     * "row,col" is the key String pointing to the Item sitting on that cell
     * Item exists until its being picked up or discarded
     */
    private final Map<String, Item> treasures;


    /**
     * Builds the GameMap from a passed grid, monsters and treasures must be added separately by
     * addMonster(Monster) and addTreasure(int, int, Item)
     *
     * @param grid rectangular char array
     */
    public GameMap(char[][] grid) {
        this.rows = grid.length;
        this.cols = grid[0].length;
        this.grid = grid;
        this.monsters = new ArrayList<>();
        this.treasures = new HashMap<>();
    }

    public char getCell(int row, int col) { return grid[row][col]; }

    public void setCell(int row, int col, char c) { grid[row][col] = c; }

    public boolean isWall(int row, int col) { return grid[row][col] == '#'; }

    /**
     * Checks if coordinates are inside map's bounds
     * @param row row coordinate
     * @param col col coordinate
     * @return boolean
     */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Adds monster to the List and on the map
     * @param monster monster to add on the map
     */
    public void addMonster(Monster monster) {
        monsters.add(monster);
        grid[monster.getPosition().getRow()][monster.getPosition().getCol()] = 'M';
    }

    /**
     * Returns the monster if present on field coordinates
     * @param row row to look for monster
     * @param col col to look for monster
     * @return monster at specified coordinates
     */
    public Monster monsterAt(int row, int col) {
        for (Monster m : monsters) {
            if (m.isAlive() && m.getPosition().getRow() == row && m.getPosition().getCol() == col) {
                return m;
            }
        }
        return null;
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
        grid[monster.getPosition().getRow()][monster.getPosition().getCol()] = '.';
    }

    public void addTreasure(int row, int col, Item item) {
        treasures.put(row + "," + col, item);
        grid[row][col] = 'T';
    }

    public Item treasureAt(int row, int col) {
        return treasures.get(row + "," + col);
    }

    public void removeTreasure(int row, int col) {
        treasures.remove(row + "," + col);
        if (grid[row][col] == 'T') grid[row][col] = '.';
    }

    /**
     * Prints the map on screen and hero is shown as '@'
     * @param heroRow hero current row
     * @param heroCol hero current col
     * @return the map string
     */
    public String render(int heroRow, int heroCol) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == heroRow && c == heroCol) sb.append('@');
                else sb.append(grid[r][c]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public int getRows() { return rows; }

    public int getCols() { return cols; }

    /**
     * @return list of all monsters (including dead ones until removed)
     * */
    public List<Monster> getMonsters() { return monsters; }

    /**
     * @return treasure map keyed by "row,col"
     * */
    public Map<String, Item> getTreasures() { return treasures; }

    public char[][] getGrid() { return grid; }

    public int getStartRow() { return startRow; }

    public void setStartRow(int startRow) { this.startRow = startRow; }

    public int getStartCol() { return startCol; }

    public void setStartCol(int startCol) { this.startCol = startCol; }

    public int getExitRow() { return exitRow; }

    public void setExitRow(int exitRow) { this.exitRow = exitRow; }

    public int getExitCol() { return exitCol; }

    public void setExitCol(int exitCol) { this.exitCol = exitCol; }
}
