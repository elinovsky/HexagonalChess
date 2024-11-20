package ru.vsu.cs.kuznetsov.scripts;

import java.util.List;

public interface Figure {
    public Factions getFaction();
    public List<Cell> getMoveOptions();
    public void moveTo(Cell cell);
    public List<Cell> getAttackingCells();

//    int[][] shifts = new int[][]{{-3, -2}, {0, 1}, {2, 1}, {1, 1}, {1, 2}, {0, 1},
//            {-2, 1}, {0, 1}, {-1, -1}, {-1, -1}, {-1, -2}, {0, -1}};
//    int[][] shifts = new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 1}, {-1, 0}, {0, 1}};
//    int[][] shifts = new int[][]{{-2, -1}, {1, -1}, {2, 1}, {0, 2}, {-2, 1}, {-1, -1}};
}
