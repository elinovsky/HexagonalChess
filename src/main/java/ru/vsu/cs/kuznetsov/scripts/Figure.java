package ru.vsu.cs.kuznetsov.scripts;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class Figure {

    protected Factions faction;
    protected HexagonalMap.Position position;
    protected HexagonalMap actionField;

    protected final int[][] pathsToDiagonal = new int[][]{
            {0, 5}, {0, 1}, {2, 1},
            {2, 3}, {4, 3}, {4, 5}
    };

    /**
     * @return faction of figure
     */
    public Factions getFaction(){
        return faction;
    }

    /**
     * @return cells where figure can move that turn
     */
    public List<HexagonalMap.Position> getMoveOptions(){
        return null;
    }
    public void moveTo(HexagonalMap.Position cell){
        actionField.setFigure(position.getCol(), position.getRow(), null);
        position = cell;
        actionField.setFigure(cell.getCol(), cell.getRow(), this);
    }

    /**
     * @return cells which can be attacked from figure
     */
    public List<HexagonalMap.Position> getAttackingCells(){
        return null;
    }

    public Figure(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
        this.actionField = actionField;
        this.faction = faction;
        this.position = position;
    }

    /**
     * @return list of empty position with attacked positions from posList keeps order stops on first met attacked position
     */
    protected List<HexagonalMap.Position> getEmptyAndAttacking(List<HexagonalMap.Position> posList){
        List<HexagonalMap.Position> result = new ArrayList<>();
        Iterator<HexagonalMap.Position> posIterator = posList.iterator();
        HexagonalMap.Position pos = null;
        while (posIterator.hasNext()){
            pos = posIterator.next();
            if (pos.getFigure() != null) {
                break;
            }
            result.add(pos);
        }
        if (pos != null && pos.getFigure() != null && pos.getFigure().getFaction() != this.getFaction()){
            result.add(pos);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o.getClass() != this.getClass()) return false;
        Figure other = (Figure) o;
        return other.position.getCol() == this.position.getCol() &&
                other.position.getRow() == this.position.getRow();
    }

    /**
     * @return name of image in assets associated with that figure
     */
    public String getImageName(){
        return null;
    }

    /**
     * @return position where figure stands
     */
    public HexagonalMap.Position getPosition(){
        return position;
    }

    public static class Pawn extends Figure{
        private int forwardDirection;
        private HexagonalMap.Position inPassing;
        private boolean canDoLongStep;

        public Pawn(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
            inPassing = null;
            canDoLongStep = true;
            if (faction == Factions.WHITE) {
                forwardDirection = 0;
            } else {
                forwardDirection = 3;
            }
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            HexagonalMap.Position pos = actionField.getPositionByDir(position, forwardDirection);
            if (pos != null && pos.getFigure() == null){
                res.add(pos);
                pos = actionField.getPositionByDir(pos, forwardDirection);
                if (canDoLongStep && pos != null && pos.getFigure() == null) {
                    res.add(pos);
                }
            }
            for (HexagonalMap.Position p : getAttackingCells()){
                if (p.getFigure() != null && p.getFigure().getFaction() != this.faction){
                    res.add(p);
                }
            }
            if (inPassing != null){
                res.add(inPassing);
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            for (int i = forwardDirection - 1; i <= forwardDirection + 1; i += 2){
                HexagonalMap.Position attacking = actionField.getPositionByDir(position, i);
                if (attacking != null) {
                    res.add(attacking);
                }
            }
            return res;
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    return "whitePawn.png";
                }
                case BLACK -> {
                    return "blackPawn.png";
                }
            }
            return "";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            if (Math.abs(cell.getRow() - position.getRow()) == 2 && canDoLongStep){
//                Figure left = actionField.getPosAfterPath(position, new int[]{forwardDirection, forwardDirection - 1}).getFigure();
//                Figure right = actionField.getPosAfterPath(position, new int[]{forwardDirection, forwardDirection + 1}).getFigure();
//                if (left != null && left.getClass() == this.getClass()){
//                    Pawn p = (Pawn) left;
//                    p.inPassing = new HexagonalMap.Position(this, actionField.getPositionByDir(position, forwardDirection));
//                }
//                if (right != null && right.getClass() == this.getClass()){
//                    Pawn p = (Pawn) right;
//                    p.inPassing = new HexagonalMap.Position(this, actionField.getPositionByDir(position, forwardDirection));
//                }
                canDoLongStep = false;
            }
            super.moveTo(cell);
        }
    }

    public static class Knight extends Figure{

        public Knight(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            int[][] gShapedPaths = new int[][]{
                    {0, 0, 5}, {0, 0, 1}, {1, 1, 0}, {1, 1, 2},
                    {2, 2, 1}, {2, 2, 3}, {3, 3, 2}, {3, 3, 4},
                    {4, 4, 3}, {4, 4, 5}, {5, 5, 4}, {5, 5, 0}
            };
            for (int[] path : gShapedPaths){
                HexagonalMap.Position pos = actionField.getPosAfterPath(position, path);
                if (pos != null && (pos.getFigure() == null ||
                        pos.getFigure() != null && pos.getFigure().getFaction() != this.faction)){
                    res.add(pos);
                }
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            return getMoveOptions();
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    return "whiteKnight.png";
                }
                case BLACK -> {
                    return "blackKnight.png";
                }
            }
            return "";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            super.moveTo(cell);
        }
    }

    public static class Bishop extends Figure {

        public Bishop(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            for (int[] path : pathsToDiagonal){
                res.addAll(getEmptyAndAttacking(actionField.getPosAfterPathAnyTimes(position, path)));
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            return getMoveOptions();
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    return "whiteBishop.png";
                }
                case BLACK -> {
                    return "blackBishop.png";
                }
            }
            return "";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            super.moveTo(cell);
        }
    }

    public static class Rook extends Figure{
        public Rook(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            for (int dir = 0; dir < 6; dir++){
                res.addAll(getEmptyAndAttacking(actionField.getAllPosThroughDir(position, dir)));
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            return getMoveOptions();
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    return "whiteRook.png";
                }
                case BLACK -> {
                    return "blackRook.png";
                }
            }
            return "";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            super.moveTo(cell);
        }
    }

    public static class Queen extends Figure{
        public Queen(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> result = new ArrayList<>();
            for (int dir = 0; dir < 6; dir++){
                result.addAll(getEmptyAndAttacking(actionField.getAllPosThroughDir(position, dir)));
            }
            for (int[] path : pathsToDiagonal){
                result.addAll(getEmptyAndAttacking(actionField.getPosAfterPathAnyTimes(position, path)));
            }
            return result;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            return getMoveOptions();
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    return "whiteQueen.png";
                }
                case BLACK -> {
                    return "blackQueen.png";
                }
            }
            return "";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            super.moveTo(cell);
        }
    }

    public static class King extends Figure{

        private boolean isAttacked = false;

        private List<Integer> availableDirections;
        private List<int[]> availableDiagonals;

        public King(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
            availableDirections = Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5});
            availableDiagonals = Arrays.asList(pathsToDiagonal);
        }

        @Override
        public String getImageName() {
            switch (faction){
                case WHITE -> {
                    if (isAttacked){
                        return "whiteKingWarning.png";
                    }
                    return "whiteKing.png";
                }
                case BLACK -> {
                    if (isAttacked){
                        return "blackKingWarning.png";
                    }
                    return "blackKing.png";
                }
            }
            return "";
        }

        public void attacked(){
            isAttacked = true;
        }

        public boolean isAttacked(){
            return isAttacked;
        }

        public boolean isPositionChecked(HexagonalMap.Position pos, List<Figure> figures){
            boolean isPosChecked = false;
            for (Figure enemy : figures){
                if (enemy.getAttackingCells().contains(pos)){
                    isPosChecked = true;
                    break;
                }
            }
            return isPosChecked;
        }

        /**
         * Updates cells where king can move
         * @param enemys - List of enemy figures.
         */
        public void updateAvailablePositions(List<Figure> enemys){
            availableDirections = IntStream.rangeClosed(0, 5).
                    filter((int a)->(!isPositionChecked(actionField.getPositionByDir(position, a), enemys))).
                    boxed().collect(Collectors.toList());
            availableDiagonals = Arrays.stream(pathsToDiagonal).filter((a) ->
                    (!isPositionChecked(actionField.getPosAfterPath(position, a), enemys))).collect(Collectors.toList());
            isAttacked = isPositionChecked(position, enemys);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
            List<HexagonalMap.Position> result = new ArrayList<>();
            for (int dir : availableDirections){
                HexagonalMap.Position pos = actionField.getPositionByDir(position, dir);
                if (pos != null && (pos.getFigure() == null ||pos.getFigure() != null
                        && pos.getFigure().getFaction() != faction)){
                    result.add(pos);
                }
            }
            for (int[] path : availableDiagonals){
                HexagonalMap.Position pos = actionField.getPosAfterPath(position, path);
                if (pos != null && (pos.getFigure() == null ||pos.getFigure() != null
                        && pos.getFigure().getFaction() != faction)){
                    result.add(pos);
                }
            }
            return result;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            return getMoveOptions();
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            super.moveTo(cell);
        }
    }
}
