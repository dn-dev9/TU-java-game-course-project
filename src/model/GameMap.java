package model;

import items.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the game map grid for a single level.
 * The grid is a List of rows, each row is a List of Cell objects.
 * Each Cell owns its own monster and treasure reference
 *
 * Cell symbols (used when rendering):
 *  '#' – wall
 *  '.' – walkable floor
 *  'T' – treasure item
 *  'M' – monster
 *  'E' – exit
 *  '@' – hero position (injected during render only)
 */
public class GameMap {

    private final List<List<Cell>> cells;
    private final int rows;
    private final int cols;
    private int startRow;
    private int startCol;
    private int exitRow;
    private int exitCol;

    /**
     * Builds the GameMap from a cell grid.
     * Monsters and treasures must be added separately by
     * addMonster() and addTreasure().
     *
     * @param cells 2D list of cell rows
     */
    public GameMap(List<List<Cell>> cells) {
        this.cells = cells;
        this.rows = cells.size();
        this.cols = cells.get(0).size();
    }

    /**
     * Gets a cell at row and col
     * @param row index
     * @param col index
     * @return cell at row,col
     */
    public Cell getCell(int row, int col) {
        return cells.get(row).get(col);
    }

    /**
     * Checks if there is a wall on the coordinates
     * @param row index
     * @param col index
     * @return true if cell is a wall
     */
    public boolean isWall(int row, int col) {
        return cells.get(row).get(col).isWall();
    }

    /**
     * Checks if coordinates are inside map bounds
     * @param row index
     * @param col index
     * @return true if the coordinates are inside the map border
     */
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Places a monster on its cell
     * @param monster instance to place
     */
    public void addMonster(Monster monster) {
        Position p = monster.getPosition();
        cells.get(p.getRow()).get(p.getCol()).setMonster(monster);
    }

    /**
     * @param row index
     * @param col index
     * @return the living monster at the given cell, or null.
     */
    public Monster monsterAt(int row, int col) {
        Monster m = cells.get(row).get(col).getMonster();
        return (m != null && m.isAlive()) ? m : null;
    }

    /**
     * Clears the monster reference from its cell.
     * @param monster reference
     */
    public void removeMonster(Monster monster) {
        Position p = monster.getPosition();
        cells.get(p.getRow()).get(p.getCol()).setMonster(null);
    }

    /**
     * Places a treasure on the given cell.
     * @param row index
     * @param col index
     * @param item reference
     */
    public void addTreasure(int row, int col, Item item) {
        cells.get(row).get(col).setTreasure(item);
    }

    /**
     * Returns the treasure at the given cell, or null.
     * @param row index
     * @param col index
     * @return item reference
     */
    public Item treasureAt(int row, int col) {
        return cells.get(row).get(col).getTreasure();
    }

    /**
     * Clears the treasure reference from the given cell.
     * @param row index
     * @param col index
     */
    public void removeTreasure(int row, int col) {
        cells.get(row).get(col).setTreasure(null);
    }

    /**
     * Marks a cell as the exit and records its coordinates.
     * Replaces the cell at that position with a new EXIT cell.
     * @param row index
     * @param col index
     */
    public void markExit(int row, int col) {
        exitRow = row;
        exitCol = col;
        cells.get(row).set(col, new Cell(Cell.Type.EXIT));
    }

    /**
     * Renders the map as a string, placing '@' at the hero position.
     * @param heroRow index
     * @param heroCol index
     * @return string representation of the map grid with game entitites
     */
    public String render(int heroRow, int heroCol) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (r == heroRow && c == heroCol) sb.append('@');
                else sb.append(cells.get(r).get(c).toChar());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Collects all monsters currently on the map by iterating cells.
     * Used by FileManager for save serialisation.
     *
     * @return List of monsters
     */
    public List<Monster> getMonsters() {
        List<Monster> result = new ArrayList<>();
        for (List<Cell> row : cells) {
            for (Cell cell : row) {
                if (cell.getMonster() != null) result.add(cell.getMonster());
            }
        }
        return result;
    }

    /**
     * Collects all treasures currently on the map
     * Used by FileManager for save serialisation.
     * @return Map of treasures
     */
    public Map<String, Item> getTreasures() {
        Map<String, Item> result = new LinkedHashMap<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Item t = cells.get(r).get(c).getTreasure();
                if (t != null) result.put(r + "," + c, t);
            }
        }
        return result;
    }


    // Getters and Setters

    public List<List<Cell>> getCells() {
        return cells;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int r) {
        this.startRow = r;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int c) {
        this.startCol = c;
    }

    public int getExitRow() {
        return exitRow;
    }

    public void setExitRow(int r) {
        this.exitRow = r;
    }

    public int getExitCol() {
        return exitCol;
    }

    public void setExitCol(int c) {
        this.exitCol = c;
    }
}
