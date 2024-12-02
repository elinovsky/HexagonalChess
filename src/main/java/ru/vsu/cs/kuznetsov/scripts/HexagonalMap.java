package ru.vsu.cs.kuznetsov.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class HexagonalMap {
    public static class Position{
        private Figure figure;
        private char col;
        private int row;

        Position(Figure figure, char col, int row){
            this.figure = figure;
            this.col = col;
            this.row = row;
        }

        Position(Figure figure, Position pos){
            this.col = pos.col;
            this.row = pos.row;
            this.figure = figure;
        }

        public Figure getFigure() {
            return figure;
        }

        public char getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Position position = (Position) o;
            return col == position.col && row == position.row;
        }

        @Override
        public int hashCode() {
            return Objects.hash(col, row);
        }
    }

    private class Cell{
        Cell[] sideNeighbors;
        int[] position;

        Figure placeHolder;

        Cell(char col, int row){
            sideNeighbors = new Cell[6];
            position = new int[]{col, row};
            placeHolder = null;
        }

        void symmetrySetNeighbour(int index, Cell newNeighbour){
            this.sideNeighbors[index] = newNeighbour;
            newNeighbour.sideNeighbors[(3 + index)%6] = this;
        }

        Cell getNeighbourByIndex(int index){
            if (index < 0){
                return sideNeighbors[index % 6 + 6];
            }
            return sideNeighbors[index % 6];
        }
    }

    public static final char[] validLetters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'k', 'l'};

    private Cell[][] map;

    HexagonalMap(){
        map = new Cell[11][];
        for (int i = 0; i < 11; i++){
            int newColSize = 11 - Math.abs(i - 5);
            map[i] = new Cell[newColSize];
            for (int j = 0; j < newColSize; j++){
                Cell newOne = new Cell(validLetters[i], j + 1);
                map[i][j] = newOne;
                if (j > 0) {
                    map[i][j - 1].symmetrySetNeighbour(5, newOne);
                }
                if (i > 0){
                    if (j > 0) {
                        map[i - 1][j - 1].symmetrySetNeighbour(4, newOne);
                    }
                    if (j < map[i - 1].length){
                        map[i - 1][j].symmetrySetNeighbour(5, newOne);
                    }
                }
            }
        }
    }

    public boolean isCoordinatesValid(int col, int row){
        if (col < 'a' || col > 'l' || col == 'j'){
            return false;
        }
        return (row >= 1 && row <= 11 - Math.abs((col < 'j'?col - 'a':col - 'a' - 1) - 5));
    }

    private Cell getCellNotSafe(int col, int row){
        return map[(col < 'j')?col - 'a':col - 'a' - 1][row - 1];
    }

    private Position cell2position(Cell cell){
        return new Position(cell.placeHolder, (char)cell.position[0], cell.position[1]);
    }

    /**
     * @param col - letta of "column" can be from 'a' to 'l' except 'j'.
     * @param row - number of "row" can be from 1 to 11.
     * @return cell with (row, col) board coordinates
     */
    public Position getCellState(char col, int row){
        if (!isCoordinatesValid(col, row)){
            throw new RuntimeException("Invalid board coordinates.");
        }
        return cell2position(getCellNotSafe(col, row));
    }

    /**
     *  \   0  /
     *   \____/
     * 5 /    \ 1
     * _/  cur \ _
     *  \ Cell /
     * 4 \____/ 2
     *   / 3  \
     * @param curPos - cell from which we go
     * @param direction - integer number of current cell side, can
     *                 be cycled like angle in cos or sin functions
     * @return state of cell connected to current with side
     *              numbered direction (see ascii image above)
     *              or null if direction pointing on board side
     */
    public Position getPositionByDir(Position curPos, int direction){
        checkSafety(curPos);
        Cell targetCell = getCellNotSafe(curPos.col, curPos.row).getNeighbourByIndex(direction);
        if (targetCell == null){
            return null;
        }
        return cell2position(targetCell);
    }

    /**
     *  \   0  /
     *   \____/
     * 5 /    \ 1
     * _/  cur \ _
     *  \ Cell /
     * 4 \____/ 2
     *   / 3  \
     * @param curPos - start of path
     * @param directionsPath - sequence of directions. Every direction
     *                       number of current cell side, can be cycled
     *                       like angle in cos or sin functions
     * @return state of cell after pass through path or null if path breaks in board side
     */
    public Position getPosAfterPath(Position curPos, int[] directionsPath){
        checkSafety(curPos);
        Cell curCell = getCellNotSafe(curPos.col, curPos.row);
        for (int dir : directionsPath){
            curCell = curCell.getNeighbourByIndex(dir);
            if (curCell == null){
                return null;
            }
        }
        return cell2position(curCell);
    }

    private void checkSafety(Position pos){
        if (!isCoordinatesValid(pos.col, pos.row)){
            throw new RuntimeException("Invalid board coordinates.");
        }
    }

    public List<Position> getAllPosThroughDir(Position curPos, int direction){
        checkSafety(curPos);
        List <Position> res = new ArrayList<>();
        Cell curCell = getCellNotSafe(curPos.col, curPos.row).getNeighbourByIndex(direction);
        while (curCell != null){
            res.add(cell2position(curCell));
            curCell = curCell.getNeighbourByIndex(direction);
        }
        return res;
    }

    public List<Position> getPosAfterPathAnyTimes(Position curPos, int[] directionsPath){
        checkSafety(curPos);
        List <Position> res = new ArrayList<>();
        Cell curCell = getCellNotSafe(curPos.col, curPos.row);
        while (curCell != null){
            for(int dir : directionsPath){
                curCell = curCell.getNeighbourByIndex(dir);
                if (curCell == null){
                    break;
                }
            }
            if (curCell != null){
                res.add(cell2position(curCell));
            }
        }
        return res;
    }

    public void setFigure(int col, int row, Figure newFigure){
        if(!isCoordinatesValid(col, row)){
            throw new RuntimeException("Invalid board coordinates.");
        }
        map[(col < 'j')?col - 'a':col - 'a' - 1][row - 1].placeHolder = newFigure;
    }

    public void setFigure(Position position, Figure newFigure){
        setFigure(position.col, position.row, newFigure);
    }

    public List<Position> getBoardState(){
        List<Position> result = new ArrayList<>();
        for (Cell[] cells : map) {
            for (int j = cells.length - 1; j >= 0; j--) {
                result.add(cell2position(cells[j]));
            }
        }
        return result;
    }
}
