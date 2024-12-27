package ru.vsu.cs.kuznetsov.scripts;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Figure {

    protected Factions faction;
    protected HexagonalMap.Position position;
    protected HexagonalMap actionField;

    protected final int[][] pathsToDiagonal = new int[][]{
            {0, 5}, {0, 1}, {2, 1},
            {2, 3}, {4, 3}, {4, 5}
    };

    protected static String[] notationCodes = new String[]{"P", "N", "B", "R", "Q", "K"};

    /**
     * @return figures notation codes in order {Pawn, Knight, Bishop, Rook, Queen, King}
     */
    public static String[] getNotationCodes(){
        return notationCodes;
    }

//    /**
//     * Function made to change notation localization.
//     * @param newNotationCodes code consider next order {Pawn, Knight, Bishop, Rook, Queen, King}
//     */
//    public static void setNotationCodes(String[] newNotationCodes){
//        notationCodes = newNotationCodes;
//    }

    /**
     * @return faction of figure
     */
    public Factions getFaction(){
        return faction;
    }

    /**
     * @return position where figure stands
     */
    public HexagonalMap.Position getPosition(){
        return position;
    }

    /**
     * @return cells where figure can move that turn
     */
    public List<HexagonalMap.Position> getMoveOptions(){
        return null;
    }

    /**
     * @return cells which can be attacked from this figure if enemy figure appears on it
     */
    public List<HexagonalMap.Position> getAttackingCells(){
        return null;
    }

    public void moveTo(HexagonalMap.Position cell){
        actionField.setFigure(position.getCol(), position.getRow(), null);
        position = cell;
        actionField.setFigure(cell.getCol(), cell.getRow(), this);
    }

    /**
     * Converts faction to strung faction name
     */
    public static final String getFactionName(Factions faction){
        switch (faction){
            case BLACK -> {return "black";}
            case WHITE -> {return "white";}
        }
        return "";
    }

    public static final Factions getFactionByName(String name){
        switch (name){
            case "black" -> {return Factions.BLACK;}
            case "white" -> {return Factions.WHITE;}
        }
        return Factions.NO_FIGURE;
    }

    public Figure(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
        this.actionField = actionField;
        this.faction = faction;
        this.position = position;
    }

    /**
     * @return list of empty positions in one line ended with position where predicate is true
     * */
    protected List<HexagonalMap.Position> getCellsFromLineByPredicate(List<HexagonalMap.Position> posList,
            Predicate<HexagonalMap.Position> predicate){
        if(posList == null){
            return null;
        }
        List<HexagonalMap.Position> result = new ArrayList<>();
        for(HexagonalMap.Position pos : posList){
            if (pos.getFigure() == null){
                result.add(pos);
            } else if (predicate.test(pos)){
                result.add(pos);
                break;
            } else {
                break;
            }
        }
        return result;
    }

    /**
     *
     * @param pos position we need to check
     * @param figures figures that may check position pos
     * @return true if position checked by any figure false else
     */
    public static boolean isPositionChecked(HexagonalMap.Position pos, List<Figure> figures){
        boolean isPosChecked = false;
        for (Figure enemy : figures){
            if (enemy.getAttackingCells().contains(pos)){
                isPosChecked = true;
                break;
            }
        }
        return isPosChecked;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o.getClass() != this.getClass()) return false;
        Figure other = (Figure) o;
        return other.position.getCol() == this.position.getCol() &&
                other.position.getRow() == this.position.getRow();
    }

    public boolean canBeChanged(){
        return false;
    }

    /**
     * @return code of this figure
     */
    public abstract String getNotationCode();

    /**
     * @return name of image in assets associated with that figure
     */
    public abstract String getImageName();

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
            return getFactionName(this.faction) + "Pawn.png";
        }

        @Override
        public void moveTo(HexagonalMap.Position cell) {
            if (position.getCol() >= 'f' && cell.getCol() >= position.getCol() ||
                    position.getCol() <= 'f' && cell.getCol() <= position.getCol()) {
                canDoLongStep = false;
            }
//            if (Math.abs(cell.getRow() - position.getRow()) == 2){
//                super.moveTo(actionField.getPositionByDir(position, forwardDirection));
//                for (int i = forwardDirection - 1; i <= forwardDirection + 1; i+=4){
//                    Figure fig = actionField.getPositionByDir(position, forwardDirection).getFigure();
//                    if (fig != null) {
//                        System.out.println(fig.getClass() == this.getClass());
//                    }
//                    if (fig != null && fig.getClass() == this.getClass()){
//                        System.out.println(1);
//                        ((Pawn)fig).inPassing = this.position;
//                    }
//                }
//            }
            super.moveTo(cell);
        }

        @Override
        public String getNotationCode() {
            return notationCodes[0];
        }

        public boolean canBeChanged(){
            if (faction == Factions.WHITE){
                return (11 - Math.abs(- 5 + ((position.getCol() < 'j')?(position.getCol() - 'a'):
                        (position.getCol() - 'a' - 1))) == position.getRow());
            }
            return position.getRow() == 1;
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
            List<HexagonalMap.Position> res = new ArrayList<>();
            int[][] gShapedPaths = new int[][]{
                    {0, 0, 5}, {0, 0, 1}, {1, 1, 0}, {1, 1, 2},
                    {2, 2, 1}, {2, 2, 3}, {3, 3, 2}, {3, 3, 4},
                    {4, 4, 3}, {4, 4, 5}, {5, 5, 4}, {5, 5, 0}
            };
            for (int[] path : gShapedPaths){
                HexagonalMap.Position pos = actionField.getPosAfterPath(position, path);
                if (pos != null){
                    res.add(pos);
                }
            }
            return res;
        }

        @Override
        public String getImageName() {
            return getFactionName(this.faction) + "Knight.png";
        }

        @Override
        public String getNotationCode() {
            return notationCodes[1];
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
                res.addAll(getCellsFromLineByPredicate(actionField.getPosAfterPathAnyTimes(position, path),
                        (a)->(a.getFigure().getFaction() != this.getFaction())));
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            for (int[] path : pathsToDiagonal){
                res.addAll(getCellsFromLineByPredicate(actionField.getPosAfterPathAnyTimes(position, path),
                        (a)->(a.getFigure() != null)));
            }
            return res;
        }

        @Override
        public String getImageName() {
            return getFactionName(this.faction) + "Bishop.png";
        }

        @Override
        public String getNotationCode() {
            return notationCodes[2];
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
                res.addAll(getCellsFromLineByPredicate(actionField.getAllPosThroughDir(position, dir),
                        (a)->(a.getFigure().getFaction() != this.getFaction())));
            }
            return res;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            List<HexagonalMap.Position> res = new ArrayList<>();
            for (int dir = 0; dir < 6; dir++){
                res.addAll(getCellsFromLineByPredicate(actionField.getAllPosThroughDir(position, dir),
                        (a)->(a.getFigure() != null)));
            }
            return res;
        }

        @Override
        public String getImageName() {
            return getFactionName(this.faction) + "Rook.png";
        }

        @Override
        public String getNotationCode() {
            return notationCodes[3];
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
                result.addAll(getCellsFromLineByPredicate(actionField.getAllPosThroughDir(position, dir),
                        (a)->(a.getFigure().getFaction() != this.getFaction())));
            }
            for (int[] path : pathsToDiagonal){
                result.addAll(getCellsFromLineByPredicate(actionField.getPosAfterPathAnyTimes(position, path),
                        (a)->(a.getFigure().getFaction() != this.getFaction())));
            }
            return result;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            List<HexagonalMap.Position> result = new ArrayList<>();
            for (int dir = 0; dir < 6; dir++){
                result.addAll(getCellsFromLineByPredicate(actionField.getAllPosThroughDir(position, dir),
                        (a)->(a.getFigure().getFaction() != null)));
            }
            for (int[] path : pathsToDiagonal){
                result.addAll(getCellsFromLineByPredicate(actionField.getPosAfterPathAnyTimes(position, path),
                        (a)->(a.getFigure() != null)));
            }
            return result;
        }

        @Override
        public String getImageName() {
            return getFactionName(this.faction) + "Queen.png";
        }

        @Override
        public String getNotationCode() {
            return notationCodes[4];
        }
    }

    public static class King extends Figure{

        private boolean isAttacked = false;

        private List <HexagonalMap.Position> availableMoves;

        public King(HexagonalMap actionField, HexagonalMap.Position position, Factions faction){
            super(actionField, position, faction);
            availableMoves = new ArrayList<>();
        }

        @Override
        public String getImageName() {
            if (isAttacked){
                return getFactionName(this.faction) + "KingWarning.png";
            }
            return getFactionName(this.faction) + "King.png";
        }

        /**
         * Updates cells where king can move
         * @param hostiles - List of enemy figures.
         */
        public void updateAvailablePositions(List<Figure> hostiles){
            availableMoves = new ArrayList<>();
            HexagonalMap.Position leaving = position;
            for (int dirCounter = 0; dirCounter < 6; dirCounter++){
                HexagonalMap.Position pos = position;
                for (int dir = 0; dir <= 1; dir++){
                    pos = actionField.getPositionByDir(pos, dirCounter + dir);
                    if (pos == null){
                        break;
                    }
                    Figure removing = pos.getFigure();
                    if (removing != null && removing.faction == this.faction){
                        continue;
                    } else if (removing != null){
                        hostiles.remove(removing);
                    }
                    moveTo(pos);
                    if  (!isPositionChecked(pos, hostiles)) {
                        availableMoves.add(pos);
                    }
                    moveTo(leaving);
                    if (removing != null){
                        hostiles.add(removing);
                        actionField.setFigure(pos, removing);
                    }
                }
            }
            isAttacked = isPositionChecked(position, hostiles);
        }

        @Override
        public List<HexagonalMap.Position> getMoveOptions() {
//            List<HexagonalMap.Position> result = new ArrayList<>();
//            for (int dir : availableDirections){
//                HexagonalMap.Position pos = actionField.getPositionByDir(position, dir);
//                if (pos != null && (pos.getFigure() == null ||pos.getFigure() != null
//                        && pos.getFigure().getFaction() != faction)){
//                    result.add(pos);
//                }
//            }
//            for (int[] path : availableDiagonals){
//                HexagonalMap.Position pos = actionField.getPosAfterPath(position, path);
//                if (pos != null && (pos.getFigure() == null ||pos.getFigure() != null
//                        && pos.getFigure().getFaction() != faction)){
//                    result.add(pos);
//                }
//            }
//            return result;
            return availableMoves;
        }

        boolean isUnderMate(List<Figure> hostiles, List<Figure> allys){
            if (!isAttacked || !availableMoves.isEmpty()) {
                return false;
            }
            List <Figure> attackingFigures = new ArrayList<>();
            for (Figure enemy : hostiles){
                if (enemy.getMoveOptions().contains(position)){
                    attackingFigures.add(enemy);
                }
            }
            if (attackingFigures.size() >= 2){
                return true;
            }
            Figure attacker = attackingFigures.get(0);
            List<HexagonalMap.Position> attackingPosition = new ArrayList<>();
            int dirInd = 0;
            if (attacker.position.getCol() == position.getCol()){
                if (attacker.position.getRow() < position.getRow())
                    dirInd = 3;
            } else if (attacker.position.getCol() < position.getCol()){
                dirInd = 4;
            } else {
                dirInd = 1;
            }
            for (int dir = dirInd; dir < dirInd + 6; dir++)
                if (attackingPosition.addAll(getCellsFromLineByPredicate(
                        actionField.getAllPosThroughDir(position, dir), (a)->(attacker.equals(a))))) break;
            if (attackingPosition.isEmpty()){
                for (int diagInd = dirInd; diagInd < dirInd + 6; diagInd++)
                    if (attackingPosition.addAll(getCellsFromLineByPredicate(
                            actionField.getPosAfterPathAnyTimes(position, pathsToDiagonal[diagInd % 6]),
                            (a)->(attacker.equals(a))))) break;
            }
            for (Figure ally : allys){
                List <HexagonalMap.Position> defended = ally.getMoveOptions();
                if (attackingPosition.stream().anyMatch(a-> defended.contains(a)))
                    return false;
                if (attackingPosition.isEmpty()) {
                    if (defended.contains(attacker.position))
                        return false;
                }
            }
            return true;
        }

        @Override
        public List<HexagonalMap.Position> getAttackingCells() {
            List<HexagonalMap.Position> result = new ArrayList<>();
            for (int dir = 0; dir < 6; dir++){
                HexagonalMap.Position pos = actionField.getPositionByDir(position, dir);
                if (pos != null)
                    result.add(pos);
            }
            for (int[] path : pathsToDiagonal){
                HexagonalMap.Position pos = actionField.getPosAfterPath(position, path);
                if (pos != null)
                    result.add(pos);
            }
            return result;
        }

        @Override
        public String getNotationCode() {
            return notationCodes[5];
        }
    }
}
