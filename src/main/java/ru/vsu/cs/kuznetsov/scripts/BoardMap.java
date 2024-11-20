package ru.vsu.cs.kuznetsov.scripts;

public class BoardMap {
    Cell[][] map;

    BoardMap(){
        map = new Cell[11][];
        for (int i = 0; i < 11; i++){
            map[i] = new Cell[11 - Math.abs(i - 5)];
        }
    }

    public boolean isCoordinatesValid(int col, int row){
        if (col < 'a' || col > 'l' || col == 'j'){
            return (row >= 1 && row <= 11 - Math.abs((col < 'j'?col - 'a':col - 'a' - 1) - 5));
        }
        return false;
    }

    private Cell getCellNotSafe(int col, int row){
        return map[(col < 'j')?col - 'a':col - 'a' - 1][row - 1];
    }

    /**
     * @param col - letta of "column" can be from 'a' to 'l' except 'j'.
     * @param row - number of "row" can be from 1 to 11.
     * @return cell with (row, col) board coordinates
     */
    public Cell getCell(char col, int row){
        if (!isCoordinatesValid(col, row)){
            throw new RuntimeException("Invalid board coordinates.");
        }
        return getCellNotSafe(col, row);
    }
}
