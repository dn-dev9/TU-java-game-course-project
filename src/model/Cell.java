package model;

import items.Item;

/**
 * Represents a single cell on the game map.
 * Owns its monster and treasure references
 */
public class Cell {

    /**
     * The cell Type enum
     */
    public enum Type {
        WALL,
        FLOOR,
        EXIT
    }

    private final Type type;
    private Monster monster;
    private Item treasure;

    /**
     * @param type type of cell
     */
    public Cell(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    /**
     * Checks if the cell is of type Wall
     * @return true if WALL
     */
    public boolean isWall() {
        return type == Type.WALL;
    }

    /**
     * Checks if the cell is of type EXIT
     * @return true if EXIT
     */
    public boolean isExit() {
        return type == Type.EXIT;
    }

    /**
     * @return monster instance
     */
    public Monster getMonster() {
        return monster;
    }

    /**
     * @param m monster to set
     */
    public void setMonster(Monster m) {
        this.monster = m;
    }

    /**
     * @return treasure instance
     */
    public Item getTreasure() {
        return treasure;
    }

    /**
     * @param t treasure to set
     */
    public void setTreasure(Item t) {
        this.treasure = t;
    }

    /**
     * Monster > treasure > cell type
     * @return the character that represents this cell when rendering the map.
     */
    public char toChar() {
        if (monster != null && monster.isAlive()) return 'M';
        if (treasure != null) return 'T';
        return switch (type) {
            case WALL  -> '#';
            case FLOOR -> '.';
            case EXIT  -> 'E';
        };
    }
}
