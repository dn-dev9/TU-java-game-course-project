package model;

/**
 * Represents a grid position
 */
public class Position {

    private int row;
    private int col;

    /**
     * @param row the row index (0-based)
     * @param col the column index (0-based)
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }

    public int getCol() { return col; }

    public void setRow(int row) { this.row = row; }

    public void setCol(int col) { this.col = col; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position p)) return false;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return row + col;
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
