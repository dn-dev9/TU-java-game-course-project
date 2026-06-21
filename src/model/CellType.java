package model;

/**
 * The type of a map cell.
 *   WALL  – impassable boundary or obstacle
 *   FLOOR – walkable empty tile
 *   EXIT  – the level exit tile
 */
public enum CellType {
    WALL,
    FLOOR,
    EXIT
}
